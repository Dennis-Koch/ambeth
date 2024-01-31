package com.koch.ambeth.ioc.spring;

import com.koch.ambeth.ioc.IBeanRuntime;
import com.koch.ambeth.ioc.ServiceContext;
import com.koch.ambeth.ioc.config.BeanConfiguration;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.BeanContextFactory;
import com.koch.ambeth.ioc.factory.IBeanContextFactoryIntern;
import com.koch.ambeth.ioc.factory.IBeanContextInitializer;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.proxy.IProxyFactory;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

public class SpringBeanRuntime<V> implements IBeanRuntime<V> {

    final SpringServiceContext serviceContext;

    Class<? extends V> beanType;

    V beanInstance;

    final boolean joinLifecycle;

    BeanConfiguration beanConfiguration;

    public SpringBeanRuntime(SpringServiceContext serviceContext, Class<? extends V> beanType, boolean joinLifecycle) {
        this.serviceContext = serviceContext;
        this.beanType = beanType;
        this.joinLifecycle = joinLifecycle;
        beanConfiguration = createBeanConfiguration(beanType);
    }

    public SpringBeanRuntime(SpringServiceContext serviceContext, V beanInstance, boolean joinLifecycle) {
        this.serviceContext = serviceContext;
        this.beanInstance = beanInstance;
        this.joinLifecycle = joinLifecycle;
        beanConfiguration = createBeanConfiguration(beanInstance.getClass());
    }

    protected BeanConfiguration createBeanConfiguration(Class<?> beanType) {
        var proxyFactory = serviceContext.getService(IProxyFactory.class, false);
        var props = serviceContext.getService(IProperties.class, true);
        return new BeanConfiguration(beanType, null, proxyFactory, props);
    }

    @Override
    public V finish() {
        var beanContextFactory = serviceContext.getBeanContextFactory();

        var beanContextInitializer = beanContextFactory.getBeanContextInitializer();
        var beanConfHierarchy = beanContextInitializer.fillParentHierarchyIfValid(serviceContext, beanContextFactory, beanConfiguration);

        var bean = getInstanceInternal(beanContextFactory, beanContextInitializer, beanConfHierarchy);
        bean = (V) beanContextInitializer.initializeBean(serviceContext, beanContextFactory, beanConfiguration, bean, beanConfHierarchy, joinLifecycle);
        return bean;
    }

    @Override
    public IBeanRuntime<V> parent(String parentBeanTemplateName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IBeanRuntime<V> propertyRef(String propertyName, String beanName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IBeanRuntime<V> propertyRefFromContext(String propertyName, String fromContext, String beanName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IBeanRuntime<V> propertyRef(String propertyName, IBeanConfiguration bean) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IBeanRuntime<V> propertyRefs(String beanName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IBeanRuntime<V> propertyRefs(String... beanNames) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IBeanRuntime<V> propertyRef(IBeanConfiguration bean) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IBeanRuntime<V> propertyValue(String propertyName, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IBeanRuntime<V> ignoreProperties(String propertyName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IBeanRuntime<V> ignoreProperties(String... propertyNames) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V getInstance() {
        if (beanInstance != null) {
            return beanInstance;
        }
        var beanContextFactory = serviceContext.getBeanContextFactory();
        var beanContextInitializer = beanContextFactory.getBeanContextInitializer();
        var beanConfHierarchy = beanContextInitializer.fillParentHierarchyIfValid(serviceContext, beanContextFactory, beanConfiguration);
        beanInstance = getInstanceInternal(beanContextFactory, beanContextInitializer, beanConfHierarchy);
        return beanInstance;
    }

    protected V getInstanceInternal(IBeanContextFactoryIntern beanContextFactory, IBeanContextInitializer beanContextInitializer, List<IBeanConfiguration> beanConfHierarchy) {
        if (beanInstance != null) {
            return beanInstance;
        }
        Class<?> beanType = this.beanType;
        if (beanType == null) {
            beanType = beanContextInitializer.resolveTypeInHierarchy(beanConfHierarchy);
        }
        beanInstance = (V) beanContextInitializer.instantiateBean(serviceContext, beanContextFactory, beanConfiguration, beanType, beanConfHierarchy);
        return beanInstance;
    }
}
