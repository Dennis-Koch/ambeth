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

import java.util.Set;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IdentityHashSet;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class PlatformContextConfiguration implements IPlatformContextConfiguration {
	public static final String PlatformContextConfigurationType = "ambeth.platform.configurationtype";

	public static IPlatformContextConfiguration create() {
		String platformConfigurationTypeName = Properties.getApplication()
				.getString(PlatformContextConfigurationType, PlatformContextConfiguration.class.getName());
		try {
			Class<?> platformConfigurationType =
					Thread.currentThread().getContextClassLoader().loadClass(platformConfigurationTypeName);
			return (IPlatformContextConfiguration) platformConfigurationType.newInstance();
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected final Properties properties = new Properties(Properties.getApplication());

	protected final Set<Class<?>> providerModuleTypes = new HashSet<>();

	protected final Set<IInitializingModule> providerModules =
			new IdentityHashSet<>();

	protected final Set<Class<?>> frameworkModuleTypes = new HashSet<>();

	protected final Set<IInitializingModule> frameworkModules =
			new IdentityHashSet<>();

	protected final Set<Class<?>> bootstrapModuleTypes = new HashSet<>();

	protected final Set<IInitializingModule> bootstrapModules =
			new IdentityHashSet<>();

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
		for (Class<?> providerModuleType : providerModuleTypes) {
			this.providerModuleTypes.add(providerModuleType);
		}
		return this;
	}

	@Override
	public IPlatformContextConfiguration addFrameworkModule(Class<?>... frameworkModuleTypes) {
		for (Class<?> frameworkModuleType : frameworkModuleTypes) {
			this.frameworkModuleTypes.add(frameworkModuleType);
		}
		return this;
	}

	@Override
	public IPlatformContextConfiguration addBootstrapModule(
			java.lang.Class<?>... bootstrapModuleTypes) {
		for (Class<?> bootstrapModuleType : bootstrapModuleTypes) {
			this.bootstrapModuleTypes.add(bootstrapModuleType);
		}
		return this;
	}

	@Override
	public IPlatformContextConfiguration addBootstrapModule(IInitializingModule... bootstrapModules) {
		for (IInitializingModule bootstrapModule : bootstrapModules) {
			this.bootstrapModules.add(bootstrapModule);
		}
		return this;
	}

	@Override
	public IPlatformContextConfiguration addProviderModule(IInitializingModule... providerModules) {
		for (IInitializingModule providerModule : providerModules) {
			this.providerModules.add(providerModule);
		}
		return this;
	}

	@Override
	public IPlatformContextConfiguration addFrameworkModule(IInitializingModule... frameworkModules) {
		for (IInitializingModule frameworkModule : frameworkModules) {
			this.frameworkModules.add(frameworkModule);
		}
		return this;
	}

	@Override
	public IPlatformContextConfiguration addProperties(IProperties... properties) {
		for (IProperties propertiesItem : properties) {
			this.properties.load(propertiesItem);
		}
		return this;
	}

	@Override
	public IPlatformContextConfiguration addProperties(java.util.Properties... properties) {
		for (java.util.Properties propertiesItem : properties) {
			this.properties.load(propertiesItem);
		}
		return this;
	}

	@Override
	public IAmbethPlatformContext createPlatformContext() {
		return AmbethPlatformContext.create(properties,
				providerModuleTypes.toArray(new Class<?>[providerModuleTypes.size()]),
				frameworkModuleTypes.toArray(new Class<?>[frameworkModuleTypes.size()]),
				bootstrapModuleTypes.toArray(new Class<?>[bootstrapModuleTypes.size()]),
				providerModules.toArray(new IInitializingModule[providerModules.size()]),
				frameworkModules.toArray(new IInitializingModule[frameworkModules.size()]),
				bootstrapModules.toArray(new IInitializingModule[bootstrapModules.size()]));
	}
}
