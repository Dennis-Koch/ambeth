package com.koch.ambeth.ioc.log;

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

import com.koch.ambeth.ioc.IBeanPreProcessor;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.config.IPropertyConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.IConfigurableLogger;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.log.LoggerFactory;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.annotation.AnnotationCache;
import com.koch.ambeth.util.collections.WeakSmartCopyMap;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;
import lombok.Setter;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class LoggerInstancePreProcessor extends WeakSmartCopyMap<Class<?>, ILogger> implements IBeanPreProcessor, ILoggerCache, IInitializingBean {
    protected final AnnotationCache<LogInstance> logInstanceCache = new AnnotationCache<LogInstance>(LogInstance.class) {
        @Override
        protected boolean annotationEquals(LogInstance left, LogInstance right) {
            return Objects.equals(left.value(), right.value());
        }
    };

    @Setter
    protected IThreadLocalObjectCollector objectCollector;

    @Override
    public void afterPropertiesSet() throws Throwable {
        ParamChecker.assertNotNull(objectCollector, "objectCollector");
    }

    @Override
    public void preProcessProperties(IBeanContextFactory beanContextFactory, IServiceContext beanContext, IProperties props, String beanName, Object bean, Class<?> beanType,
            List<IPropertyConfiguration> propertyConfigs, Set<String> ignoredPropertyNames, IPropertyInfo[] properties) {
        preProcessPropertiesIntern(props, bean, beanType, bean.getClass(), ignoredPropertyNames);
    }

    @SneakyThrows
    protected void preProcessPropertiesIntern(IProperties props, Object bean, Class<?> beanType, Class<?> type, Set<String> ignoredPropertyNames) {
        if (type == null || Object.class.equals(type)) {
            return;
        }
        preProcessPropertiesIntern(props, bean, beanType, type.getSuperclass(), ignoredPropertyNames);
        var fields = ReflectUtil.getDeclaredFields(type);
        for (int a = fields.length; a-- > 0; ) {
            var field = fields[a];
            if (!field.getType().equals(ILogger.class)) {
                continue;
            }
            if (ignoredPropertyNames.contains(field.getName())) {
                // do not handle this property
                continue;
            }
            var logger = getLoggerIfNecessary(props, beanType, field);
            if (logger == null) {
                continue;
            }
            field.set(bean, logger);
        }
    }

    protected ILogger getLoggerIfNecessary(IProperties props, Class<?> beanType, Field memberInfo) {
        var logInstance = logInstanceCache.getAnnotation(memberInfo);
        if (logInstance == null) {
            return null;
        }
        var loggerBeanType = memberInfo.getDeclaringClass();
        if (!void.class.equals(logInstance.value())) {
            loggerBeanType = logInstance.value();
        }
        return getCachedLogger(props, loggerBeanType);
    }

    @Override
    public ILogger getCachedLogger(IServiceContext beanContext, Class<?> loggerBeanType) {
        var logger = get(loggerBeanType);
        if (logger != null) {
            return logger;
        }
        return getCachedLogger(beanContext.getService(IProperties.class), loggerBeanType);
    }

    @Override
    public ILogger getCachedLogger(IProperties properties, Class<?> loggerBeanType) {
        var logger = get(loggerBeanType);
        if (logger != null) {
            return logger;
        }
        var writeLock = getWriteLock();
        writeLock.lock();
        try {
            logger = get(loggerBeanType);
            if (logger != null) {
                // Concurrent thread might have been faster
                return logger;
            }
            logger = LoggerFactory.getLogger(loggerBeanType, properties);
            if (logger instanceof IConfigurableLogger) {
                ((IConfigurableLogger) logger).setObjectCollector(objectCollector);
            }
            put(loggerBeanType, logger);
            return logger;
        } finally {
            writeLock.unlock();
        }
    }
}
