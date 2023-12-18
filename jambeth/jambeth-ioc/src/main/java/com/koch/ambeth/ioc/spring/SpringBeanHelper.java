package com.koch.ambeth.ioc.spring;

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IFactoryBean;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.extendable.ExtendableBean;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public interface SpringBeanHelper {
    default BeanReference createBeanDefinition(Class<?> beanType, String propertyName, Object propertyValue) {
        return createBeanDefinition(beanType, (beanName, bean) -> {
            bean.getPropertyValues().add(propertyName, propertyValue);
        });
    }

    BeanDefinitionRegistry getBeanDefinitionRegistry();

    AtomicInteger getBeanSequence();

    default BeanReference createBeanDefinition(Class<?> beanType) {
        Objects.requireNonNull(beanType, "beanType must be valid");
        var id = getBeanSequence().incrementAndGet();
        return createBeanDefinition(beanType.getSimpleName() + "#" + id, beanType, null);
    }

    default BeanReference createBeanDefinition(String beanName, Class<?> beanType) {
        Objects.requireNonNull(beanName, "beanName must be valid");
        Objects.requireNonNull(beanType, "beanType must be valid");
        return createBeanDefinition(beanName, beanType, null);
    }

    default BeanReference createBeanDefinition(Class<?> beanType, BiConsumer<String, GenericBeanDefinition> beanConfigurer) {
        Objects.requireNonNull(beanType, "beanType must be valid");
        Objects.requireNonNull(beanConfigurer, "beanConfigurer must be valid");
        var id = getBeanSequence().incrementAndGet();
        return createBeanDefinition(beanType.getSimpleName() + "#" + id, beanType, beanConfigurer);
    }

    default BeanReference createBeanDefinition(String beanName, Class<?> beanType, BiConsumer<String, GenericBeanDefinition> beanConfigurer) {
        Objects.requireNonNull(beanName, "beanName must be valid");
        Objects.requireNonNull(beanType, "beanType must be valid");

        var bean = new GenericBeanDefinition();
        bean.setBeanClass(beanType);
        if (IInitializingBean.class.isAssignableFrom(beanType) && !InitializingBean.class.isAssignableFrom(beanType)) {
            bean.setInitMethodName("afterPropertiesSet");
        }
        if (IDisposableBean.class.isAssignableFrom(beanType) && !DisposableBean.class.isAssignableFrom(beanType)) {
            bean.setDestroyMethodName("destroy");
        }
        if (IFactoryBean.class.isAssignableFrom(beanType)) {
            var factoryBeanName = beanName + "##factory";
            if (beanConfigurer != null) {
                beanConfigurer.accept(beanName, bean);
            }
            getBeanDefinitionRegistry().registerBeanDefinition(factoryBeanName, bean);

            var targetBean = new GenericBeanDefinition();
            targetBean.setFactoryBeanName(factoryBeanName);
            targetBean.setFactoryMethodName("getObject");
            getBeanDefinitionRegistry().registerBeanDefinition(beanName, targetBean);
        } else {
            if (beanConfigurer != null) {
                beanConfigurer.accept(beanName, bean);
            }
            getBeanDefinitionRegistry().registerBeanDefinition(beanName, bean);
        }
        return new RuntimeBeanReference(beanName);
    }

    default BeanReference registerExtendableBean(String beanName, Class<?> providerType, Class<?> extendableType, ClassLoader classLoader) {
        if (beanName != null) {
            var bean = createBeanDefinition(beanName, ExtendableBean.class, (currBeanName, currBean) -> {
                currBean.getPropertyValues().add("providerType", providerType);
                currBean.getPropertyValues().add("extendableType", extendableType);
            });
            return bean;
        }
        var bean = createBeanDefinition(ExtendableBean.class, (currBeanName, currBean) -> {
            currBean.getPropertyValues().add("providerType", providerType);
            currBean.getPropertyValues().add("extendableType", extendableType);
        });
        return bean;
    }
}
