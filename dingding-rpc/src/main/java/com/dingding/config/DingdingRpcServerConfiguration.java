package com.dingding.config;

import com.dingding.server.RpcServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DingdingRpcServerConfiguration {

    @Value("${dingding.rpc.server.port}")
    private int port;

    @Value("${dingding.rpc.server.backlog}")
    private int backlogSize;

    @Value("${dingding.rpc.server.worker}")
    private int workerGroupSize;

    @Bean
    public RpcServer rpcServer() {
        RpcServer rpcServer = new RpcServer();
        rpcServer.setPort(port);
        rpcServer.setBacklogSize(backlogSize);
        rpcServer.setWorkerGroupSize(workerGroupSize);
        rpcServer.startServer();

        return rpcServer;
    }
}
