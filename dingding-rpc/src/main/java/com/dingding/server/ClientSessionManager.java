package com.dingding.server;

import com.dingding.model.MessagePayload;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientSessionManager {
    private static final Map<String, Channel> registeredClients = new ConcurrentHashMap<>();
    private static final Map<String, MessagePayload> requestMap = new ConcurrentHashMap<>();

    public static void register(String clientId, Channel channel) {
        registeredClients.put(clientId, channel);
    }

    public static boolean isClientRegistered(String clientId) {
        return registeredClients.containsKey(clientId);
    }

    public static Channel getClientChannel(String clientId) {
        return registeredClients.get(clientId);
    }

    public static void signOut(String clientId) {
        registeredClients.remove(clientId);
    }

    public static void putRequest(MessagePayload messagePayload) {
        MessagePayload.RpcRequest rpcRequest = (MessagePayload.RpcRequest) messagePayload.getPayload();
        requestMap.put(rpcRequest.getRequestId(), messagePayload);
    }

    public static MessagePayload getRequest(String requestId) {
        return requestMap.get(requestId);
    }

    public static void deleteRequest(String requestId) {
        requestMap.remove(requestId);
    }
}
