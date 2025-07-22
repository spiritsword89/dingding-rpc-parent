package com.dingding.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({RpcClientRegistrar.class, RpcServiceScanPostProcessor.class})
public @interface EnableDingdingRpcClient {
    String clientId() default "";
    String[] packages() default {};
}
