package com.koch.ambeth.service.proxy;

/*-
 * #%L
 * jambeth-service
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

import com.koch.ambeth.ioc.IBeanPostProcessor;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IOrderedBeanProcessor;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.ProcessorOrder;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.proxy.Factory;
import com.koch.ambeth.util.proxy.ICascadedInterceptor;
import com.koch.ambeth.util.proxy.IProxyFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Set;

public abstract class AbstractCascadePostProcessor implements IBeanPostProcessor, IInitializingBean, IOrderedBeanProcessor {
    private static final Class<?>[] emptyClasses = new Class<?>[0];
    @Autowired
    protected IProxyFactory proxyFactory;
    @LogInstance
    private ILogger log;

    @Override
    public void afterPropertiesSet() throws Throwable {
        // intended blank
    }

    @Override
    public ProcessorOrder getOrder() {
        return ProcessorOrder.DEFAULT;
    }

    @Override
    public Object postProcessBean(IBeanContextFactory beanContextFactory, IServiceContext beanContext, IBeanConfiguration beanConfiguration, Class<?> beanType, Object targetBean,
            Set<Class<?>> requestedTypes) {
        Factory factory = null;
        ICascadedInterceptor cascadedInterceptor = null;
        var proxiedTargetBean = targetBean;
        if (targetBean instanceof Factory) {
            factory = (Factory) targetBean;
            var callback = factory.getCallback(0);
            if (callback instanceof ICascadedInterceptor) {
                cascadedInterceptor = (ICascadedInterceptor) callback;
                proxiedTargetBean = cascadedInterceptor.getTarget();
            }
        }
        var interceptor = handleServiceIntern(beanContextFactory, beanContext, beanConfiguration, beanType, requestedTypes);
        if (interceptor == null) {
            return targetBean;
        }
        if (log.isDebugEnabled()) {
            log.debug("Proxying bean with name '" + beanConfiguration.getName() + "' by " + getClass().getName());
        }
        if (cascadedInterceptor == null) {
            var lastInterceptor = interceptor;
            while (lastInterceptor.getTarget() instanceof ICascadedInterceptor) {
                lastInterceptor = (ICascadedInterceptor) lastInterceptor.getTarget();
            }
            lastInterceptor.setTarget(proxiedTargetBean);
            Object proxy;
            if (requestedTypes.isEmpty()) {
                proxy = proxyFactory.createProxy(beanType, emptyClasses, interceptor);
            } else {
                proxy = proxyFactory.createProxy(requestedTypes.toArray(Class[]::new), interceptor);
            }
            postHandleServiceIntern(beanContextFactory, beanContext, beanConfiguration, beanType, requestedTypes, proxy);
            return proxy;
        }
        interceptor.setTarget(cascadedInterceptor);
        factory.setCallback(0, interceptor);
        return targetBean;
    }

    protected void postHandleServiceIntern(IBeanContextFactory beanContextFactory, IServiceContext beanContext, IBeanConfiguration beanConfiguration, Class<?> type, Set<Class<?>> requestedTypes,
            Object proxy) {
        // Intended blank
    }

    protected ICascadedInterceptor handleServiceIntern(IBeanContextFactory beanContextFactory, IServiceContext beanContext, IBeanConfiguration beanConfiguration, Class<?> type,
            Set<Class<?>> requestedTypes) {
        return null;
    }

    public IMethodLevelBehavior<Annotation> createInterceptorModeBehavior(Class<?> beanType) {
        var methodToAnnotationMap = new MethodLevelHashMap<Annotation>();
        var methods = ReflectUtil.getMethods(beanType);
        for (Method method : methods) {
            var annotation = lookForAnnotation(method);
            if (annotation != null) {
                methodToAnnotationMap.put(method.getName(), method.getParameterTypes(), annotation);
                continue;
            }
            for (Class<?> currInterface : beanType.getInterfaces()) {
                var methodOnInterface = ReflectUtil.getDeclaredMethod(true, currInterface, null, method.getName(), method.getParameterTypes());
                if (methodOnInterface == null) {
                    continue;
                }
                annotation = lookForAnnotation(methodOnInterface);
                if (annotation == null) {
                    continue;
                }
                methodToAnnotationMap.put(method.getName(), method.getParameterTypes(), annotation);
                break;
            }
        }
        return new MethodLevelBehavior<>(lookForAnnotation(beanType), methodToAnnotationMap);
    }

    protected Annotation lookForAnnotation(AnnotatedElement member) {
        return null;
    }
}
