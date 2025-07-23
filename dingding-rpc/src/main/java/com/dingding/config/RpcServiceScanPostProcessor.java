package com.dingding.config;

import com.dingding.client.RemoteService;
import com.dingding.client.RpcClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.*;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

//        for(BeanDefinitionHolder holder : beanDefinitionHolders){
//            registry.registerBeanDefinition(holder.getBeanName(), holder.getBeanDefinition());
//        }
//        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false) {
//            @Override
//            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
//                return true;
//            }
//        };
//        scanner.addIncludeFilter(new TypeFilter() {
//            @Override
//            public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
//                ClassMetadata classMetadata = metadataReader.getClassMetadata();
//                AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
//
//                return classMetadata.isInterface() &&
//                        annotationMetadata.hasAnnotation(AutoRemoteInjection.class.getName());
//            }
//        });
//
//        Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(basePackage);
//
//        System.out.println("candidateComponents:" + candidateComponents);
    }

    //    @Override
//    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
//
//
//
//        Set<RemoteServiceFieldHolder> targetFields = new HashSet<>();
//
//        String[] beanDefinitionNames = registry.getBeanDefinitionNames();
//
//        //扫描出所有Bean，然后找出哪些Bean内部维护了需要调用RPC服务的接口
//        for(String beanDefinitionName: beanDefinitionNames){
//            String beanClassName = registry.getBeanDefinition(beanDefinitionName).getBeanClassName();
//
//            if(beanClassName != null) {
//                try {
//                    Class<?> clazz = Class.forName(beanClassName);
//                    Field[] declaredFields = clazz.getDeclaredFields();
//
//                    for(Field declaredField: declaredFields){
//                        declaredField.setAccessible(true);
//                        if(declaredField.isAnnotationPresent(AutoRemoteInjection.class)) {
//                            AutoRemoteInjection annotation = declaredField.getAnnotation(AutoRemoteInjection.class);
//                            RemoteServiceFieldHolder remoteServiceFieldHolder = new RemoteServiceFieldHolder(declaredField, annotation.requestClientId());
//
//                            if(annotation.fallbackClass() != Void.class) {
//                                remoteServiceFieldHolder.setFallbackClass(annotation.fallbackClass());
//                            }
//
//                            targetFields.add(remoteServiceFieldHolder);
//                        }
//                    }
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        for(RemoteServiceFieldHolder fieldHolder: targetFields){
//            Class<?> targetClass = fieldHolder.getRemoteServiceField().getType();
//
//            if(RemoteService.class.isAssignableFrom(targetClass)){
//                //怎么生成代理对象？
//                //1.创建一个属于TargetClass类型的RemoteServiceFactoryBean BeanDefinition
//                AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(RemoteServiceFactoryBean.class).getBeanDefinition();
//                //传入构造方法参数种类的名字
//                beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(targetClass.getName());
//                beanDefinition.getPropertyValues().addPropertyValue("requestClientId", fieldHolder.getRequestClientId());
//
//                //从Spring容器中找出类型为RpcClient.class的bean，然后注入
//                beanDefinition.getPropertyValues().addPropertyValue("remoteClient", new RuntimeBeanReference(RpcClient.class));
//
//                if(fieldHolder.getFallbackClass() != null) {
//                    beanDefinition.getPropertyValues().addPropertyValue("fallbackClass", fieldHolder.getFallbackClass());
//                }
//
//                registry.registerBeanDefinition(fieldHolder.getAlias(), beanDefinition);
//            }
//        }
//    }
}
