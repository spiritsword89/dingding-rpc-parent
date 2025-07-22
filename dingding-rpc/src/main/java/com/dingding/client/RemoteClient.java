package com.dingding.client;

import com.dingding.model.MessagePayload;

import java.util.concurrent.CompletableFuture;

public interface RemoteClient {
    public void sendRequest(Object message, String requestId, CompletableFuture<MessagePayload.RpcResponse> future);
    public void sendResponse(Object message);
    public void didCatchResponse(Object message, String requestId);
    public String clientId();
}
