package com.dingding.handler;

import com.alibaba.fastjson.JSON;
import com.dingding.model.MessagePayload;
import com.dingding.model.MessageType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class JsonCallMessageEncoder extends MessageToByteEncoder<MessagePayload> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, MessagePayload messagePayload, ByteBuf byteBuf) throws Exception {
        // MessagePayload -> byte[] (100kb) -> ByteBuf -> Server
        //第一次30kb； 第二次70kb
        //第一次10kb，第二次50，第三次40kb
        //自定义一套规则

        //第1个字节： 消息种类
        byte type;
        if(messagePayload.getMessageType().equals(MessageType.REGISTER)) {
            type = 1;
        } else if (messagePayload.getMessageType().equals(MessageType.CALL)) {
            type = 2;
        }else if (messagePayload.getMessageType().equals(MessageType.FORWARD)) {
            type = 3;
        }else if(messagePayload.getMessageType().equals(MessageType.RESPONSE)){
            type = 4;
        }else {
            type = 5;
        }
        byteBuf.writeByte(type);
        //接下来4个字节 = 1 int = 存放消息主体的大小
        byte[] jsonBytes = JSON.toJSONBytes(messagePayload);
        byteBuf.writeInt(jsonBytes.length);
        //剩下的所有字节 = 消息主题本身
        byteBuf.writeBytes(jsonBytes);
    }
}
