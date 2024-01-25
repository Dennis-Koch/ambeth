package com.koch.ambeth.ioc.spring;

import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.config.IPropertyConfiguration;
import com.koch.ambeth.ioc.config.PropertiesPreProcessor;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.config.IProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.List;
import java.util.Set;

public class SpringPropertiesPreProcessor extends PropertiesPreProcessor implements BeanFactoryPostProcessor, BeanPostProcessor, InitializingBean {

    protected ConfigurableListableBeanFactory beanFactory;

    protected IProperties props;

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (this.beanFactory != null) {
            throw new IllegalStateException("This post processor was already used in another bean factory");
        }
        this.beanFactory = beanFactory;
        props = beanFactory.getBean(AmbethBootstrapSpringConfig.PROPERTIES_BEAN_NAME, IProperties.class);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        IBeanConfiguration beanConfiguration = null;
        var ignoredPropertyNames = beanConfiguration != null ? new HashSet<>(beanConfiguration.getIgnoredPropertyNames()) : Set.<String>of();
        var propertyConfigurations = beanConfiguration != null ? beanConfiguration.getPropertyConfigurations() : List.<IPropertyConfiguration>of();
        preProcessProperties(null, null, props, beanName, bean, bean.getClass(), propertyConfigurations, ignoredPropertyNames, null);
        return bean;
    }
}
