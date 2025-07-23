package com.dingding.config;

import com.dingding.client.RpcClient;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class RemoteServiceScanner extends ClassPathBeanDefinitionScanner {
    public RemoteServiceScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters) {
        super(registry, useDefaultFilters);
        addIncludeFilter(new TypeFilter() {
            @Override
            public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
                ClassMetadata classMetadata = metadataReader.getClassMetadata();
                AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();

                return classMetadata.isInterface() && annotationMetadata.hasAnnotation(AutoRemoteInjection.class.getName());
            }
        });
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return true;
    }

    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitionHolders = super.doScan(basePackages);

        for(BeanDefinitionHolder holder : beanDefinitionHolders){
            BeanDefinition beanDefinition = holder.getBeanDefinition();
            if (beanDefinition instanceof ScannedGenericBeanDefinition scannedBeanDefinition) {
                AnnotationMetadata metadata = scannedBeanDefinition.getMetadata();
                Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(AutoRemoteInjection.class.getName());

                String requestClientId = annotationAttributes.get("requestClientId").toString();
                Class<?> fallbackClass = (Class<?>)annotationAttributes.get("fallbackClass");

                String beanClassName = scannedBeanDefinition.getBeanClassName();

                scannedBeanDefinition.setBeanClass(RemoteServiceFactoryBean.class);
                scannedBeanDefinition.getConstructorArgumentValues().addGenericArgumentValue(beanClassName);
                scannedBeanDefinition.getPropertyValues().addPropertyValue("requestClientId", requestClientId);
                scannedBeanDefinition.getPropertyValues().addPropertyValue("remoteClient", new RuntimeBeanReference(RpcClient.class));

                if(fallbackClass != Void.class) {
                    scannedBeanDefinition.getPropertyValues().addPropertyValue("fallbackClass", fallbackClass);
                }
            }
        }

        return  beanDefinitionHolders;
    }
}
