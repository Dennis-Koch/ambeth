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

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.function.CheckedConsumer;

import java.io.Closeable;
import java.util.List;
import java.util.Properties;

public interface IAmbethConfiguration {
    /**
     * Adds the provided properties to the root {@link com.koch.ambeth.log.config.Properties} object.
     *
     * @param properties Properties to add
     * @return This configuration object
     */
    IAmbethConfiguration withProperties(IProperties properties);

    /**
     * Adds the provided properties to the root {@link com.koch.ambeth.log.config.Properties} object.
     *
     * @param properties Properties to add
     * @return This configuration object
     */
    IAmbethConfiguration withProperties(Properties properties);

    /**
     * Adds the provided properties to the root {@link com.koch.ambeth.log.config.Properties} object.
     *
     * @param name  Name of the property to add
     * @param value Value of the property to add
     * @return This configuration object
     */
    IAmbethConfiguration withProperty(String name, String value);

    /**
     * Adds the name of a properties file to load.
     *
     * @param name Name of the properties file to load
     * @return This configuration object
     */
    IAmbethConfiguration withPropertiesFile(String name);

    /**
     * Switches off the search for an environment property 'property.file'.
     *
     * @return This configuration object
     */
    IAmbethConfiguration withoutPropertiesFileSearch();

    /**
     * Adds the provided command line arguments to the root
     * {@link com.koch.ambeth.log.config.Properties} object.
     *
     * @param args Command line arguments
     * @return This configuration object
     */
    IAmbethConfiguration withArgs(String... args);

    /**
     * Adds the provided module classes to the list of modules to start with the framework context.
     *
     * @param frameworkModuleTypes Ambeth modules
     * @return This configuration object
     */
    IAmbethConfiguration withFrameworkModules(Class<? extends IInitializingModule>... frameworkModuleTypes);

    /**
     * Adds the provided module classes to the list of modules to start with the framework context.
     *
     * @param frameworkModuleTypes Ambeth modules
     * @return This configuration object
     */
    IAmbethConfiguration withFrameworkModules(List<Class<? extends IInitializingModule>> frameworkModuleTypes);

    /**
     * Adds the provided module classes to the list of modules to start with the framework context.
     *
     * @param moduleDelegates Ambeth modules
     * @return This configuration object
     */
    @SuppressWarnings("unchecked")
    IAmbethConfigurationIntern withFrameworkModules(CheckedConsumer<IBeanContextFactory>... moduleDelegates);

    /**
     * Adds the provided module classes to the list of modules to start with the application context.
     *
     * @param applicationModuleTypes Application modules
     * @return This configuration object
     */
    IAmbethConfiguration withApplicationModules(Class<? extends IInitializingModule>... applicationModuleTypes);


    /**
     * Adds the provided module classes to the list of modules to start with the framework context.
     *
     * @param applicationModuleTypes Ambeth modules
     * @return This configuration object
     */
    IAmbethConfiguration withApplicationModules(List<Class<? extends IInitializingModule>> applicationModuleTypes);

    /**
     * Adds the provided module classes to the list of modules to start with the application context.
     *
     * @param moduleDelegates Application modules
     * @return This configuration object
     */
    @SuppressWarnings("unchecked")
    IAmbethConfigurationIntern withApplicationModules(CheckedConsumer<IBeanContextFactory>... moduleDelegates);

    /**
     * Extension point to add new features to this fluent API.
     *
     * @param extensionType Type of the extension to instantiate and return
     * @return Instance of the extension type
     */
    <E extends IAmbethConfigurationExtension> E withExtension(Class<E> extensionType);

    /**
     * Sets the provided class loader as the class loader to derive bytecode generated classes from
     *
     * @param classLoader The base class loader where all bytecode classes pull their references from
     * @return This configuration object
     */
    IAmbethConfiguration withClassLoader(ClassLoader classLoader);

    /**
     * Starts the configured Ambeth context. It returns an {@link Closeable}
     * {@link IAmbethApplication} object that also holds the configured application context. Closing
     * this object shuts down the root context and all of its child contexts.
     *
     * @return Ambeth application object
     */
    IAmbethApplication start();

    /**
     * Starts the configured Ambeth context. Also it registers the {@link IAmbethApplication#close()}
     * method with the JVM as a shutdown hook. So after all modules and services have finished their
     * afterStarted() calls and the Thread leaves the main() methods the root context and application
     * are cleanly shut down as well.
     */
    void startAndClose();
}
