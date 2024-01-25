package com.koch.ambeth.ioc.spring;

import com.koch.ambeth.ioc.log.LoggerInstancePreProcessor;
import com.koch.ambeth.util.config.IProperties;
import lombok.Setter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Set;

public class SpringLoggerInstancePreProcessor extends LoggerInstancePreProcessor implements BeanPostProcessor, BeanFactoryAware, ApplicationContextAware {
    @Setter
    protected BeanFactory beanFactory;

    @Setter
    protected ApplicationContext applicationContext;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        var props = applicationContext.getBean(IProperties.class);
        try {
            var beanDefinition = ((BeanDefinitionRegistry) applicationContext.getAutowireCapableBeanFactory()).getBeanDefinition(beanName);
            if (beanDefinition instanceof GenericBeanDefinition genBeanDef) {
                var beanType = genBeanDef.getBeanClass();
                preProcessPropertiesIntern(props, bean, beanType, bean.getClass(), Set.of());
            }
            return bean;
        } catch (NoSuchBeanDefinitionException e) {
            throw e;
        }
    }
}
