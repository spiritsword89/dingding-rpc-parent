package com.dingding.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(DingdingRpcServerConfiguration.class)
public @interface EnableDingdingRpcServer {
}
