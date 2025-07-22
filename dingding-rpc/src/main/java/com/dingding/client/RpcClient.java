package com.dingding.client;

import com.dingding.exceptions.RequestClassNotDetermineException;
import com.dingding.handler.ClientHeartbeatHandler;
import com.dingding.handler.JsonCallMessageEncoder;
import com.dingding.handler.JsonMessageDecoder;
import com.dingding.handler.RpcClientMessageHandler;
import com.dingding.model.MarkAsRpc;
import com.dingding.model.MessagePayload;
import com.dingding.model.MessageType;
import com.dingding.model.RpcMethodDescriptor;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


// 自定义注解 放在其他服务上， SpringbootApplication
// 自定义注解，clientId, basePackages

// 处理RPC请求
// 1. 发现RPC服务
// 例如：User服务，自定义逻辑允许某些特定方法接收RPC请求
// 扫描支持RPC的所有方法

// 创建并发送RPC请求

public class RpcClient extends RemoteClientTemplate {
    private static final Logger logger =  LoggerFactory.getLogger(RpcClient.class);

    private NioEventLoopGroup workerGroup = new NioEventLoopGroup();
    private Channel channel;
    @Value("${dingding.rpc.server.host}")
    private String host;
    @Value("${dingding.rpc.server.port}")
    private Integer port;
    private int retryCount = 0;
    private int maxRetryCount;

    @PostConstruct
    public void initialize() {
        System.out.println("Rpc Client 启动");
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
                            socketChannel.pipeline().addLast(new JsonCallMessageEncoder());
                            socketChannel.pipeline().addLast(new JsonMessageDecoder());
                            socketChannel.pipeline().addLast(new IdleStateHandler(0, 5, 0));
                            socketChannel.pipeline().addLast(new ClientHeartbeatHandler());
                            //假设已经有了Handler接收返回的结果
                            //1. 处理返回结果
                            //2. 处理接收到的请求
                            socketChannel.pipeline().addLast(new RpcClientMessageHandler(RpcClient.this));
                        }
                    });

            bootstrap.connect(host, port).addListener(future -> {
                if (future.isSuccess()) {
                    logger.info("Client ID: {} 连接成功", clientId());
                    ChannelFuture channelFuture = (ChannelFuture) future;
                    this.channel = channelFuture.channel();
                    sendRegistrationRequest();

                    channelFuture.channel().closeFuture().sync();
                } else {
                    System.out.println("Failed to connect server");
                    reconnect();
                }
            });
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new RuntimeException();
        }
    }

    public void reconnect() {
        workerGroup.schedule(this::connect, 5, TimeUnit.SECONDS);
    }

    public void sendRegistrationRequest() {
        MessagePayload msg = new MessagePayload.MessageBuilder().setClientId(clientId()).setMessageType(MessageType.REGISTER).build();
        this.channel.writeAndFlush(msg);
    }

    public void sendRequest(Object request, String requestId, CompletableFuture<MessagePayload.RpcResponse> future) {
        super.sendRequest(request, requestId, future);
        channel.writeAndFlush(request); //发送RPC请求到服务端
    }

    @Override
    public void sendResponse(Object message) {
        channel.writeAndFlush(message);
    }
}
