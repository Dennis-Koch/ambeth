package com.koch.ambeth.ioc.spring;

import com.koch.ambeth.ioc.IBeanInstantiationProcessor;
import com.koch.ambeth.ioc.IBeanPostProcessor;
import com.koch.ambeth.ioc.IBeanPreProcessor;
import com.koch.ambeth.ioc.IBeanRuntime;
import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IExternalServiceContext;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.IServiceContextIntern;
import com.koch.ambeth.ioc.IServiceLookup;
import com.koch.ambeth.ioc.config.BeanRuntime;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.ioc.factory.IBeanContextFactoryIntern;
import com.koch.ambeth.ioc.hierarchy.IBeanContextHolder;
import com.koch.ambeth.ioc.hierarchy.SearchType;
import com.koch.ambeth.ioc.link.ILinkContainer;
import com.koch.ambeth.ioc.link.ILinkRegistryNeededRuntime;
import com.koch.ambeth.ioc.link.SpringBeanLookup;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.function.CheckedConsumer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.swing.*;
import java.lang.annotation.Annotation;
import java.util.List;

@RequiredArgsConstructor
public class SpringServiceContext implements IServiceContextIntern {

    @NonNull
    final AnnotationConfigApplicationContext applicationContext;

    @NonNull
    final IServiceLookup beanLookup;

    @NonNull
    final SpringBeanHelper springBeanHelper;

    @Override
    public ISet<Class<?>> collectAllTypeWiredServices() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDisposed() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDisposing() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRunning() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IServiceContext getParent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IServiceContext getRoot() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IServiceContext createService(Class<?>... serviceModules) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IServiceContext createService(String childContextName, Class<?>... serviceModules) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IServiceContext createService(CheckedConsumer<IBeanContextFactory> registerPhaseDelegate, Class<?>... serviceModules) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IServiceContext createService(String childContextName, CheckedConsumer<IBeanContextFactory> registerPhaseDelegate, Class<?>... serviceModules) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <V> IBeanContextHolder<V> createHolder(Class<V> autowiredBeanClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <V> IBeanContextHolder<V> createHolder(String beanName, Class<V> expectedClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> List<T> getObjects(Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Annotation> List<Object> getAnnotatedObjects(Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> List<T> getImplementingObjects(Class<T> interfaceType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerDisposable(IDisposableBean disposableBean) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerDisposeHook(CheckedConsumer<IServiceContext> disposeCallback) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <V> IBeanRuntime<V> registerWithLifecycle(V object) {
        return new SpringBeanRuntime(this, object, true);
    }

    @Override
    public <V> IBeanRuntime<V> registerExternalBean(V externalBean) {
        return new SpringBeanRuntime(this, externalBean, false);
    }

    @Override
    public <V> IBeanRuntime<V> registerAnonymousBean(Class<V> beanType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <V> IBeanRuntime<V> registerBean(Class<V> beanType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IBeanConfiguration getBeanConfiguration(String beanName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printContent(StringBuilder sb) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getService(String serviceName) {
        return beanLookup.getService(serviceName);
    }

    @Override
    public Object getService(String serviceName, boolean checkExistence) {
        return beanLookup.getService(serviceName, checkExistence);
    }

    @Override
    public <V> V getService(String serviceName, Class<V> targetType) {
        return beanLookup.getService(serviceName, targetType);
    }

    @Override
    public <V> V getService(String serviceName, Class<V> targetType, boolean checkExistence) {
        return beanLookup.getService(serviceName, targetType, checkExistence);
    }

    @Override
    public <T> T getService(Class<T> type)
    {
        return beanLookup.getService(type);
    }

    @Override
    public <T> T getService(Class<T> type, boolean checkExistence) {
        return beanLookup.getService(type, checkExistence);
    }

    @Override
    public ILinkRegistryNeededRuntime<?> link(String listenerBeanName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ILinkRegistryNeededRuntime<?> link(String listenerBeanName, String methodName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ILinkRegistryNeededRuntime<?> link(IBeanConfiguration listenerBean) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ILinkRegistryNeededRuntime<?> link(IBeanConfiguration listenerBean, String methodName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <D> ILinkRegistryNeededRuntime<D> link(D listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ILinkRegistryNeededRuntime<?> link(Object listener, String methodName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void dispose()
    {
        close();
    }

    @Override
    public void close() {
        applicationContext.close();
    }

    @Override
    public void childContextDisposed(IServiceContext childContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getDirectBean(String beanName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getDirectBean(Class<?> serviceType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getServiceIntern(Class<T> serviceType, SearchType searchType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getServiceIntern(String serviceName, Class<T> serviceType, SearchType searchType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IBeanContextFactoryIntern getBeanContextFactory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<IBeanInstantiationProcessor> getInstantiationProcessors() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IExternalServiceContext getExternalServiceContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addInstantiationProcessor(IBeanInstantiationProcessor bean) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addPreProcessor(IBeanPreProcessor bean) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addPostProcessor(IBeanPostProcessor bean) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addLinkContainer(ILinkContainer bean) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<IBeanPreProcessor> getPreProcessors() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<IBeanPostProcessor> getPostProcessors() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IBeanConfiguration getBeanConfiguration(IBeanContextFactoryIntern beanContextFactory, String beanName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addNamedBean(String beanName, Object bean) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addAutowiredBean(Class<?> autowireableType, Object bean) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ILinkContainer> getLinkContainers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRunning() {
        throw new UnsupportedOperationException();
    }
}
