package com.dingding.config;

import com.dingding.client.RpcClient;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

public class RpcClientRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry, BeanNameGenerator importBeanNameGenerator) {
        Map<String, Object> annotationAttributes = importingClassMetadata.getAnnotationAttributes(EnableDingdingRpcClient.class.getName());

        if(annotationAttributes != null){
            String clientId = (String)annotationAttributes.get("clientId");
            String[] packages = (String[]) annotationAttributes.get("packages");

            if(clientId != null && packages != null && packages.length > 0) {
                //注册RpcClient为BeanDefinition
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(RpcClient.class);
                builder.addPropertyValue("clientId", clientId);
                builder.addPropertyValue("scanPackages", packages);
                builder.setScope(GenericBeanDefinition.SCOPE_SINGLETON);
                builder.setLazyInit(false);

                registry.registerBeanDefinition("rpcClient", builder.getBeanDefinition());
            }
        }
    }
}
