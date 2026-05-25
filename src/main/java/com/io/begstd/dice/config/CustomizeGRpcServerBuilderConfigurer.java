package com.io.begstd.dice.config;


import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.grpc.ServerBuilder;
import org.lognet.springboot.grpc.GRpcServerBuilderConfigurer;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CustomizeGRpcServerBuilderConfigurer extends GRpcServerBuilderConfigurer {

    @Value("${grpc.pool.core:4}")
    private int corePoolSize;
    @Value("${grpc.pool.maximum:16}")
    private int maxPoolSize;
    @Value("${grpc.pool.keepAlive:60}")
    private int keepAliveTime;
    @Value("${grpc.pool.queue:5000}")
    private int messageQueue;

    @Override
    public void configure(ServerBuilder<?> serverBuilder) {
        serverBuilder.executor(new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue<>(messageQueue), new ThreadFactoryBuilder().setNameFormat("customizeGrpcThread-%d").build()));
    }
}
