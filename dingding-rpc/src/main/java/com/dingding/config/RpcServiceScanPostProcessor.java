package com.dingding.config;

import com.dingding.client.RemoteService;
import com.dingding.client.RpcClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class RpcServiceScanPostProcessor implements BeanDefinitionRegistryPostProcessor {
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

        Set<RemoteServiceFieldHolder> targetFields = new HashSet<>();

        String[] beanDefinitionNames = registry.getBeanDefinitionNames();

        //扫描出所有Bean，然后找出哪些Bean内部维护了需要调用RPC服务的接口
        for(String beanDefinitionName: beanDefinitionNames){
            String beanClassName = registry.getBeanDefinition(beanDefinitionName).getBeanClassName();

            if(beanClassName != null) {
                try {
                    Class<?> clazz = Class.forName(beanClassName);
                    Field[] declaredFields = clazz.getDeclaredFields();

                    for(Field declaredField: declaredFields){
                        declaredField.setAccessible(true);
                        if(declaredField.isAnnotationPresent(AutoRemoteInjection.class)) {
                            AutoRemoteInjection annotation = declaredField.getAnnotation(AutoRemoteInjection.class);
                            RemoteServiceFieldHolder remoteServiceFieldHolder = new RemoteServiceFieldHolder(declaredField, annotation.requestClientId());
                            targetFields.add(remoteServiceFieldHolder);
                        }
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        for(RemoteServiceFieldHolder fieldHolder: targetFields){
            Class<?> targetClass = fieldHolder.getRemoteServiceField().getType();

            if(RemoteService.class.isAssignableFrom(targetClass)){
                //怎么生成代理对象？
                //1.创建一个属于TargetClass类型的RemoteServiceFactoryBean BeanDefinition
                AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(RemoteServiceFactoryBean.class).getBeanDefinition();
                //传入构造方法参数种类的名字
                beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(targetClass.getName());
                beanDefinition.getPropertyValues().addPropertyValue("requestClientId", fieldHolder.getRequestClientId());

                //从Spring容器中找出类型为RpcClient.class的bean，然后注入
                beanDefinition.getPropertyValues().addPropertyValue("rpcClient", new RuntimeBeanReference(RpcClient.class));

                registry.registerBeanDefinition(fieldHolder.getAlias(), beanDefinition);
            }
        }
    }
}
