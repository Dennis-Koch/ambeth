package com.koch.ambeth.core;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import com.koch.ambeth.core.bundle.IBundleModule;
import com.koch.ambeth.core.start.ConfigurableClasspathScanner;
import com.koch.ambeth.core.start.IAmbethApplication;
import com.koch.ambeth.core.start.IAmbethConfiguration;
import com.koch.ambeth.core.start.IAmbethConfigurationExtension;
import com.koch.ambeth.core.start.IAmbethConfigurationIntern;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.BootstrapModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.IocConfigurationConstants;
import com.koch.ambeth.ioc.factory.BeanContextFactory;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IdentityLinkedSet;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.collections.LinkedHashSet;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;

public class Ambeth
		implements IAmbethConfiguration, IAmbethConfigurationIntern, IAmbethApplication {
	/**
	 * Creates an Ambeth context and scans for Ambeth and application modules.
	 *
	 * @return Configuration object
	 */
	public static IAmbethConfiguration createDefault() {
		Ambeth ambeth = new Ambeth(true, true);
		return ambeth;
	}

	/**
	 * Creates an Ambeth context for a specific Ambeth bundle and scans for application modules.
	 *
	 * @return Configuration object
	 */
	public static IAmbethConfiguration createBundle(Class<? extends IBundleModule> bundleModule) {
		Ambeth ambeth = new Ambeth(false, true);
		setBundleModule(bundleModule, ambeth);
		return ambeth;
	}

	/**
	 * Creates an Ambeth context for a specific Ambeth bundle and does not scan for any modules.
	 *
	 * @return Configuration object
	 */
	public static IAmbethConfiguration createEmptyBundle(
			Class<? extends IBundleModule> bundleModule) {
		Ambeth ambeth = new Ambeth(false, false);
		setBundleModule(bundleModule, ambeth);
		return ambeth;
	}

	/**
	 * Creates an Ambeth context without any Ambeth or application modules.
	 *
	 * @return Configuration object
	 */
	public static IAmbethConfiguration createEmpty() {
		Ambeth ambeth = new Ambeth(false, false);
		return ambeth;
	}

	protected static void setBundleModule(Class<? extends IBundleModule> bundleModule,
			Ambeth ambeth) {
		try {
			IBundleModule bundleModuleInstance = bundleModule.newInstance();
			Class<? extends IInitializingModule>[] bundleModules =
					bundleModuleInstance.getBundleModules();
			ambeth.withAmbethModules(bundleModules);
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected Properties properties = new Properties();

	private ArrayList<String> propertiesFiles = new ArrayList<>();

	protected boolean scanForPropertiesFile = true;

	protected IdentityLinkedSet<IBackgroundWorkerParamDelegate<IBeanContextFactory>> ambethModuleDelegates =
			new IdentityLinkedSet<>();

	protected LinkedHashSet<Class<?>> ambethModules = new LinkedHashSet<>();

	protected IdentityLinkedSet<IBackgroundWorkerParamDelegate<IBeanContextFactory>> applicationModuleDelegates =
			new IdentityLinkedSet<>();

	protected LinkedHashSet<Class<?>> applicationModules = new LinkedHashSet<>();

	protected LinkedHashMap<Class<?>, Object> autowiredInstances = new LinkedHashMap<>();

	protected final boolean scanForAmbethModules;

	protected final boolean scanForApplicationModules;

	protected ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

	private IServiceContext rootContext;

	private IServiceContext serviceContext;

	private Ambeth(boolean scanForAmbethModules, boolean scanForApplicationModules) {
		this.scanForAmbethModules = scanForAmbethModules;
		this.scanForApplicationModules = scanForApplicationModules;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAmbethConfiguration withProperties(IProperties properties) {
		this.properties.load(properties);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAmbethConfiguration withProperties(java.util.Properties properties) {
		this.properties.load(properties);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAmbethConfiguration withProperty(String name, String value) {
		properties.putString(name, value);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAmbethConfiguration withPropertiesFile(String name) {
		propertiesFiles.add(name);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAmbethConfiguration withoutPropertiesFileSearch() {
		scanForPropertiesFile = false;

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAmbethConfiguration withArgs(String... args) {
		properties.fillWithCommandLineArgs(args);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAmbethConfiguration withClassLoader(ClassLoader classLoader) {
		ParamChecker.assertParamNotNull(classLoader, "classLoader");
		this.classLoader = classLoader;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAmbethConfiguration withAmbethModules(Class<?>... modules) {
		ambethModules.addAll(modules);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public IAmbethConfigurationIntern withAmbethModules(
			IBackgroundWorkerParamDelegate<IBeanContextFactory>... moduleDelegates) {
		ambethModuleDelegates.addAll(moduleDelegates);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAmbethConfiguration withApplicationModules(Class<?>... modules) {
		applicationModules.addAll(modules);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public IAmbethConfigurationIntern withApplicationModules(
			IBackgroundWorkerParamDelegate<IBeanContextFactory>... moduleDelegates) {
		applicationModuleDelegates.addAll(moduleDelegates);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E extends IAmbethConfigurationExtension> E withExtension(Class<E> extensionType) {
		try {
			E extension = extensionType.newInstance();
			extension.setAmbethConfiguration(this);
			return extension;
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAmbethApplication start() {
		startInternal(false);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void startAndClose() {
		startInternal(true);
	}

	protected void startInternal(boolean andClose) {
		Properties properties = new Properties(Properties.getApplication());
		if (scanForPropertiesFile) {
			Properties.loadBootstrapPropertyFile(properties);
		}
		properties.load(this.properties);
		for (int i = 0, size = propertiesFiles.size(); i < size; i++) {
			String filename = propertiesFiles.get(i);
			properties.load(filename);
		}
		properties.put(IocConfigurationConstants.ExplicitClassLoader, classLoader);

		ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(classLoader);
		try {
			rootContext = BeanContextFactory.createBootstrap(properties);

			if (andClose) {
				registerShutdownHook();
			}

			scanForModules();

			final IAmbethApplication ambethApplication = this;
			IServiceContext frameworkContext =
					rootContext.createService(new IBackgroundWorkerParamDelegate<IBeanContextFactory>() {
						@Override
						public void invoke(IBeanContextFactory beanContextFactory) throws Throwable {
							beanContextFactory.registerExternalBean(ambethApplication)
									.autowireable(IAmbethApplication.class);

							for (IBackgroundWorkerParamDelegate<IBeanContextFactory> moduleDelegate : ambethModuleDelegates) {
								moduleDelegate.invoke(beanContextFactory);
							}
							for (Entry<Class<?>, Object> autowiring : autowiredInstances) {
								Class<?> typeToPublish = autowiring.getKey();
								Object externalBean = autowiring.getValue();
								beanContextFactory.registerExternalBean(externalBean).autowireable(typeToPublish);
							}
						}
					}, ambethModules.toArray(Class.class));

			if (applicationModules.size() > 0 || applicationModuleDelegates.size() > 0) {
				serviceContext = frameworkContext
						.createService(new IBackgroundWorkerParamDelegate<IBeanContextFactory>() {
							@Override
							public void invoke(IBeanContextFactory beanContextFactory) throws Throwable {
								for (IBackgroundWorkerParamDelegate<IBeanContextFactory> moduleDelegate : applicationModuleDelegates) {
									moduleDelegate.invoke(beanContextFactory);
								}
							}
						}, applicationModules.toArray(Class.class));
			}
			else {
				serviceContext = frameworkContext;
			}
		}
		finally {
			Thread.currentThread().setContextClassLoader(oldCL);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IServiceContext getApplicationContext() {
		return serviceContext;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException {
		rootContext.dispose();
	}

	/**
	 * Internal method for {@link IAmbethConfigurationExtension}s. This way they can hook bean
	 * instances deep in the start process.
	 *
	 * @param instance Bean instance to add to the framework and classpath scanner contexts
	 * @param autowiring Type to autowire the bean to
	 */
	public <T> void registerBean(T instance, Class<T> autowiring) {
		autowiredInstances.put(autowiring, instance);
	}

	protected void registerShutdownHook() {
		final IServiceContext rootContext = this.rootContext;
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				rootContext.dispose();
			}
		}));
	}

	protected void scanForModules() {
		if (!scanForAmbethModules && !scanForApplicationModules) {
			return;
		}

		ConfigurableClasspathScanner classpathScanner =
				rootContext.registerBean(ConfigurableClasspathScanner.class)
						.propertyValue("AutowiredInstances", autowiredInstances).finish();
		try {
			if (scanForAmbethModules) {
				List<Class<?>> ambethModules =
						classpathScanner.scanClassesAnnotatedWith(FrameworkModule.class);
				this.ambethModules.addAll(ambethModules);
			}
			if (scanForApplicationModules) {
				// TODO replace with @ApplicationModule and mark @BootstrapModule as deprecated
				List<Class<?>> applicationModules =
						classpathScanner.scanClassesAnnotatedWith(BootstrapModule.class);
				this.applicationModules.addAll(applicationModules);
			}
		}
		finally {
			classpathScanner.dispose();
		}
	}
}
