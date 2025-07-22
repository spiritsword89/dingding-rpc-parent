package com.dingding.handler;

import com.dingding.exceptions.ChannelNotRegisterException;
import com.dingding.exceptions.RemoteCallException;
import com.dingding.model.MessagePayload;
import com.dingding.model.MessageType;
import com.dingding.server.ClientSessionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcServerMessageHandler extends SimpleChannelInboundHandler<MessagePayload> {
    private static final Logger logger =  LoggerFactory.getLogger(RpcServerMessageHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessagePayload messagePayload) throws Exception {
        MessageType messageType = messagePayload.getMessageType();

        Thread.sleep(8000);
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
        channel.writeAndFlush(messagePayload);
    }

    private void forwardRequestToClient(MessagePayload messagePayload, Channel channel) {
        ClientSessionManager.putRequest(messagePayload);
        messagePayload.setMessageType(MessageType.FORWARD);
        channel.writeAndFlush(messagePayload);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String clientId = ctx.channel().attr(AttributeKey.valueOf("clientId")).get().toString();
        ClientSessionManager.clearByClientId(clientId);
    }

    private void registerClientIntoSession(MessagePayload messagePayload, Channel channel) {
        logger.info("Client id {} is now registering with Netty Server",  messagePayload.getClientId());
        ClientSessionManager.register(messagePayload.getClientId(), channel);
        channel.attr(AttributeKey.valueOf("clientId")).set(messagePayload.getClientId());
    }
}
