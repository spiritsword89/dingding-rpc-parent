package com.dingding.config;

import com.dingding.client.RpcClient;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(RpcClientRegistrar.class)
public @interface EnableDingdingRpcClient {
    String clientId() default "";
    String[] packages() default {};
}
