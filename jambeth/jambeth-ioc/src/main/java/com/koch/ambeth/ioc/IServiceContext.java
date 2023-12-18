package com.koch.ambeth.ioc;

/*-
 * #%L
 * jambeth-ioc
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.ioc.hierarchy.IBeanContextHolder;
import com.koch.ambeth.ioc.link.ILinkRuntimeExtendable;
import com.koch.ambeth.util.IDisposable;
import com.koch.ambeth.util.IPrintable;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.function.CheckedConsumer;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Interface to access a running jAmbeth IoC context from application code.
 */
public interface IServiceContext extends IDisposable, ILinkRuntimeExtendable, AutoCloseable, IPrintable, IServiceLookup {

    /**
     * Builds a set with all types where a bean instance is wired to in this context. This method does
     * not evaluate the content of parent contexts.
     *
     * @return A set containing all types where a bean instance is wired to in this context.
     */
    ISet<Class<?>> collectAllTypeWiredServices();

    /**
     * Returns the name of the context
     *
     * @return The name of the context
     */
    String getName();

    /**
     * Checks if the context is already fully disposed. It is not the opposite of isRunning(). The
     * context also could be starting or in the process of being disposed.
     *
     * @return True if the context is disposed, otherwise false.
     */
    boolean isDisposed();

    /**
     * Checks if the context is in the process of being disposed. In this case {@link #isRunning()} is
     * still true but gives additionally the hint that some functionalities might not work completely
     * as expected any more. This flag will return to false (again) when the disposal is finished.
     *
     * @return True if - and only if - the context is in the process of being disposed, otherwise
     * false.
     */
    boolean isDisposing();

    /**
     * Checks if the context is running. It is not the opposite of isDisposed(). The context also
     * could be starting or in the process of being disposed.
     *
     * @return True if the context is running, otherwise false.
     */
    boolean isRunning();

    /**
     * Getter for the parent context of this context.
     *
     * @return Parent context or null if this is the root context.
     */
    IServiceContext getParent();

    /**
     * Getter for the root context.
     *
     * @return Root context or this if this is the root context.
     */
    IServiceContext getRoot();

    /**
     * Creates a child context of this context with the additional beans from the given initializing
     * modules.
     *
     * @param serviceModules Initializing modules defining the content of the new context.
     * @return New IoC context.
     */
    IServiceContext createService(Class<?>... serviceModules);

    /**
     * Creates a child context of this context with the additional beans from the given initializing
     * modules.
     *
     * @param childContextName The name of the childContext. This makes sense if the context hierarchy will be
     *                         monitored e.g. via JMX clients
     * @param serviceModules   Initializing modules defining the content of the new context.
     * @return New IoC context.
     */
    IServiceContext createService(String childContextName, Class<?>... serviceModules);

    /**
     * Creates a child context of this context with the additional beans from the given initializing
     * modules plus everything you do in the RegisterPhaseDelegate.
     *
     * @param registerPhaseDelegate Similar to an already instantiated module.
     * @param serviceModules        Initializing modules defining the content of the new context.
     * @return New IoC context.
     */
    IServiceContext createService(CheckedConsumer<IBeanContextFactory> registerPhaseDelegate, Class<?>... serviceModules);

    /**
     * Creates a child context of this context with the additional beans from the given initializing
     * modules plus everything you do in the RegisterPhaseDelegate.
     *
     * @param childContextName      The name of the childContext. This makes sense if the context hierarchy will be
     *                              monitored e.g. via JMX clients
     * @param registerPhaseDelegate Similar to an already instantiated module.
     * @param serviceModules        Initializing modules defining the content of the new context.
     * @return New IoC context.
     */
    IServiceContext createService(String childContextName, CheckedConsumer<IBeanContextFactory> registerPhaseDelegate, Class<?>... serviceModules);

    /**
     * For future feature of complex context hierarchies.
     *
     * @param autowiredBeanClass Type the service bean is autowired to.
     * @return Lazy holder for the requested bean.
     */
    <V> IBeanContextHolder<V> createHolder(Class<V> autowiredBeanClass);

    /**
     * For future feature of complex context hierarchies.
     *
     * @param beanName      Name of the service bean to lookup.
     * @param expectedClass Type the service bean is casted to.
     * @return Lazy holder for the requested bean.
     */
    <V> IBeanContextHolder<V> createHolder(String beanName, Class<V> expectedClass);

    /**
     * Lookup for all beans assignable to a given type.
     *
     * @param type Lookup type.
     * @return All beans assignable to a given type.
     */
    <T> List<T> getObjects(Class<T> type);

    /**
     * Lookup for all beans annotated with a given annotation.
     *
     * @param type Annotation type to look for.
     * @return Annotated beans.
     */
    <T extends Annotation> List<Object> getAnnotatedObjects(Class<T> type);

    /**
     * Lookup for all beans implementing a given interface.
     *
     * @param interfaceType Interface type to look for.
     * @return Implementing beans.
     */
    <T> List<T> getImplementingObjects(Class<T> interfaceType);

    /**
     * Links an external bean instance to the contexts dispose life cycle hook.
     *
     * @param disposableBean Bean instance to be disposed with this context.
     */
    void registerDisposable(IDisposableBean disposableBean);

    /**
     * Adds a callback to be executed during context shutdown.
     *
     * @param disposeCallback Callback to be executed.
     */
    void registerDisposeHook(CheckedConsumer<IServiceContext> disposeCallback);

    /**
     * Adds an external bean to the context and links it to the contexts dispose life cycle hook.
     * Injections are done by the context.
     *
     * @param object External bean instance.
     * @return IBeanRuntime to add things to the bean or finish the registration.
     */
    <V> IBeanRuntime<V> registerWithLifecycle(V object);

    /**
     * Adds an external bean to the context while the context is already running. No injections are
     * done by the context.
     *
     * @param externalBean External bean instance.
     * @return IBeanRuntime to add things to the bean or finish the registration.
     */
    <V> IBeanRuntime<V> registerExternalBean(V externalBean);

    /**
     * Registers an anonymous bean while the context is already running.
     *
     * @param beanType Class of the bean to be instantiated.
     * @return IBeanRuntime to add things to the bean or finish the registration.
     */
    <V> IBeanRuntime<V> registerAnonymousBean(Class<V> beanType);

    /**
     * Registers an anonymous bean while the context is already running.
     *
     * @param beanType Class of the bean to be instantiated.
     * @return IBeanRuntime to add things to the bean or finish the registration.
     */
    <V> IBeanRuntime<V> registerBean(Class<V> beanType);

    /**
     * Finder for configuration of a named bean. Makes it possible to read and change the IoC
     * configuration of a bean during runtime.
     *
     * @param beanName Name of the bean.
     * @return Configuration of a named bean.
     */
    IBeanConfiguration getBeanConfiguration(String beanName);

    /**
     * Disabled.
     *
     * @param sb Target StringBuilder.
     */
    void printContent(StringBuilder sb);

    @Override
    default void toString(StringBuilder sb) {
        sb.append(getName());
    }

}
