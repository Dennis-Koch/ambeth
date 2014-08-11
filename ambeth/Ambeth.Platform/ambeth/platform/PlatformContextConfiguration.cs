using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;
using System;

namespace De.Osthus.Ambeth.Platform
{
public class PlatformContextConfiguration : IPlatformContextConfiguration
{
	public static readonly String PlatformContextConfigurationType = "ambeth.platform.configurationtype";

	public static IPlatformContextConfiguration Create()
	{
		String platformConfigurationTypeName = Properties.Application.GetString(PlatformContextConfigurationType,
				typeof(PlatformContextConfiguration).FullName);
		Type platformConfigurationType = AssemblyHelper.GetTypeFromAssemblies(platformConfigurationTypeName);
		return (IPlatformContextConfiguration) Activator.CreateInstance(platformConfigurationType);
	}

	protected Properties properties = new Properties(Properties.Application);

	protected IISet<Type> providerModuleTypes = new CHashSet<Type>();

	protected IISet<IInitializingModule> providerModules = new IdentityHashSet<IInitializingModule>();

	protected IISet<Type> frameworkModuleTypes = new CHashSet<Type>();

	protected IISet<IInitializingModule> frameworkModules = new IdentityHashSet<IInitializingModule>();

	protected IISet<Type> bootstrapModuleTypes = new CHashSet<Type>();

	protected IISet<IInitializingModule> bootstrapModules = new IdentityHashSet<IInitializingModule>();

	public IPlatformContextConfiguration AddProperties(IProperties properties)
	{
		this.properties.Load(properties);
		return this;
	}

	public IPlatformContextConfiguration AddProviderModule(params Type[] providerModuleTypes)
	{
		foreach (Type providerModuleType in providerModuleTypes)
		{
			this.providerModuleTypes.Add(providerModuleType);
		}
		return this;
	}

	public IPlatformContextConfiguration AddFrameworkModule(params Type[] frameworkModuleTypes)
	{
		foreach (Type frameworkModuleType in frameworkModuleTypes)
		{
			this.frameworkModuleTypes.Add(frameworkModuleType);
		}
		return this;
	}

	public IPlatformContextConfiguration AddBootstrapModule(params Type[] bootstrapModuleTypes)
	{
		foreach (Type bootstrapModuleType in bootstrapModuleTypes)
		{
			this.bootstrapModuleTypes.Add(bootstrapModuleType);
		}
		return this;
	}

	public IPlatformContextConfiguration AddBootstrapModule(params IInitializingModule[] bootstrapModules)
	{
		foreach (IInitializingModule bootstrapModule in bootstrapModules)
		{
			this.bootstrapModules.Add(bootstrapModule);
		}
		return this;
	}

	public IPlatformContextConfiguration AddProviderModule(params IInitializingModule[] providerModules)
	{
		foreach (IInitializingModule providerModule in providerModules)
		{
			this.providerModules.Add(providerModule);
		}
		return this;
	}

	public IPlatformContextConfiguration AddFrameworkModule(params IInitializingModule[] frameworkModules)
	{
		foreach (IInitializingModule frameworkModule in frameworkModules)
		{
			this.frameworkModules.Add(frameworkModule);
		}
		return this;
	}

	public IPlatformContextConfiguration AddProperties(params IProperties[] properties)
	{
		foreach (IProperties propertiesItem in properties)
		{
			this.properties.Load(propertiesItem);
		}
		return this;
	}

	public IAmbethPlatformContext CreatePlatformContext()
	{
		return AmbethPlatformContext.Create(properties, providerModuleTypes.ToArray(),
				frameworkModuleTypes.ToArray(),
				bootstrapModuleTypes.ToArray(),
				providerModules.ToArray(),
				frameworkModules.ToArray(),
				bootstrapModules.ToArray());
	}
}
}