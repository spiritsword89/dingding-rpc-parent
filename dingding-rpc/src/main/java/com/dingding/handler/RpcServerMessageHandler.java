package com.dingding.handler;

import com.dingding.exceptions.ChannelNotRegisterException;
import com.dingding.exceptions.RemoteCallException;
import com.dingding.model.MessagePayload;
import com.dingding.model.MessageType;
import com.dingding.server.ClientSessionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class RpcServerMessageHandler extends SimpleChannelInboundHandler<MessagePayload> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessagePayload messagePayload) throws Exception {
        MessageType messageType = messagePayload.getMessageType();

        if(messageType.equals(MessageType.REGISTER)) {
            //把客户端的信息记录下来
            registerClientIntoSession(messagePayload, ctx.channel());
        }

        if(messageType.equals(MessageType.CALL)) {
            //需要把RpcRequest请求调用信息转发给对应的服务
            MessagePayload.RpcRequest rpcRequest = (MessagePayload.RpcRequest) messagePayload.getPayload();
            if(rpcRequest.getRequestClientId().equals(messagePayload.getClientId())) {
                throw new RemoteCallException("Client Id and the request client id cannot be the same.");
            }
            Channel channel = ClientSessionManager.getClientChannel(rpcRequest.getRequestClientId());

            if(channel == null) {
                throw new ChannelNotRegisterException(rpcRequest.getRequestClientId());
            }

            forwardRequestToClient(messagePayload, channel);
        }

        if(messageType.equals(MessageType.RESPONSE)) {
            //结果原路返回
            returnRequestToClient(messagePayload);
        }
    }

    private void returnRequestToClient(MessagePayload messagePayload) {
        MessagePayload.RpcResponse response = (MessagePayload.RpcResponse) messagePayload.getPayload();
        String requestId = response.getRequestId();
        MessagePayload request = ClientSessionManager.getRequest(requestId);

        //Client Id: 请求方的Client Id
        String clientId = request.getClientId();

        //Channel: 请求方的Channel
        Channel channel = ClientSessionManager.getClientChannel(clientId);
        channel.writeAndFlush(response);
    }

    private void forwardRequestToClient(MessagePayload messagePayload, Channel channel) {
        ClientSessionManager.putRequest(messagePayload);
        messagePayload.setMessageType(MessageType.FORWARD);
        channel.writeAndFlush(messagePayload);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    private void registerClientIntoSession(MessagePayload messagePayload, Channel channel) {
        ClientSessionManager.register(messagePayload.getClientId(), channel);
    }
}
