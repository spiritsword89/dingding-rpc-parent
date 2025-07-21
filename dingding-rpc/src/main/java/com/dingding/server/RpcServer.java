package com.dingding.server;

import com.dingding.handler.JsonCallMessageEncoder;
import com.dingding.handler.JsonMessageDecoder;
import com.dingding.handler.RpcServerMessageHandler;
import com.dingding.handler.ServerHeartbeatHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

public class RpcServer {
    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);

    @Value("${dingding.rpc.server.port}")
    private int port;
    @Value("${dingding.rpc.server.worker}")
    private int workerGroupSize;
    @Value("${dingding.rpc.server.backlog}")
    private int backlogSize;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getWorkerGroupSize() {
        return workerGroupSize;
    }

    public void setWorkerGroupSize(int workerGroupSize) {
        this.workerGroupSize = workerGroupSize;
    }

    public int getBacklogSize() {
        return backlogSize;
    }

    public void setBacklogSize(int backlogSize) {
        this.backlogSize = backlogSize;
    }

    public void startServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                start();
            }
        }).start();
    }

    private void start() {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup(workerGroupSize);

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss,worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, backlogSize)
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new JsonMessageDecoder());
                            socketChannel.pipeline().addLast(new JsonCallMessageEncoder());
                            socketChannel.pipeline().addLast(new IdleStateHandler(10, 0, 10));
                            socketChannel.pipeline().addLast(new ServerHeartbeatHandler());
                            socketChannel.pipeline().addLast(new RpcServerMessageHandler());
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            System.out.println("Server started on port 11111");
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error("Server start error",e);
            throw new RuntimeException();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
