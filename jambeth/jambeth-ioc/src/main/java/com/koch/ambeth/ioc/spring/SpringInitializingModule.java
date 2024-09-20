package com.koch.ambeth.ioc.spring;

import com.koch.ambeth.ioc.IInitializingModule;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

@RequiredArgsConstructor
public class SpringInitializingModule implements BeanFactoryPostProcessor, BeanDefinitionRegistryPostProcessor {

    @NonNull
    final Object module;

    @Getter
    Runnable moduleFinalizer;

    @SneakyThrows
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanFactory) throws BeansException {
        if (module instanceof IInitializingModule) {
            moduleFinalizer = SpringBeanContextFactory.processModuleInSpring(beanFactory, (IInitializingModule) module);
        } else {
            var currModule = ((Class<? extends IInitializingModule>) module).getConstructor().newInstance();
            moduleFinalizer = SpringBeanContextFactory.processModuleInSpring(beanFactory, currModule);
        }
    }

    @SneakyThrows
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (module instanceof IInitializingModule) {
            moduleFinalizer = SpringBeanContextFactory.processModuleInSpring(beanFactory, (IInitializingModule) module);
        } else {
            var currModule = ((Class<? extends IInitializingModule>) module).getConstructor().newInstance();
            moduleFinalizer = SpringBeanContextFactory.processModuleInSpring(beanFactory, currModule);
        }
    }
}
