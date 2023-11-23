package com.koch.ambeth.cache.proxy;

/*-
 * #%L
 * jambeth-cache
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

import com.koch.ambeth.cache.Cached;
import com.koch.ambeth.cache.interceptor.CacheInterceptor;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.interceptor.MergeInterceptor;
import com.koch.ambeth.merge.proxy.MergePostProcessor;
import com.koch.ambeth.service.IServiceExtendable;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.proxy.Service;
import com.koch.ambeth.service.proxy.ServiceClient;
import com.koch.ambeth.util.annotation.AnnotationCache;
import com.koch.ambeth.util.annotation.AnnotationEntry;
import com.koch.ambeth.util.proxy.ICascadedInterceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Objects;
import java.util.Set;

public class CachePostProcessor extends MergePostProcessor {
    protected final AnnotationCache<Service> serviceAnnotationCache = new AnnotationCache<Service>(Service.class) {
        @Override
        protected boolean annotationEquals(Service left, Service right) {
            return Objects.equals(left.value(), right.value()) && Objects.equals(left.name(), right.name());
        }
    };
    protected final AnnotationCache<ServiceClient> serviceClientAnnotationCache = new AnnotationCache<ServiceClient>(ServiceClient.class) {
        @Override
        protected boolean annotationEquals(ServiceClient left, ServiceClient right) {
            return Objects.equals(left.value(), right.value());
        }
    };
    @Property(name = ServiceConfigurationConstants.NetworkClientMode, defaultValue = "false")
    protected boolean isNetworkClientMode;
    @LogInstance
    private ILogger log;

    @Override
    protected ICascadedInterceptor handleServiceIntern(IBeanContextFactory beanContextFactory, IServiceContext beanContext, IBeanConfiguration beanConfiguration, Class<?> type,
            Set<Class<?>> requestedTypes) {
        var serviceAnnotation = serviceAnnotationCache.getAnnotation(type);
        if (serviceAnnotation != null) {
            return handleServiceAnnotation(serviceAnnotation, beanContextFactory, beanContext, beanConfiguration, type);
        }
        var serviceClientAnnotation = serviceClientAnnotationCache.getAnnotationEntry(type);
        if (serviceClientAnnotation != null) {
            return handleServiceClientAnnotation(serviceClientAnnotation, beanContextFactory, beanContext, beanConfiguration, type);
        }
        return super.handleServiceIntern(beanContextFactory, beanContext, beanConfiguration, type, requestedTypes);
    }

    protected String extractServiceName(IServiceContext beanContext, String serviceName, Class<?> type) {
        if (serviceName == null || serviceName.length() == 0) {
            serviceName = type.getSimpleName();
            if (serviceName.endsWith("Proxy")) {
                serviceName = serviceName.substring(0, serviceName.length() - 5);
            }
            if (serviceName.charAt(0) == 'I' && Character.isUpperCase(serviceName.charAt(1))) {
                serviceName = serviceName.substring(1);
            }
        }
        return serviceName;
    }

    protected ICascadedInterceptor handleServiceAnnotation(Service serviceAnnotation, IBeanContextFactory beanContextFactory, IServiceContext beanContext, IBeanConfiguration beanConfiguration,
            Class<?> type) {
        if (serviceAnnotation.customExport()) {
            // Do nothing if the service wants to be exported by some special way anywhere else
            return null;
        }
        var serviceName = extractServiceName(beanContext, serviceAnnotation.name(), type);
        if (!isNetworkClientMode) {
            var behavior = createInterceptorModeBehavior(type);

            var interceptor = new CacheInterceptor();
            if (beanContext.isRunning()) {
                interceptor = beanContext.registerWithLifecycle(interceptor)//
                                         .propertyValue(MergeInterceptor.SERVICE_NAME_PROP, serviceName)//
                                         .propertyValue(MergeInterceptor.BEHAVIOR_PROP, behavior)//
                                         .ignoreProperties(MergeInterceptor.PROCESS_SERVICE_PROP)//
                                         .finish();
                beanContext.link(beanConfiguration).to(IServiceExtendable.class).with(serviceName);
            } else {
                beanContextFactory.registerWithLifecycle(interceptor)//
                                  .propertyValue(MergeInterceptor.SERVICE_NAME_PROP, serviceName)//
                                  .propertyValue(MergeInterceptor.BEHAVIOR_PROP, behavior)//
                                  .ignoreProperties(MergeInterceptor.PROCESS_SERVICE_PROP);
                beanContextFactory.link(beanConfiguration).to(IServiceExtendable.class).with(serviceName);
            }
            if (log.isInfoEnabled()) {
                log.info("Registering application service '" + serviceName + "'");
            }
            return interceptor;
        } else {
            if (log.isInfoEnabled()) {
                log.info("Registering application client stub '" + serviceName + "'");
            }
            if (beanContext.isRunning()) {
                beanContext.link(beanConfiguration).to(IServiceExtendable.class).with(serviceName);
            } else {
                beanContextFactory.link(beanConfiguration).to(IServiceExtendable.class).with(serviceName);
            }
            return null;
        }
    }

    protected ICascadedInterceptor handleServiceClientAnnotation(AnnotationEntry<ServiceClient> serviceClientAnnotation, IBeanContextFactory beanContextFactory, IServiceContext beanContext,
            IBeanConfiguration beanConfiguration, Class<?> type) {
        var serviceName = extractServiceName(beanContext, serviceClientAnnotation.getAnnotation().value(), serviceClientAnnotation.getDeclaringType());

        var behavior = createInterceptorModeBehavior(type);

        var interceptor = new CacheInterceptor();
        if (beanContext.isRunning()) {
            interceptor = beanContext.registerWithLifecycle(interceptor)//
                                     .propertyValue(MergeInterceptor.SERVICE_NAME_PROP, serviceName)//
                                     .propertyValue(MergeInterceptor.BEHAVIOR_PROP, behavior)//
                                     .finish();
            // beanContext.link(cacheInterceptorName).to(ICacheServiceByNameExtendable.class).with(serviceName);
        } else {
            beanContextFactory.registerWithLifecycle(interceptor)//
                              .propertyValue(MergeInterceptor.SERVICE_NAME_PROP, serviceName)//
                              .propertyValue(MergeInterceptor.BEHAVIOR_PROP, behavior);
            // beanContextFactory.link(cacheInterceptorName).to(ICacheServiceByNameExtendable.class).with(serviceName);
        }

        if (log.isInfoEnabled()) {
            log.info("Creating application service stub for service '" + serviceName + "' accessing with '" + serviceClientAnnotation.getDeclaringType().getName() + "'");
        }
        return interceptor;
    }

    protected String buildCacheInterceptorName(String serviceName) {
        return "cacheInterceptor." + serviceName;
    }

    @Override
    protected Annotation lookForAnnotation(AnnotatedElement member) {
        var annotation = super.lookForAnnotation(member);
        if (annotation != null) {
            return annotation;
        }
        return member.getAnnotation(Cached.class);
    }
}
