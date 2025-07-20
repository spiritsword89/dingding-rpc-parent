package com.dingding.handler;

import com.dingding.client.RpcClient;
import com.dingding.model.MessagePayload;
import com.dingding.model.MessageType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcClientMessageHandler extends SimpleChannelInboundHandler<MessagePayload> {
    private static final Logger logger = LoggerFactory.getLogger(RpcClientMessageHandler.class);

    private RpcClient rpcClient;

    public RpcClientMessageHandler(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MessagePayload messagePayload) {
        try {
            if(messagePayload.getMessageType().equals(MessageType.FORWARD)) {
                //消息转发请求
                //请求调用
                processRequestAndGenerateResponse(messagePayload);
            } else if (messagePayload.getMessageType().equals(MessageType.RESPONSE)) {
                //请求返回
                completeRequest(messagePayload);
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
    }

    private void completeRequest(MessagePayload messagePayload) {
        //调用CompletableFuture complete
        MessagePayload.RpcResponse response = (MessagePayload.RpcResponse) messagePayload.getPayload();
        rpcClient.completeRequest(response);
    }

    private void processRequestAndGenerateResponse(MessagePayload messagePayload) {

    }
}
