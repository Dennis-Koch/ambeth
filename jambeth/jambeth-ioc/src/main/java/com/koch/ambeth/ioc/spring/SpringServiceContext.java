package com.koch.ambeth.ioc.spring;

import com.koch.ambeth.ioc.IBeanRuntime;
import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.ioc.hierarchy.IBeanContextHolder;
import com.koch.ambeth.ioc.link.ILinkRegistryNeededRuntime;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.function.CheckedConsumer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.lang.annotation.Annotation;
import java.util.List;

@RequiredArgsConstructor
public class SpringServiceContext implements IServiceContext {

    @NonNull
    final AnnotationConfigApplicationContext applicationContext;

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
        throw new UnsupportedOperationException();
    }

    @Override
    public <V> IBeanRuntime<V> registerExternalBean(V externalBean) {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getService(String serviceName, boolean checkExistence) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <V> V getService(String serviceName, Class<V> targetType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <V> V getService(String serviceName, Class<V> targetType, boolean checkExistence) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getService(Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getService(Class<T> type, boolean checkExistence) {
        throw new UnsupportedOperationException();
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
    public void dispose() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }
}
