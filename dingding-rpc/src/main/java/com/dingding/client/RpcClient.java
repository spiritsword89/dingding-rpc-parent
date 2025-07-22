package com.dingding.client;

import com.dingding.exceptions.RequestClassNotDetermineException;
import com.dingding.handler.ClientHeartbeatHandler;
import com.dingding.handler.JsonCallMessageEncoder;
import com.dingding.handler.JsonMessageDecoder;
import com.dingding.handler.RpcClientMessageHandler;
import com.dingding.model.MarkAsRpc;
import com.dingding.model.MessagePayload;
import com.dingding.model.MessageType;
import com.dingding.model.RpcMethodDescriptor;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


// 自定义注解 放在其他服务上， SpringbootApplication
// 自定义注解，clientId, basePackages

// 处理RPC请求
// 1. 发现RPC服务
// 例如：User服务，自定义逻辑允许某些特定方法接收RPC请求
// 扫描支持RPC的所有方法

// 创建并发送RPC请求

public class RpcClient implements SmartInitializingSingleton, ApplicationContextAware {
    private static final Logger logger =  LoggerFactory.getLogger(RpcClient.class);

    private ApplicationContext applicationContext;

    private NioEventLoopGroup workerGroup = new NioEventLoopGroup();

    private String clientId;

    private String[] scanPackages; //扫描的包， 里面都是暴漏出去的RPC服务

    private Channel channel;

    private Map<String, Map<String, RpcMethodDescriptor>> classMethodDescriptorMap = new HashMap<>();

    private Map<String, Method> reflectedMethodMap = new HashMap<>();

    private Map<String, CompletableFuture<MessagePayload.RpcResponse>> requestMap = new ConcurrentHashMap<>();

    @Value("${dingding.rpc.server.host}")
    private String host;

    @Value("${dingding.rpc.server.port}")
    private Integer port;

    private int retryCount = 0;

    private int maxRetryCount;

    @PostConstruct
    public void initialize() {
        System.out.println("Rpc Client 启动");
        new Thread(this::connect).start();
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String[] getScanPackages() {
        return scanPackages;
    }

    public void setScanPackages(String[] scanPackages) {
        this.scanPackages = scanPackages;
    }

    public void connect() {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap
                    .group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new JsonCallMessageEncoder());
                            socketChannel.pipeline().addLast(new JsonMessageDecoder());
                            socketChannel.pipeline().addLast(new IdleStateHandler(0, 5, 0));
                            socketChannel.pipeline().addLast(new ClientHeartbeatHandler());
                            //假设已经有了Handler接收返回的结果
                            //1. 处理返回结果
                            //2. 处理接收到的请求
                            socketChannel.pipeline().addLast(new RpcClientMessageHandler(RpcClient.this));
                        }
                    });

            bootstrap.connect(host, port).addListener(future -> {
                if (future.isSuccess()) {
                    logger.info("Client ID: {} 连接成功", clientId);
                    ChannelFuture channelFuture = (ChannelFuture) future;
                    this.channel = channelFuture.channel();
                    sendRegistrationRequest();

                    channelFuture.channel().closeFuture().sync();
                } else {
                    System.out.println("Failed to connect server");
                    reconnect();
                }
            });
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new RuntimeException();
        }
    }

    public void reconnect() {
        workerGroup.schedule(this::connect, 5, TimeUnit.SECONDS);
    }

    public void sendRegistrationRequest() {
        MessagePayload msg = new MessagePayload.MessageBuilder().setClientId(this.clientId).setMessageType(MessageType.REGISTER).build();
        this.channel.writeAndFlush(msg);
    }

    public void sendRequest(Object request, String requestId, CompletableFuture<MessagePayload.RpcResponse> future) {
        this.requestMap.put(requestId, future);
        channel.writeAndFlush(request); //发送RPC请求到服务端
    }

    public void processRequest(MessagePayload messagePayload) throws NoSuchMethodException {
        MessagePayload.RpcRequest request = (MessagePayload.RpcRequest) messagePayload.getPayload();

        //com.dingding_demo.common.booking.BookingDetailService
        String requestClassName = request.getRequestClassName();

        String methodName = request.getRequestMethodSimpleName();
        String[] paramTypes = request.getParamTypes();
        String returnValueType = request.getReturnValueType();

        String methodId = RpcMethodDescriptor.generateMethodId(methodName, paramTypes.length, paramTypes, returnValueType);

        Class<?> requestedClass = null;
        try {
            requestedClass = Class.forName(requestClassName);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e);
        }

        //找出com.dingding_demo.common.booking.BookingDetailService的具体实现类
        Map<String, ?> beansOfType = applicationContext.getBeansOfType(requestedClass);

        if(beansOfType.isEmpty()){
            logger.error("Class of implementation is not found");
            throw new RuntimeException();
        }

        //扩展点
        if(beansOfType.size() > 1) {
            logger.error("More than one bean of type {} found", requestClassName);
            throw new RequestClassNotDetermineException();
        }

        //真正的服务提供者
        Object requestedClassBean = beansOfType.values().iterator().next();

        String className = requestedClassBean.getClass().getName();

        Map<String, RpcMethodDescriptor> rpcMethods = classMethodDescriptorMap.get(className);

        RpcMethodDescriptor rpcMethodDescriptor = rpcMethods.get(methodId);

        //validation
        if(rpcMethodDescriptor == null) {
            throw new NoSuchMethodException();
        }

        //提供更加具体的验证步骤
        //自己编写一套validate的规则
        if(rpcMethodDescriptor.getMethodName().equals(request.getRequestMethodSimpleName())
                && rpcMethodDescriptor.getNumOfParams() == paramTypes.length) {
            Method method = reflectedMethodMap.get(methodId);
            try {
                Object result = method.invoke(requestedClassBean, request.getParams());
                MessagePayload response = new MessagePayload.MessageBuilder()
                        .setRequestId(request.getRequestId())
                        .setMessageType(MessageType.RESPONSE)
                        .setReturnValue(result).build();

                channel.writeAndFlush(response);

            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.error(e.getMessage(),e);
            }
        }
    }

    public void completeRequest(MessagePayload.RpcResponse rpcResponse) {
        String requestId = rpcResponse.getRequestId();
        CompletableFuture<MessagePayload.RpcResponse> rpcResponseCompletableFuture = requestMap.get(requestId);
        rpcResponseCompletableFuture.complete(rpcResponse);
        this.requestMap.remove(requestId);
    }

    @Override
    public void afterSingletonsInstantiated() {
        System.out.println("RPC methods scanning started");

        List<Class<?>> rpcClasses = new ArrayList<>();
        //com.dingding_demo.booking.service
        for(String scanPackage : scanPackages){
            scanPackage = scanPackage.replace("\\.", "/");
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AssignableTypeFilter(Object.class));

            Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(scanPackage);

            for(BeanDefinition bd: candidateComponents){
                try {
                    Class<?> scannedClass = Class.forName(bd.getBeanClassName());
                    if(RemoteService.class.isAssignableFrom(scannedClass)){
                        rpcClasses.add(scannedClass);
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        //到这里的时候，rpcClasses已经包含了所有RemoteService的实现类
        //接下来，我们需要找出哪些方法被允许远程调用（MarkAsRpc）

        //Class是Rpc接口的实现类
        //例如: com.dingding_demo.booking.service.BookingDetailServiceImpl
        for(Class<?> clazz: rpcClasses) {
            Method[] declaredMethods = clazz.getDeclaredMethods();

            for(Method declaredMethod: declaredMethods){
                if(declaredMethod.isAnnotationPresent(MarkAsRpc.class)) {
                    RpcMethodDescriptor md = RpcMethodDescriptor.build(declaredMethod);
                    classMethodDescriptorMap.computeIfAbsent(clazz.getName(), k -> new HashMap<>()).put(md.getMethodId(), md);
                    reflectedMethodMap.put(md.getMethodId(), declaredMethod);
                }
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
