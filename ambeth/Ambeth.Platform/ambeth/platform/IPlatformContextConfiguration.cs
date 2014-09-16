using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using System;

namespace De.Osthus.Ambeth.Platform
{
public interface IPlatformContextConfiguration
{
	IPlatformContextConfiguration AddProperties(params IProperties[] properties);

	IPlatformContextConfiguration AddProviderModule(params Type[] providerModuleTypes);

	IPlatformContextConfiguration AddFrameworkModule(params Type[] frameworkModuleTypes);

	IPlatformContextConfiguration AddBootstrapModule(params Type[] bootstrapModuleTypes);

	IPlatformContextConfiguration AddProviderModule(params IInitializingModule[] providerModules);

	IPlatformContextConfiguration AddFrameworkModule(params IInitializingModule[] frameworkModules);

	IPlatformContextConfiguration AddBootstrapModule(params IInitializingModule[] bootstrapModules);

	IAmbethPlatformContext CreatePlatformContext();
}
}