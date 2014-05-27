package de.osthus.ambeth.platform;

import java.util.Set;

import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IdentityHashSet;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingModule;

public class PlatformContextConfiguration implements IPlatformContextConfiguration
{
	public static final String PlatformContextConfigurationType = "ambeth.platform.configurationtype";

	public static IPlatformContextConfiguration create()
	{
		String platformConfigurationTypeName = Properties.getApplication().getString(PlatformContextConfigurationType,
				PlatformContextConfiguration.class.getName());
		try
		{
			Class<?> platformConfigurationType = Thread.currentThread().getContextClassLoader().loadClass(platformConfigurationTypeName);
			return (IPlatformContextConfiguration) platformConfigurationType.newInstance();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected Properties properties = new Properties(Properties.getApplication());

	protected Set<Class<?>> providerModuleTypes = new HashSet<Class<?>>();

	protected Set<IInitializingModule> providerModules = new IdentityHashSet<IInitializingModule>();

	protected Set<Class<?>> frameworkModuleTypes = new HashSet<Class<?>>();

	protected Set<IInitializingModule> frameworkModules = new IdentityHashSet<IInitializingModule>();

	protected Set<Class<?>> bootstrapModuleTypes = new HashSet<Class<?>>();

	protected Set<IInitializingModule> bootstrapModules = new IdentityHashSet<IInitializingModule>();

	public IPlatformContextConfiguration addProperties(IProperties properties)
	{
		this.properties.load(properties);
		return this;
	}

	public IPlatformContextConfiguration addProperties(java.util.Properties properties)
	{
		this.properties.load(properties);
		return this;
	}

	@Override
	public IPlatformContextConfiguration addProviderModule(Class<?>... providerModuleTypes)
	{
		for (Class<?> providerModuleType : providerModuleTypes)
		{
			this.providerModuleTypes.add(providerModuleType);
		}
		return this;
	}

	@Override
	public IPlatformContextConfiguration addFrameworkModule(Class<?>... frameworkModuleTypes)
	{
		for (Class<?> frameworkModuleType : frameworkModuleTypes)
		{
			this.frameworkModuleTypes.add(frameworkModuleType);
		}
		return this;
	}

	@Override
	public IPlatformContextConfiguration addBootstrapModule(java.lang.Class<?>... bootstrapModuleTypes)
	{
		for (Class<?> bootstrapModuleType : bootstrapModuleTypes)
		{
			this.bootstrapModuleTypes.add(bootstrapModuleType);
		}
		return this;
	}

	@Override
	public IPlatformContextConfiguration addBootstrapModule(IInitializingModule... bootstrapModules)
	{
		for (IInitializingModule bootstrapModule : bootstrapModules)
		{
			this.bootstrapModules.add(bootstrapModule);
		}
		return this;
	}

	@Override
	public IPlatformContextConfiguration addProviderModule(IInitializingModule... providerModules)
	{
		for (IInitializingModule providerModule : providerModules)
		{
			this.providerModules.add(providerModule);
		}
		return this;
	}

	@Override
	public IPlatformContextConfiguration addFrameworkModule(IInitializingModule... frameworkModules)
	{
		for (IInitializingModule frameworkModule : frameworkModules)
		{
			this.frameworkModules.add(frameworkModule);
		}
		return this;
	}

	@Override
	public IPlatformContextConfiguration addProperties(IProperties... properties)
	{
		for (IProperties propertiesItem : properties)
		{
			this.properties.load(propertiesItem);
		}
		return this;
	}

	@Override
	public IPlatformContextConfiguration addProperties(java.util.Properties... properties)
	{
		for (java.util.Properties propertiesItem : properties)
		{
			this.properties.load(propertiesItem);
		}
		return this;
	}

	@Override
	public IAmbethPlatformContext createPlatformContext()
	{
		return AmbethPlatformContext.create(properties, providerModuleTypes.toArray(new Class<?>[providerModuleTypes.size()]),
				frameworkModuleTypes.toArray(new Class<?>[frameworkModuleTypes.size()]),
				bootstrapModuleTypes.toArray(new Class<?>[bootstrapModuleTypes.size()]),
				providerModules.toArray(new IInitializingModule[providerModules.size()]),
				frameworkModules.toArray(new IInitializingModule[frameworkModules.size()]),
				bootstrapModules.toArray(new IInitializingModule[bootstrapModules.size()]));
	}
}
