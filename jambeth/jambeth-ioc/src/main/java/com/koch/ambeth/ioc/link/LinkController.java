package com.koch.ambeth.ioc.link;

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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.ServiceContext;
import com.koch.ambeth.ioc.config.BeanConfiguration;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.extendable.IExtendableRegistry;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.proxy.IProxyFactory;
import lombok.Setter;

import java.lang.reflect.Method;

public class LinkController implements ILinkController, IInitializingBean {
    private static final Object[] emptyArgs = new Object[0];

    @Setter
    protected Class<? extends ILinkContainer> linkContainerType = LinkContainer.class;

    @Setter
    protected IExtendableRegistry extendableRegistry;

    @Setter
    protected IProxyFactory proxyFactory;

    @Setter
    protected IProperties props;

    @Override
    public void afterPropertiesSet() throws Throwable {
        ParamChecker.assertNotNull(extendableRegistry, "extendableRegistry");
        ParamChecker.assertNotNull(linkContainerType, "linkContainerType");
        ParamChecker.assertNotNull(props, "props");
        ParamChecker.assertNotNull(proxyFactory, "proxyFactory");
    }

    @Override
    public ILinkRegistryNeededRuntime<?> link(IServiceContext serviceContext, String listenerBeanName) {
        return link(serviceContext, listenerBeanName, (String) null);
    }

    @Override
    public ILinkRegistryNeededRuntime<?> link(IServiceContext serviceContext, String listenerBeanName, String methodName) {
        var linkRuntime = new LinkRuntime<>((ServiceContext) serviceContext, linkContainerType);
        linkRuntime.listener(listenerBeanName);
        if (methodName != null) {
            linkRuntime.listenerMethod(methodName);
        }
        return linkRuntime;
    }

    @Override
    public ILinkRegistryNeededRuntime<?> link(IServiceContext serviceContext, IBeanConfiguration listenerBean) {
        return link(serviceContext, listenerBean, (String) null);
    }

    @Override
    public ILinkRegistryNeededRuntime<?> link(IServiceContext serviceContext, IBeanConfiguration listenerBean, String methodName) {
        var linkRuntime = new LinkRuntime<>((ServiceContext) serviceContext, linkContainerType);
        linkRuntime.listener(listenerBean);
        if (methodName != null) {
            linkRuntime.listenerMethod(methodName);
        }
        return linkRuntime;
    }

    @Override
    public ILinkRegistryNeededRuntime<?> link(IServiceContext serviceContext, Object listener, String methodName) {
        if (listener instanceof String) {
            return link(serviceContext, (String) listener);
        } else if (listener instanceof IBeanConfiguration) {
            return link(serviceContext, (IBeanConfiguration) listener);
        }
        var linkRuntime = new LinkRuntime<>((ServiceContext) serviceContext, linkContainerType);
        linkRuntime.listener(listener);
        if (methodName != null) {
            linkRuntime.listenerMethod(methodName);
        }
        return linkRuntime;
    }

    @Override
    public <D> ILinkRegistryNeededRuntime<D> link(IServiceContext serviceContext, D listener) {
        var linkRuntime = new LinkRuntime<D>((ServiceContext) serviceContext, linkContainerType);
        linkRuntime.listener(listener);
        return linkRuntime;
    }

    @Override
    public LinkConfiguration<Object> createLinkConfiguration(String listenerBeanName, String methodName) {
        var linkConfiguration = new LinkConfiguration<>(linkContainerType, proxyFactory, props);
        linkConfiguration.propertyValue(AbstractLinkContainer.P_LISTENER_NAME, listenerBeanName);
        if (methodName != null) {
            linkConfiguration.propertyValue(AbstractLinkContainer.P_LISTENER_METHOD_NAME, methodName);
        }
        return linkConfiguration;
    }

    @Override
    public LinkConfiguration<Object> createLinkConfiguration(IBeanConfiguration listenerBean, String methodName) {
        var linkConfiguration = new LinkConfiguration<>(linkContainerType, proxyFactory, props);
        linkConfiguration.propertyValue(AbstractLinkContainer.P_LISTENER_BEAN, listenerBean);
        if (methodName != null) {
            linkConfiguration.propertyValue(AbstractLinkContainer.P_LISTENER_METHOD_NAME, methodName);
        }
        return linkConfiguration;
    }

    @Override
    public <D> LinkConfiguration<D> createLinkConfiguration(D listener, String methodName) {
        if (listener instanceof String) {
            return (LinkConfiguration<D>) createLinkConfiguration((String) listener, methodName);
        } else if (listener instanceof IBeanConfiguration) {
            return (LinkConfiguration<D>) createLinkConfiguration((IBeanConfiguration) listener, methodName);
        }
        // else if (listener instanceof Delegate)
        // {
        // throw new Exception("Illegal state: Delegate can not have an additional methodName");
        // }
        var linkConfiguration = new LinkConfiguration<D>(linkContainerType, proxyFactory, props);
        linkConfiguration.propertyValue(AbstractLinkContainer.P_LISTENER, listener);
        if (methodName != null) {
            linkConfiguration.propertyValue(AbstractLinkContainer.P_LISTENER_METHOD_NAME, methodName);
        }
        return linkConfiguration;
    }

    @Override
    public <D> LinkConfiguration<D> createLinkConfiguration(D listener) {
        var linkConfiguration = new LinkConfiguration<D>(linkContainerType, proxyFactory, props);
        linkConfiguration.propertyValue(AbstractLinkContainer.P_LISTENER, listener);
        return linkConfiguration;
    }

    @Deprecated
    @Override
    public void link(IServiceContext serviceContext, String registryBeanName, String listenerBeanName, Class<?> registryClass, Object... arguments) {
        ParamChecker.assertParamNotNull(serviceContext, "serviceContext");
        ParamChecker.assertParamNotNull(registryBeanName, "registryBeanName");
        ParamChecker.assertParamNotNull(listenerBeanName, "listenerBeanName");
        ParamChecker.assertParamNotNull(registryClass, "registryClass");
        ParamChecker.assertParamNotNull(arguments, "arguments");

        int expectedParamCount = arguments.length + 1;

        Method[] methods = extendableRegistry.getAddRemoveMethods(registryClass, arguments, null);

        Object[] realArguments = new Object[expectedParamCount];
        System.arraycopy(arguments, 0, realArguments, 1, arguments.length);

        LinkContainerOld listenerContainer = new LinkContainerOld();
        listenerContainer.setRegistryBeanName(registryBeanName);
        listenerContainer.setListenerBeanName(listenerBeanName);
        listenerContainer.setAddMethod(methods[0]);
        listenerContainer.setRemoveMethod(methods[1]);
        listenerContainer.setArguments(realArguments);
        listenerContainer.setBeanContext(serviceContext);

        serviceContext.registerWithLifecycle(listenerContainer);
    }

    @Deprecated
    @Override
    public void link(IServiceContext serviceContext, String registryBeanName, String listenerBeanName, Class<?> registryClass) {
        link(serviceContext, registryBeanName, listenerBeanName, registryClass, emptyArgs);
    }

    @Deprecated
    @Override
    public void link(IServiceContext serviceContext, IBeanConfiguration listenerBean, Class<?> autowiredRegistryClass) {
        link(serviceContext, listenerBean, autowiredRegistryClass, emptyArgs);
    }

    @Deprecated
    @Override
    public void link(IServiceContext serviceContext, IBeanConfiguration listenerBean, Class<?> autowiredRegistryClass, Object... arguments) {
        ParamChecker.assertParamNotNull(serviceContext, "serviceContext");
        ParamChecker.assertParamNotNull(listenerBean, "listenerBean");
        ParamChecker.assertParamNotNull(autowiredRegistryClass, "autowiredRegistryClass");
        ParamChecker.assertParamNotNull(arguments, "arguments");

        int expectedParamCount = arguments.length + 1;

        Method[] methods = extendableRegistry.getAddRemoveMethods(autowiredRegistryClass, arguments, null);

        Object[] realArguments = new Object[expectedParamCount];
        System.arraycopy(arguments, 0, realArguments, 1, arguments.length);

        LinkContainerOld listenerContainer = new LinkContainerOld();
        listenerContainer.setRegistryBeanAutowiredType(autowiredRegistryClass);
        listenerContainer.setListenerBean(listenerBean);
        listenerContainer.setAddMethod(methods[0]);
        listenerContainer.setRemoveMethod(methods[1]);
        listenerContainer.setArguments(realArguments);
        listenerContainer.setBeanContext(serviceContext);

        serviceContext.registerWithLifecycle(listenerContainer);
    }

    @Deprecated
    @Override
    public void link(IServiceContext serviceContext, String listenerBeanName, Class<?> autowiredRegistryClass) {
        link(serviceContext, listenerBeanName, autowiredRegistryClass, emptyArgs);
    }

    @Deprecated
    @Override
    public void link(IServiceContext serviceContext, String listenerBeanName, Class<?> autowiredRegistryClass, Object... arguments) {
        ParamChecker.assertParamNotNull(serviceContext, "serviceContext");
        ParamChecker.assertParamNotNull(listenerBeanName, "listenerBeanName");
        ParamChecker.assertParamNotNull(autowiredRegistryClass, "autowiredRegistryClass");
        ParamChecker.assertParamNotNull(arguments, "arguments");

        var expectedParamCount = arguments.length + 1;

        var methods = extendableRegistry.getAddRemoveMethods(autowiredRegistryClass, arguments, null);

        var realArguments = new Object[expectedParamCount];
        System.arraycopy(arguments, 0, realArguments, 1, arguments.length);

        var listenerContainer = new LinkContainerOld();
        listenerContainer.setRegistryBeanAutowiredType(autowiredRegistryClass);
        listenerContainer.setListenerBeanName(listenerBeanName);
        listenerContainer.setAddMethod(methods[0]);
        listenerContainer.setRemoveMethod(methods[1]);
        listenerContainer.setArguments(realArguments);
        listenerContainer.setBeanContext(serviceContext);

        serviceContext.registerWithLifecycle(listenerContainer);
    }

    @Deprecated
    @Override
    public IBeanConfiguration createLinkConfiguration(String registryBeanName, String listenerBeanName, Class<?> registryClass) {
        return createLinkConfiguration(registryBeanName, listenerBeanName, registryClass, emptyArgs);
    }

    @Deprecated
    @Override
    public IBeanConfiguration createLinkConfiguration(String registryBeanName, String listenerBeanName, Class<?> registryClass, Object... arguments) {
        ParamChecker.assertParamNotNull(registryBeanName, "registryBeanName");
        ParamChecker.assertParamNotNull(listenerBeanName, "listenerBeanName");
        ParamChecker.assertParamNotNull(registryClass, "registryClass");
        ParamChecker.assertParamNotNull(arguments, "arguments");

        var expectedParamCount = arguments.length + 1;

        var methods = extendableRegistry.getAddRemoveMethods(registryClass, arguments, null);

        var realArguments = new Object[expectedParamCount];
        System.arraycopy(arguments, 0, realArguments, 1, arguments.length);

        var beanConfiguration = new BeanConfiguration(LinkContainerOld.class, null, proxyFactory, props);
        beanConfiguration.propertyValue("RegistryBeanName", registryBeanName);
        beanConfiguration.propertyValue("ListenerBeanName", listenerBeanName);
        beanConfiguration.propertyValue("AddMethod", methods[0]);
        beanConfiguration.propertyValue("RemoveMethod", methods[1]);
        beanConfiguration.propertyValue("Arguments", realArguments);
        return beanConfiguration;
    }

    @Deprecated
    @Override
    public IBeanConfiguration createLinkConfiguration(String listenerBeanName, Class<?> autowiredRegistryClass) {
        return createLinkConfiguration(listenerBeanName, autowiredRegistryClass, emptyArgs);
    }

    @Deprecated
    @Override
    public IBeanConfiguration createLinkConfiguration(String listenerBeanName, Class<?> autowiredRegistryClass, Object... arguments) {
        ParamChecker.assertParamNotNull(listenerBeanName, "listenerBeanName");
        ParamChecker.assertParamNotNull(autowiredRegistryClass, "autowiredRegistryClass");
        ParamChecker.assertParamNotNull(arguments, "arguments");

        var expectedParamCount = arguments.length + 1;

        var methods = extendableRegistry.getAddRemoveMethods(autowiredRegistryClass, arguments, null);

        var realArguments = new Object[expectedParamCount];
        System.arraycopy(arguments, 0, realArguments, 1, arguments.length);

        var beanConfiguration = new BeanConfiguration(LinkContainerOld.class, null, proxyFactory, props);
        beanConfiguration.propertyValue("RegistryBeanAutowiredType", autowiredRegistryClass);
        beanConfiguration.propertyValue("ListenerBeanName", listenerBeanName);
        beanConfiguration.propertyValue("AddMethod", methods[0]);
        beanConfiguration.propertyValue("RemoveMethod", methods[1]);
        beanConfiguration.propertyValue("Arguments", realArguments);
        return beanConfiguration;
    }
}
