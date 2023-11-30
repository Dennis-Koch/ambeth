package com.koch.ambeth.platform;

/*-
 * #%L
 * jambeth-platform
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

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IdentityHashSet;
import com.koch.ambeth.util.config.IProperties;
import lombok.SneakyThrows;

import java.util.Set;

public class PlatformContextConfiguration implements IPlatformContextConfiguration {
    public static final String PlatformContextConfigurationType = "ambeth.platform.configurationtype";

    @SneakyThrows
    public static IPlatformContextConfiguration create() {
        var platformConfigurationTypeName = Properties.getApplication().getString(PlatformContextConfigurationType, PlatformContextConfiguration.class.getName());
        var platformConfigurationType = Thread.currentThread().getContextClassLoader().loadClass(platformConfigurationTypeName);
        return (IPlatformContextConfiguration) platformConfigurationType.getConstructor().newInstance();
    }

    protected final Properties properties = new Properties(Properties.getApplication());

    protected final Set<Class<?>> providerModuleTypes = new HashSet<>();

    protected final Set<IInitializingModule> providerModules = new IdentityHashSet<>();

    protected final Set<Class<?>> frameworkModuleTypes = new HashSet<>();

    protected final Set<IInitializingModule> frameworkModules = new IdentityHashSet<>();

    protected final Set<Class<?>> applicationModuleTypes = new HashSet<>();

    protected final Set<IInitializingModule> applicationModules = new IdentityHashSet<>();

    public IPlatformContextConfiguration addProperties(IProperties properties) {
        this.properties.load(properties);
        return this;
    }

    public IPlatformContextConfiguration addProperties(java.util.Properties properties) {
        this.properties.load(properties);
        return this;
    }

    @Override
    public IPlatformContextConfiguration addProviderModule(Class<?>... providerModuleTypes) {
        for (var providerModuleType : providerModuleTypes) {
            this.providerModuleTypes.add(providerModuleType);
        }
        return this;
    }

    @Override
    public IPlatformContextConfiguration addFrameworkModule(Class<?>... frameworkModuleTypes) {
        for (var frameworkModuleType : frameworkModuleTypes) {
            this.frameworkModuleTypes.add(frameworkModuleType);
        }
        return this;
    }

    @Override
    public IPlatformContextConfiguration addBootstrapModule(java.lang.Class<?>... bootstrapModuleTypes) {
        for (var bootstrapModuleType : bootstrapModuleTypes) {
            this.applicationModuleTypes.add(bootstrapModuleType);
        }
        return this;
    }

    @Override
    public IPlatformContextConfiguration addBootstrapModule(IInitializingModule... bootstrapModules) {
        for (var bootstrapModule : bootstrapModules) {
            this.applicationModules.add(bootstrapModule);
        }
        return this;
    }

    @Override
    public IPlatformContextConfiguration addProviderModule(IInitializingModule... providerModules) {
        for (var providerModule : providerModules) {
            this.providerModules.add(providerModule);
        }
        return this;
    }

    @Override
    public IPlatformContextConfiguration addFrameworkModule(IInitializingModule... frameworkModules) {
        for (var frameworkModule : frameworkModules) {
            this.frameworkModules.add(frameworkModule);
        }
        return this;
    }

    @Override
    public IPlatformContextConfiguration addProperties(IProperties... properties) {
        for (var propertiesItem : properties) {
            this.properties.load(propertiesItem);
        }
        return this;
    }

    @Override
    public IPlatformContextConfiguration addProperties(java.util.Properties... properties) {
        for (var propertiesItem : properties) {
            this.properties.load(propertiesItem);
        }
        return this;
    }

    @Override
    public IAmbethPlatformContext createPlatformContext() {
        return AmbethPlatformContext.create(properties, providerModuleTypes.toArray(new Class<?>[providerModuleTypes.size()]), frameworkModuleTypes.toArray(new Class<?>[frameworkModuleTypes.size()]),
                applicationModuleTypes.toArray(new Class<?>[applicationModuleTypes.size()]), providerModules.toArray(new IInitializingModule[providerModules.size()]),
                frameworkModules.toArray(new IInitializingModule[frameworkModules.size()]), applicationModules.toArray(new IInitializingModule[applicationModules.size()]));
    }
}
