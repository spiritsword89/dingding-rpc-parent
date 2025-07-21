package com.dingding.handler;
import com.dingding.model.MessagePayload;
import com.dingding.model.MessageType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class ClientHeartbeatHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent event){
            if(event.state() == IdleState.WRITER_IDLE){
                System.out.println("客户端心跳机制");
                MessagePayload message = new MessagePayload.MessageBuilder().setMessageType(MessageType.HEART_BEAT).build();
                ctx.writeAndFlush(message);
            }
        }else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
