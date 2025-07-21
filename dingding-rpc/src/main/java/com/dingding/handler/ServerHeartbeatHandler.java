package com.dingding.handler;

import com.dingding.server.ClientSessionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;

public class ServerHeartbeatHandler extends ChannelInboundHandlerAdapter {

    public ServerHeartbeatHandler() {

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent event) {
            if(event.state() == IdleState.READER_IDLE){
                Channel channel = ctx.channel();

                String clientId = channel.attr(AttributeKey.valueOf("clientId")).get().toString();

                ClientSessionManager.clearByClientId(clientId);

                channel.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
