package com.dingding.config;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AutoRemoteInjection {
    String requestClientId() default "";
    Class<?> fallbackClass() default Void.class;
}
