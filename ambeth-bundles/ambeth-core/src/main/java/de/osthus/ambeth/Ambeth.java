package de.osthus.ambeth;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import de.osthus.ambeth.bundle.IBundleModule;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IdentityLinkedSet;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.collections.LinkedHashSet;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.BootstrapModule;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.BeanContextFactory;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.start.ConfigurableClasspathScanner;
import de.osthus.ambeth.start.IAmbethApplication;
import de.osthus.ambeth.start.IAmbethConfiguration;
import de.osthus.ambeth.start.IAmbethConfigurationExtension;
import de.osthus.ambeth.start.IAmbethConfigurationIntern;
import de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate;

public class Ambeth implements IAmbethConfiguration, IAmbethConfigurationIntern, IAmbethApplication
{
	/**
	 * Creates an Ambeth context and scans for Ambeth and application modules.
	 * 
	 * @return Configuration object
	 */
	public static IAmbethConfiguration createDefault()
	{
		Ambeth ambeth = new Ambeth(true, true);
		return ambeth;
	}

	/**
	 * Creates an Ambeth context for a specific Ambeth bundle and scans for application modules.
	 * 
	 * @return Configuration object
	 */
	public static IAmbethConfiguration createBundle(Class<? extends IBundleModule> bundleModule)
	{
		Ambeth ambeth = new Ambeth(false, true);
		setBundleModule(bundleModule, ambeth);
		return ambeth;
	}

	/**
	 * Creates an Ambeth context for a specific Ambeth bundle and does not scan for any modules.
	 * 
	 * @return Configuration object
	 */
	public static IAmbethConfiguration createEmptyBundle(Class<? extends IBundleModule> bundleModule)
	{
		Ambeth ambeth = new Ambeth(false, false);
		setBundleModule(bundleModule, ambeth);
		return ambeth;
	}

	/**
	 * Creates an Ambeth context without any Ambeth or application modules.
	 * 
	 * @return Configuration object
	 */
	public static IAmbethConfiguration createEmpty()
	{
		Ambeth ambeth = new Ambeth(false, false);
		return ambeth;
	}

	protected static void setBundleModule(Class<? extends IBundleModule> bundleModule, Ambeth ambeth)
	{
		try
		{
			IBundleModule bundleModuleInstance = bundleModule.newInstance();
			Class<? extends IInitializingModule>[] bundleModules = bundleModuleInstance.getBundleModules();
			ambeth.withAmbethModules(bundleModules);
		}
		catch (Exception e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected Properties properties = new Properties();

	private ArrayList<String> propertiesFiles = new ArrayList<String>();

	protected boolean scanForPropertiesFile = true;

	protected IdentityLinkedSet<IBackgroundWorkerParamDelegate<IBeanContextFactory>> ambethModuleDelegates = new IdentityLinkedSet<IBackgroundWorkerParamDelegate<IBeanContextFactory>>();

	protected LinkedHashSet<Class<?>> ambethModules = new LinkedHashSet<Class<?>>();

	protected IdentityLinkedSet<IBackgroundWorkerParamDelegate<IBeanContextFactory>> applicationModuleDelegates = new IdentityLinkedSet<IBackgroundWorkerParamDelegate<IBeanContextFactory>>();

	protected LinkedHashSet<Class<?>> applicationModules = new LinkedHashSet<Class<?>>();

	protected LinkedHashMap<Class<?>, Object> autowiredInstances = new LinkedHashMap<Class<?>, Object>();

	protected final boolean scanForAmbethModules;

	protected final boolean scanForApplicationModules;

	private IServiceContext rootContext;

	private IServiceContext serviceContext;

	private Ambeth(boolean scanForAmbethModules, boolean scanForApplicationModules)
	{
		this.scanForAmbethModules = scanForAmbethModules;
		this.scanForApplicationModules = scanForApplicationModules;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAmbethConfiguration withProperties(IProperties properties)
	{
		this.properties.load(properties);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAmbethConfiguration withProperties(java.util.Properties properties)
	{
		this.properties.load(properties);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAmbethConfiguration withProperty(String name, String value)
	{
		properties.putString(name, value);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAmbethConfiguration withPropertiesFile(String name)
	{
		propertiesFiles.add(name);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAmbethConfiguration withoutPropertiesFileSearch()
	{
		scanForPropertiesFile = false;

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAmbethConfiguration withArgs(String... args)
	{
		properties.fillWithCommandLineArgs(args);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAmbethConfiguration withAmbethModules(Class<?>... modules)
	{
		ambethModules.addAll(modules);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAmbethConfigurationIntern withAmbethModules(IBackgroundWorkerParamDelegate<IBeanContextFactory>... moduleDelegates)
	{
		ambethModuleDelegates.addAll(moduleDelegates);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAmbethConfiguration withApplicationModules(Class<?>... modules)
	{
		applicationModules.addAll(modules);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAmbethConfigurationIntern withApplicationModules(IBackgroundWorkerParamDelegate<IBeanContextFactory>... moduleDelegates)
	{
		applicationModuleDelegates.addAll(moduleDelegates);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E extends IAmbethConfigurationExtension> E withExtension(Class<E> extensionType)
	{
		try
		{
			E extension = extensionType.newInstance();
			extension.setAmbethConfiguration(this);
			return extension;
		}
		catch (Exception e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAmbethApplication start()
	{
		startInternal(false);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void startAndClose()
	{
		startInternal(true);
	}

	protected void startInternal(boolean andClose)
	{
		Properties properties = new Properties(Properties.getApplication());
		if (scanForPropertiesFile)
		{
			Properties.loadBootstrapPropertyFile(properties);
		}
		properties.load(this.properties);
		for (int i = 0, size = propertiesFiles.size(); i < size; i++)
		{
			String filename = propertiesFiles.get(i);
			properties.load(filename);
		}

		rootContext = BeanContextFactory.createBootstrap(properties);

		if (andClose)
		{
			registerShutdownHook();
		}

		scanForModules();

		final IAmbethApplication ambethApplication = this;
		IServiceContext frameworkContext = rootContext.createService(new IBackgroundWorkerParamDelegate<IBeanContextFactory>()
		{
			@Override
			public void invoke(IBeanContextFactory beanContextFactory) throws Throwable
			{
				beanContextFactory.registerExternalBean(ambethApplication).autowireable(IAmbethApplication.class);

				for (IBackgroundWorkerParamDelegate<IBeanContextFactory> moduleDelegate : ambethModuleDelegates)
				{
					moduleDelegate.invoke(beanContextFactory);
				}
				for (Entry<Class<?>, Object> autowiring : autowiredInstances)
				{
					Class<?> typeToPublish = autowiring.getKey();
					Object externalBean = autowiring.getValue();
					beanContextFactory.registerExternalBean(externalBean).autowireable(typeToPublish);
				}
			}
		}, ambethModules.toArray(Class.class));

		if (applicationModules.size() > 0 || applicationModuleDelegates.size() > 0)
		{
			serviceContext = frameworkContext.createService(new IBackgroundWorkerParamDelegate<IBeanContextFactory>()
			{
				@Override
				public void invoke(IBeanContextFactory beanContextFactory) throws Throwable
				{
					for (IBackgroundWorkerParamDelegate<IBeanContextFactory> moduleDelegate : applicationModuleDelegates)
					{
						moduleDelegate.invoke(beanContextFactory);
					}
				}
			}, applicationModules.toArray(Class.class));
		}
		else
		{
			serviceContext = frameworkContext;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IServiceContext getApplicationContext()
	{
		return serviceContext;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException
	{
		rootContext.dispose();
	}

	/**
	 * Internal method for {@link IAmbethConfigurationExtension}s. This way they can hook bean instances deep in the start process.
	 * 
	 * @param instance
	 *            Bean instance to add to the framework and classpath scanner contexts
	 * @param autowiring
	 *            Type to autowire the bean to
	 */
	public <T> void registerBean(T instance, Class<T> autowiring)
	{
		autowiredInstances.put(autowiring, instance);
	}

	protected void registerShutdownHook()
	{
		final IServiceContext rootContext = this.rootContext;
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				rootContext.dispose();
			}
		}));
	}

	protected void scanForModules()
	{
		if (!scanForAmbethModules && !scanForApplicationModules)
		{
			return;
		}

		ConfigurableClasspathScanner classpathScanner = rootContext.registerBean(ConfigurableClasspathScanner.class)
				.propertyValue("AutowiredInstances", autowiredInstances).finish();
		try
		{
			if (scanForAmbethModules)
			{
				List<Class<?>> ambethModules = classpathScanner.scanClassesAnnotatedWith(FrameworkModule.class);
				this.ambethModules.addAll(ambethModules);
			}
			if (scanForApplicationModules)
			{
				// TODO replace with @ApplicationModule and mark @BootstrapModule as deprecated
				List<Class<?>> applicationModules = classpathScanner.scanClassesAnnotatedWith(BootstrapModule.class);
				this.applicationModules.addAll(applicationModules);
			}
		}
		finally
		{
			classpathScanner.dispose();
		}
	}
}
