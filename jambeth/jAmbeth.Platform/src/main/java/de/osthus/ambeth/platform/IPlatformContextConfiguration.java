package de.osthus.ambeth.platform;

import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.ioc.IInitializingModule;

public interface IPlatformContextConfiguration
{
	IPlatformContextConfiguration addProperties(IProperties... properties);

	IPlatformContextConfiguration addProperties(java.util.Properties... properties);

	IPlatformContextConfiguration addProviderModule(Class<?>... providerModuleTypes);

	IPlatformContextConfiguration addFrameworkModule(Class<?>... frameworkModuleTypes);

	IPlatformContextConfiguration addBootstrapModule(Class<?>... bootstrapModuleTypes);

	IPlatformContextConfiguration addProviderModule(IInitializingModule... providerModules);

	IPlatformContextConfiguration addFrameworkModule(IInitializingModule... frameworkModules);

	IPlatformContextConfiguration addBootstrapModule(IInitializingModule... bootstrapModules);

	IAmbethPlatformContext createPlatformContext();
}