package com.dingding.handler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.dingding.model.MessagePayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;

public class JsonMessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if(byteBuf.readableBytes() < 5) {
            return;
        }

        byteBuf.markReaderIndex();
        byte messageType = byteBuf.readByte();
        int length = byteBuf.readInt();

        if(byteBuf.readableBytes() < length) {
            byteBuf.resetReaderIndex();
            return;
        }

        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);

        MessagePayload messagePayload = JSON.parseObject(bytes, MessagePayload.class);

        switch(messageType) {
            case 1,5:
                messagePayload.setPayload(null);
                break;
            case 2,3:
                JSONObject payload = (JSONObject) messagePayload.getPayload();
                MessagePayload.RpcRequest rpcRequest = payload.toJavaObject(MessagePayload.RpcRequest.class);
                messagePayload.setPayload(rpcRequest);
                break;
            case 4:
                JSONObject response = (JSONObject) messagePayload.getPayload();
                MessagePayload.RpcResponse rpcResponse = response.toJavaObject(MessagePayload.RpcResponse.class);
                messagePayload.setPayload(rpcResponse);
                break;
        }

        list.add(messagePayload);
    }
}
