package com.koch.ambeth.core.start;

/*-
 * #%L
 * jambeth-core
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

import com.koch.ambeth.core.config.CoreConfigurationConstants;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.IClasspathScanner;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.IDisposable;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.config.IProperties;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

public class ConfigurableClasspathScanner implements IClasspathScanner, IInitializingBean, IDisposable, Closeable {
    @Autowired
    protected IServiceContext serviceContext;
    @Autowired
    protected IProperties properties;
    @Autowired
    protected IConversionHelper conversionHelper;
    @Property
    protected IMap<Class<?>, Object> autowiredInstances;
    protected IServiceContext scannerContext;
    protected IClasspathScanner classpathScanner;
    @LogInstance
    private ILogger log;

    @Override
    public void afterPropertiesSet() throws Throwable {
        var classpathScannerClass = getConfiguredType(CoreConfigurationConstants.ClasspathScannerClass, CoreClasspathScanner.class.getName());
        var classpathInfoClass = getConfiguredType(CoreConfigurationConstants.ClasspathInfoClass, SystemClasspathInfo.class.getName());

        scannerContext = serviceContext.createService(childContextFactory -> {
            childContextFactory.registerBean(classpathScannerClass).autowireable(IClasspathScanner.class);
            childContextFactory.registerBean(classpathInfoClass).autowireable(IClasspathInfo.class);

            for (Entry<Class<?>, Object> autowiring : autowiredInstances) {
                var typeToPublish = autowiring.getKey();
                var externalBean = autowiring.getValue();
                childContextFactory.registerExternalBean(externalBean).autowireable(typeToPublish);
            }
        });
        classpathScanner = scannerContext.getService(IClasspathScanner.class);
    }

    @Override
    public void dispose() {
        if (scannerContext != null) {
            scannerContext.dispose();

            scannerContext = null;
            classpathScanner = null;
        }
    }

    @Override
    public void close() throws IOException {
        dispose();
    }

    @Override
    public List<Class<?>> scanClassesAnnotatedWith(Class<?>... annotationTypes) {
        List<Class<?>> annotatedWith = classpathScanner.scanClassesAnnotatedWith(annotationTypes);
        return annotatedWith;
    }

    @Override
    public List<Class<?>> scanClassesImplementing(Class<?>... superTypes) {
        List<Class<?>> implementing = classpathScanner.scanClassesImplementing(superTypes);
        return implementing;
    }

    protected Class<?> getConfiguredType(String propertyName, String defaulValue) {
        String className = properties.getString(propertyName, defaulValue);
        Class<?> clazz = conversionHelper.convertValueToType(Class.class, className);

        return clazz;
    }
}
