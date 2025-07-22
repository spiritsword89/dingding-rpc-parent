package com.dingding.config;

import com.dingding.client.RpcClient;
import com.dingding.model.MessagePayload;
import com.dingding.model.MessageType;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class RemoteServiceFactoryBean<T> implements FactoryBean<T> {

    private String requestClientId;

    private Class<T> rpcInterfaceClass;

    private RpcClient rpcClient;

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

    public RpcClient getRpcClient() {
        return rpcClient;
    }

    public void setRpcClient(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
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

                MessagePayload message = new MessagePayload.MessageBuilder()
                        .setClientId(rpcClient.getClientId())
                        .setRequestClientId(requestClientId)
                        .setRequestId(requestId)
                        .setMessageType(MessageType.CALL)
                        .setParamTypes(paramTypes)
                        .setParams(args)
                        .setRequestMethodName(method.getName())
                        .setReturnValueType(method.getReturnType().getSimpleName())
                        .setRequestedClassName(rpcInterfaceClass.getName()).build();

                CompletableFuture<MessagePayload.RpcResponse> future = new CompletableFuture<>();

                rpcClient.sendRequest(message, requestId, future);

                try {
                    MessagePayload.RpcResponse rpcResponse = future.get(5, TimeUnit.SECONDS);
                    return rpcResponse.getReturnValue();
                }catch (Exception e){
                    return "超时！";
                }
            }
        });
    }

    @Override
    public Class<?> getObjectType() {
        return rpcInterfaceClass;
    }
}
