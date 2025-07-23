package com.dingding.config;

import org.springframework.beans.factory.support.*;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;


public class RpcServiceScanPostProcessor implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry, BeanNameGenerator importBeanNameGenerator) {
        String applicationClassName = importingClassMetadata.getClassName();
        String basePackage = null;
        try {
            Class<?> applicationClass = Class.forName(applicationClassName);
            basePackage = applicationClass.getPackage().getName();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        RemoteServiceScanner remoteServiceScanner = new RemoteServiceScanner(registry, false);
        remoteServiceScanner.doScan(new String[]{basePackage});
    }
}
