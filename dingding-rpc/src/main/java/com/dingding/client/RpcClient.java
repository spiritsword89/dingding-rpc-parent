package com.dingding.client;

import com.dingding.model.MarkAsRpc;
import com.dingding.model.RpcMethodDescriptor;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.reflect.Method;
import java.util.*;


// 自定义注解 放在其他服务上， SpringbootApplication

// 处理RPC请求
// 1. 发现RPC服务
// 例如：User服务，自定义逻辑允许某些特定方法接收RPC请求
// 扫描支持RPC的所有方法

// 创建并发送RPC请求
public class RpcClient implements SmartInitializingSingleton {
    private static final Logger logger =  LoggerFactory.getLogger(RpcClient.class);

    private NioEventLoopGroup workerGroup = new NioEventLoopGroup();

    private String clientId;

    private String[] scanPackages; //扫描的包， 里面都是暴漏出去的RPC服务

    private Channel channel;

    private Map<String, Map<String, RpcMethodDescriptor>> classMethodDescriptorMap = new HashMap<>();

    private Map<String, Method> reflectedMethodMap = new HashMap<>();

    @Value("${dingding.rpc.server.host}")
    private String host;

    @Value("${dingding.rpc.server.port}")
    private Integer port;

    @PostConstruct
    public void initialize() {
        new Thread(this::connect).start();
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

                        }
                    });

            ChannelFuture cf = bootstrap.connect(host, port).addListener(future -> {
                if (future.isSuccess()) {
                    ChannelFuture channelFuture = (ChannelFuture) future;
                    this.channel = channelFuture.channel();
                } else {
                    System.out.println("Failed to connect server");
                    reconnect();
                }
            });

            cf.channel().closeFuture().sync();
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new RuntimeException();
        }finally {
            workerGroup.shutdownGracefully();
        }
    }

    public void reconnect() {

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
}
