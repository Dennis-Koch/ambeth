package com.koch.ambeth.ioc.spring;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.List;

@RequiredArgsConstructor
public class SpringInitializingModuleFinalizer implements BeanFactoryPostProcessor {

    @Setter
    List<SpringInitializingModule> springInitializingModules;

    @SneakyThrows
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (springInitializingModules != null) {
            for (var springInitializingModule : springInitializingModules) {
                springInitializingModule.getModuleFinalizer().run();
            }
        } else {
            var moduleNames = beanFactory.getBeanNamesForType(SpringInitializingModule.class);
            for (var moduleName : moduleNames) {
                var module = beanFactory.getBean(moduleName, SpringInitializingModule.class);
                module.getModuleFinalizer().run();
            }
        }
    }
}
