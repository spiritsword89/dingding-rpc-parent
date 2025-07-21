package com.dingding.server;

import com.dingding.model.MessagePayload;
import io.netty.channel.Channel;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClientSessionManager {

    //Key: clientId
    private static final Map<String, Channel> registeredClients = new ConcurrentHashMap<>();

    //Key: requestId
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

    public static void clearByClientId(String clientId) {
        registeredClients.remove(clientId);
        Set<Map.Entry<String, MessagePayload>> entries = requestMap.entrySet();

        Iterator<Map.Entry<String, MessagePayload>> iterator = entries.iterator();

        while(iterator.hasNext()) {
            Map.Entry<String, MessagePayload> next = iterator.next();
            MessagePayload messagePayload = next.getValue();

            if(messagePayload.getClientId().equals(clientId)) {
                iterator.remove();
            }
        }
    }
}
