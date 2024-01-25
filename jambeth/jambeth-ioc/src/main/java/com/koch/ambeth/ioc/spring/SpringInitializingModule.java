package com.koch.ambeth.ioc.spring;

import com.koch.ambeth.ioc.IInitializingModule;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

@RequiredArgsConstructor
public class SpringInitializingModule implements BeanFactoryPostProcessor {

    @NonNull
    final Object module;

    @Getter
    Runnable moduleFinalizer;

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
