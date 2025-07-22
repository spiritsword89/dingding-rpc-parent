package com.dingding.client;

import com.dingding.exceptions.RequestClassNotDetermineException;
import com.dingding.model.MarkAsRpc;
import com.dingding.model.MessagePayload;
import com.dingding.model.MessageType;
import com.dingding.model.RpcMethodDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public abstract class RemoteClientTemplate implements RemoteClient, SmartInitializingSingleton, ApplicationContextAware {
    private static final Logger logger =  LoggerFactory.getLogger(RemoteClientTemplate.class);
    private String clientId;
    private String[] scanPackages; //扫描的包， 里面都是暴漏出去的RPC服务
    private ApplicationContext applicationContext;
    private Map<String, Map<String, RpcMethodDescriptor>> classMethodDescriptorMap = new HashMap<>();
    private Map<String, Method> reflectedMethodMap = new HashMap<>();
    private Map<String, CompletableFuture<MessagePayload.RpcResponse>> requestMap = new ConcurrentHashMap<>();

    @Override
    public String clientId() {
        return this.clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setScanPackages(String[] scanPackages) {
        this.scanPackages = scanPackages;
    }

    public String[] getScanPackages() {
        return scanPackages;
    }

    @Override
    public void sendRequest(Object message, String requestId, CompletableFuture<MessagePayload.RpcResponse> future) {
        this.requestMap.put(requestId, future);
    }

    public void completeRequest(MessagePayload.RpcResponse rpcResponse) {
        String requestId = rpcResponse.getRequestId();
        CompletableFuture<MessagePayload.RpcResponse> rpcResponseCompletableFuture = requestMap.get(requestId);
        rpcResponseCompletableFuture.complete(rpcResponse);
    }

    @Override
    public void didCatchResponse(Object response, String requestId) {
        this.requestMap.remove(requestId);
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
                MessagePayload message = new MessagePayload.MessageBuilder()
                        .setRequestId(request.getRequestId())
                        .setMessageType(MessageType.RESPONSE)
                        .setReturnValue(result).build();

                sendResponse(message);

            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.error(e.getMessage(),e);
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
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
}
