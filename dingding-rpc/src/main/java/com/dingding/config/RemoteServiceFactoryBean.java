package com.dingding.config;

import com.dingding.client.RemoteClient;
import com.dingding.client.RemoteService;
import com.dingding.client.RpcClient;
import com.dingding.model.MessagePayload;
import com.dingding.model.MessageType;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class RemoteServiceFactoryBean<T> implements FactoryBean<T> {

    private String requestClientId;

    private Class<T> rpcInterfaceClass;

    private Class<? extends T> fallbackClass;

    private RemoteClient remoteClient;

    public Class<? extends T> getFallbackClass() {
        return fallbackClass;
    }

    public void setFallbackClass(Class<? extends T> fallbackClass) {
        this.fallbackClass = fallbackClass;
    }

    public RemoteServiceFactoryBean(Class<T> rpcInterfaceClass) {
        this.rpcInterfaceClass = rpcInterfaceClass;
    }

    public String getRequestClientId() {
        return requestClientId;
    }

    public void setRequestClientId(String requestClientId) {
        this.requestClientId = requestClientId;
    }

    public Class<T> getRpcInterfaceClass() {
        return rpcInterfaceClass;
    }

    public void setRpcInterfaceClass(Class<T> rpcInterfaceClass) {
        this.rpcInterfaceClass = rpcInterfaceClass;
    }

    public RemoteClient getRemoteClient() {
        return remoteClient;
    }

    public void setRemoteClient(RemoteClient remoteClient) {
        this.remoteClient = remoteClient;
    }

    @Override
    @SuppressWarnings("all")
    public T getObject() throws Exception {
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{rpcInterfaceClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                int paramCounts = args.length;
                String[] paramTypes = new String[paramCounts];

                Class<?>[] parameterTypes = method.getParameterTypes();
                for(int i = 0; i < parameterTypes.length; i++) {
                    paramTypes[i] = parameterTypes[i].getSimpleName();
                }

                String requestId = UUID.randomUUID().toString();

                Class<?> remoteRpcInterface = getRemoteRpcInterface(rpcInterfaceClass);

                if(remoteRpcInterface == null) {
                    return triggerCallback(method, args);
                }

                MessagePayload message = new MessagePayload.MessageBuilder()
                        .setClientId(remoteClient.clientId())
                        .setRequestClientId(requestClientId)
                        .setRequestId(requestId)
                        .setMessageType(MessageType.CALL)
                        .setParamTypes(paramTypes)
                        .setParams(args)
                        .setRequestMethodName(method.getName())
                        .setReturnValueType(method.getReturnType().getSimpleName())
                        .setRequestedClassName(remoteRpcInterface.getName()).build();

                CompletableFuture<MessagePayload.RpcResponse> future = new CompletableFuture<>();

                remoteClient.sendRequest(message, requestId, future);

                try {
                    MessagePayload.RpcResponse rpcResponse = future.get(5, TimeUnit.SECONDS);
                    remoteClient.didCatchResponse(rpcResponse, requestId);
                    return rpcResponse.getReturnValue();
                }catch (Exception e){
                    return triggerCallback(method, args);
                }
            }
        });
    }

    private Class<?> getRemoteRpcInterface(Class<?> targetInterface) {
        for(Class<?> interfaceClass : targetInterface.getInterfaces()) {
            if(interfaceClass.equals(RemoteService.class)) {
                return targetInterface;
            }
            Class<?> found = getRemoteRpcInterface(interfaceClass);
            if(found != null) {
                return found;
            }
        }

        return null;
    }

    private Object triggerCallback(Method method, Object[] args) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if(fallbackClass != null){
            Object fallbackBean = fallbackClass.getConstructor().newInstance();
            return fallbackClass.getMethod(method.getName(), method.getParameterTypes()).invoke(fallbackBean, args);
        }

        return null;
    }

    @Override
    public Class<?> getObjectType() {
        return rpcInterfaceClass;
    }
}
