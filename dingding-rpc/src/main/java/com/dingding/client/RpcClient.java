package com.dingding.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

public class RpcClient {
    private static final Logger logger =  LoggerFactory.getLogger(RpcClient.class);

    private NioEventLoopGroup workerGroup = new NioEventLoopGroup();

    private Channel channel;

    @Value("${dingding.rpc.server.host}")
    private String host;

    @Value("${dingding.rpc.server.port}")
    private Integer port;

    @PostConstruct
    public void initialize() {
        new Thread(this::connect).start();
    }

    public void connect() {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap
                    .group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {

                        }
                    });

            ChannelFuture cf = bootstrap.connect(host, port).addListener(future -> {
                if (future.isSuccess()) {
                    ChannelFuture channelFuture = (ChannelFuture) future;
                    this.channel = channelFuture.channel();
                } else {
                    System.out.println("Failed to connect server");
                    reconnect();
                }
            });

            cf.channel().closeFuture().sync();
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new RuntimeException();
        }finally {
            workerGroup.shutdownGracefully();
        }
    }

    public void reconnect() {

    }
}
