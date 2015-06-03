package de.osthus.ambeth;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import de.osthus.ambeth.bundle.IBundleModule;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.config.CoreConfigurationConstants;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.BootstrapModule;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.BeanContextFactory;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.start.CoreClasspathScanner;
import de.osthus.ambeth.start.IAmbethApplication;
import de.osthus.ambeth.start.IAmbethConfiguration;
import de.osthus.ambeth.start.IAmbethConfigurationExtension;
import de.osthus.ambeth.start.IClasspathInfo;
import de.osthus.ambeth.start.SystemClasspathInfo;
import de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate;
import de.osthus.ambeth.util.IClasspathScanner;
import de.osthus.ambeth.util.IConversionHelper;

public class Ambeth implements IAmbethConfiguration, IAmbethApplication
{
	public static IAmbethConfiguration createDefault()
	{
		Ambeth ambeth = new Ambeth(true, true);
		return ambeth;
	}

	public static IAmbethConfiguration createBundle(Class<? extends IBundleModule> bundleModule)
	{
		try
		{
			Ambeth ambeth = new Ambeth(false, true);

			IBundleModule bundleModuleInstance = bundleModule.newInstance();
			Class<? extends IInitializingModule>[] bundleModules = bundleModuleInstance.getBundleModules();
			ambeth.withAmbethModules(bundleModules);

			return ambeth;
		}
		catch (Exception e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	public static IAmbethConfiguration createEmpty()
	{
		Ambeth ambeth = new Ambeth(false, false);
		return ambeth;
	}

	protected Properties properties = new Properties();

	protected HashSet<Class<?>> ambethModules = new HashSet<Class<?>>();

	protected HashSet<Class<?>> applicationModules = new HashSet<Class<?>>();

	protected HashMap<Class<?>, Object> autowiredInstances = new HashMap<Class<?>, Object>();

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
	public IAmbethConfiguration withApplicationModules(Class<?>... modules)
	{
		applicationModules.addAll(modules);
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
		Properties properties = Properties.getApplication();
		properties.load(this.properties);
		Properties.loadBootstrapPropertyFile();

		rootContext = BeanContextFactory.createBootstrap(properties);

		if (andClose)
		{
			registerShutdownHook();
		}

		scanForModules();

		IServiceContext frameworkContext = rootContext.createService(new IBackgroundWorkerParamDelegate<IBeanContextFactory>()
		{

			@Override
			public void invoke(IBeanContextFactory beanContextFactory) throws Throwable
			{
				for (Entry<Class<?>, Object> autowiring : autowiredInstances)
				{
					Class<?> typeToPublish = autowiring.getKey();
					Object externalBean = autowiring.getValue();
					beanContextFactory.registerExternalBean(externalBean).autowireable(typeToPublish);
				}
			}
		}, ambethModules.toArray(Class.class));

		serviceContext = frameworkContext.createService(applicationModules.toArray(Class.class));
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
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			@Override
			public void run()
			{
				rootContext.dispose();
			}
		});
	}

	protected void scanForModules()
	{
		if (!scanForAmbethModules && !scanForApplicationModules)
		{
			return;
		}

		IServiceContext rootContext = this.rootContext;

		final Class<?> classpathScannerClass = getConfiguredType(CoreConfigurationConstants.ClasspathScannerClass, CoreClasspathScanner.class.getName());
		final Class<?> classpathInfoClass = getConfiguredType(CoreConfigurationConstants.ClasspathInfoClass, SystemClasspathInfo.class.getName());

		IServiceContext scannerContext = rootContext.createService(new IBackgroundWorkerParamDelegate<IBeanContextFactory>()
		{

			@Override
			public void invoke(IBeanContextFactory beanContextFactory) throws Throwable
			{
				beanContextFactory.registerBean(classpathScannerClass).autowireable(IClasspathScanner.class);
				beanContextFactory.registerBean(classpathInfoClass).autowireable(IClasspathInfo.class);

				for (Entry<Class<?>, Object> autowiring : autowiredInstances)
				{
					Class<?> typeToPublish = autowiring.getKey();
					Object externalBean = autowiring.getValue();
					beanContextFactory.registerExternalBean(externalBean).autowireable(typeToPublish);
				}
			}
		});
		try
		{
			IClasspathScanner classpathScanner = scannerContext.getService(IClasspathScanner.class);

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
			scannerContext.dispose();
		}
	}

	protected Class<?> getConfiguredType(String propertyName, String defaulValue)
	{
		IServiceContext rootContext = this.rootContext;
		IProperties properties = rootContext.getService(IProperties.class);
		IConversionHelper conversionHelper = rootContext.getService(IConversionHelper.class);

		String classpathScannerClassName = properties.getString(propertyName, defaulValue);
		Class<?> classpathScannerClass = conversionHelper.convertValueToType(Class.class, classpathScannerClassName);

		return classpathScannerClass;
	}
}
