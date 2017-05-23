package com.koch.ambeth.ioc.factory;

/*-
 * #%L
 * jambeth-ioc
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

import java.util.List;

import com.koch.ambeth.ioc.DisposableBeanHook;
import com.koch.ambeth.ioc.DisposableHook;
import com.koch.ambeth.ioc.IBeanInstantiationProcessor;
import com.koch.ambeth.ioc.IBeanPostProcessor;
import com.koch.ambeth.ioc.IBeanPreProcessor;
import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IExternalServiceContext;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.ServiceContext;
import com.koch.ambeth.ioc.accessor.AccessorTypeProvider;
import com.koch.ambeth.ioc.accessor.IAccessorTypeProvider;
import com.koch.ambeth.ioc.bytecode.ClassCache;
import com.koch.ambeth.ioc.bytecode.SimpleClassLoaderProvider;
import com.koch.ambeth.ioc.config.BeanConfiguration;
import com.koch.ambeth.ioc.config.BeanInstanceConfiguration;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.config.IocConfigurationConstants;
import com.koch.ambeth.ioc.config.PropertiesPreProcessor;
import com.koch.ambeth.ioc.extendable.ExtendableRegistry;
import com.koch.ambeth.ioc.extendable.IExtendableRegistry;
import com.koch.ambeth.ioc.garbageproxy.GarbageProxyFactory;
import com.koch.ambeth.ioc.garbageproxy.IGarbageProxyFactory;
import com.koch.ambeth.ioc.link.AutoLinkPreProcessor;
import com.koch.ambeth.ioc.link.ILinkController;
import com.koch.ambeth.ioc.link.ILinkRegistryNeededConfiguration;
import com.koch.ambeth.ioc.link.ILinkRegistryNeededRuntime;
import com.koch.ambeth.ioc.link.LinkConfiguration;
import com.koch.ambeth.ioc.link.LinkController;
import com.koch.ambeth.ioc.log.ILoggerCache;
import com.koch.ambeth.ioc.log.LoggerHistory;
import com.koch.ambeth.ioc.log.LoggerInstancePreProcessor;
import com.koch.ambeth.ioc.proxy.CallingProxyPostProcessor;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBeanExtendable;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import com.koch.ambeth.ioc.threadlocal.ThreadLocalCleanupController;
import com.koch.ambeth.ioc.typeinfo.PropertyInfoProvider;
import com.koch.ambeth.ioc.util.ConversionHelper;
import com.koch.ambeth.ioc.util.DelegatingConversionHelper;
import com.koch.ambeth.log.ILoggerHistory;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.util.DelegateFactory;
import com.koch.ambeth.util.IClassCache;
import com.koch.ambeth.util.IClassLoaderProvider;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.IDedicatedConverterExtendable;
import com.koch.ambeth.util.IDelegateFactory;
import com.koch.ambeth.util.IDisposable;
import com.koch.ambeth.util.IInterningFeature;
import com.koch.ambeth.util.InterningFeature;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.StringBuilderCollectableController;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptySet;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.objectcollector.ICollectableControllerExtendable;
import com.koch.ambeth.util.objectcollector.IObjectCollector;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import com.koch.ambeth.util.objectcollector.NoOpObjectCollector;
import com.koch.ambeth.util.objectcollector.ObjectCollector;
import com.koch.ambeth.util.objectcollector.ThreadLocalObjectCollector;
import com.koch.ambeth.util.proxy.IProxyFactory;
import com.koch.ambeth.util.proxy.ProxyFactory;
import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;
import com.koch.ambeth.util.threading.SensitiveThreadLocal;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;
import com.koch.ambeth.util.typeinfo.ITypeInfoProvider;

public class BeanContextFactory implements IBeanContextFactory, ILinkController, IDisposable {
	public static final Object[] emptyArgs = new Object[0];

	public static final Class<?>[] emptyServiceModules = new Class<?>[0];

	public static final ThreadLocal<ILinkedMap<Object, IBeanConfiguration>> pendingConfigurationMapTL =
			new SensitiveThreadLocal<>();

	/**
	 * Creates an IoC context. The content is defined by the bootstrap modules.
	 *
	 * @param bootstrapModules Initializing modules defining the content of the new context.
	 * @return New IoC context.
	 */
	public static IServiceContext createBootstrap(Class<?>... bootstrapModules) {
		return createBootstrap(null, bootstrapModules);
	}

	/**
	 * Creates an IoC context. The content is defined by the properties and bootstrap modules.
	 *
	 * @param properties Properties for the new context.
	 * @param bootstrapModules Initializing modules defining the content of the new context.
	 * @return New IoC context.
	 */
	public static IServiceContext createBootstrap(IProperties properties,
			Class<?>... bootstrapModules) {
		return createBootstrap(properties, bootstrapModules, emptyArgs);
	}

	/**
	 * Creates an IoC context. The content is defined by the properties and bootstrap modules.
	 *
	 * @param properties Properties for the new context.
	 * @param bootstrapModules Initializing modules defining the content of the new context.
	 * @param bootstrapModuleInstances Instantiated modules. E.g. containing resource instances from
	 *        Servlet containers or application servers.
	 * @return New IoC context.
	 */
	public static IServiceContext createBootstrap(IProperties properties, Class<?>[] bootstrapModules,
			Object[] bootstrapModuleInstances) {
		ThreadLocalCleanupController threadLocalCleanupController = null;
		try {
			if (properties == null) {
				properties = Properties.getApplication();
			}
			// create own sub-instance of properties
			Properties newProps = new Properties(properties);

			boolean objectCollectorActive = Boolean
					.parseBoolean(newProps.getString(IocConfigurationConstants.UseObjectCollector, "true"));

			threadLocalCleanupController = new ThreadLocalCleanupController();

			IObjectCollector objectCollector;
			IThreadLocalObjectCollector tlObjectCollector;
			AccessorTypeProvider accessorTypeProvider = new AccessorTypeProvider();

			if (objectCollectorActive) {
				ThreadLocalObjectCollector tempTlLocalObjectCollector = new ThreadLocalObjectCollector();
				ObjectCollector tempObjectCollector = new ObjectCollector();
				tempObjectCollector.setThreadLocalObjectCollector(tempTlLocalObjectCollector);

				tempObjectCollector.registerCollectableController(new StringBuilderCollectableController(),
						StringBuilder.class);

				tlObjectCollector = tempTlLocalObjectCollector;
				objectCollector = tempObjectCollector;

				threadLocalCleanupController.setObjectCollector(tempTlLocalObjectCollector);
			}
			else {
				NoOpObjectCollector tempObjectCollector = new NoOpObjectCollector(accessorTypeProvider);
				tlObjectCollector = tempObjectCollector;
				objectCollector = tempObjectCollector;
			}

			ConversionHelper conversionHelper = new ConversionHelper();
			DelegatingConversionHelper delegatingConversionHelper = new DelegatingConversionHelper();
			LinkController linkController = new LinkController();
			LoggerHistory loggerHistory = new LoggerHistory();
			ExtendableRegistry extendableRegistry = new ExtendableRegistry();
			GarbageProxyFactory garbageProxyFactory = new GarbageProxyFactory();
			InterningFeature interningFeature = new InterningFeature();
			PropertyInfoProvider propertyInfoProvider = new PropertyInfoProvider();

			BeanContextInitializer beanContextInitializer = new BeanContextInitializer();
			CallingProxyPostProcessor callingProxyPostProcessor = new CallingProxyPostProcessor();
			ClassCache classCache = new ClassCache();
			SimpleClassLoaderProvider classLoaderProvider = new SimpleClassLoaderProvider();
			classLoaderProvider.setClassLoader(
					(ClassLoader) properties.get(IocConfigurationConstants.ExplicitClassLoader));
			DelegateFactory delegateFactory = new DelegateFactory();
			ProxyFactory proxyFactory = new ProxyFactory();
			AutoLinkPreProcessor threadLocalCleanupPreProcessor = new AutoLinkPreProcessor();
			LoggerInstancePreProcessor loggerInstancePreProcessor = new LoggerInstancePreProcessor();

			callingProxyPostProcessor.setPropertyInfoProvider(propertyInfoProvider);
			classCache.setClassLoaderProvider(classLoaderProvider);
			classCache.setInterningFeature(interningFeature);
			conversionHelper.setClassCache(classCache);
			delegatingConversionHelper.setDefaultConversionHelper(conversionHelper);
			extendableRegistry.setObjectCollector(tlObjectCollector);
			linkController.setExtendableRegistry(extendableRegistry);
			linkController.setProps(newProps);
			linkController.setProxyFactory(proxyFactory);
			beanContextInitializer.setCallingProxyPostProcessor(callingProxyPostProcessor);
			beanContextInitializer.setConversionHelper(delegatingConversionHelper);
			beanContextInitializer.setObjectCollector(tlObjectCollector);
			beanContextInitializer.setPropertyInfoProvider(propertyInfoProvider);
			garbageProxyFactory.setAccessorTypeProvider(accessorTypeProvider);
			loggerInstancePreProcessor.setObjectCollector(tlObjectCollector);
			propertyInfoProvider.setObjectCollector(tlObjectCollector);
			proxyFactory.setClassLoaderProvider(classLoaderProvider);
			threadLocalCleanupPreProcessor.setExtendableRegistry(extendableRegistry);
			threadLocalCleanupPreProcessor.setExtendableType(IThreadLocalCleanupBeanExtendable.class);

			classLoaderProvider.afterPropertiesSet();
			loggerInstancePreProcessor.afterPropertiesSet();
			propertyInfoProvider.afterPropertiesSet();

			scanForLogInstance(loggerInstancePreProcessor, propertyInfoProvider, newProps,
					accessorTypeProvider);
			scanForLogInstance(loggerInstancePreProcessor, propertyInfoProvider, newProps,
					callingProxyPostProcessor);
			scanForLogInstance(loggerInstancePreProcessor, propertyInfoProvider, newProps,
					delegatingConversionHelper);
			scanForLogInstance(loggerInstancePreProcessor, propertyInfoProvider, newProps,
					extendableRegistry);
			scanForLogInstance(loggerInstancePreProcessor, propertyInfoProvider, newProps,
					linkController);
			scanForLogInstance(loggerInstancePreProcessor, propertyInfoProvider, newProps, loggerHistory);
			scanForLogInstance(loggerInstancePreProcessor, propertyInfoProvider, newProps,
					beanContextInitializer);
			scanForLogInstance(loggerInstancePreProcessor, propertyInfoProvider, newProps,
					propertyInfoProvider);
			scanForLogInstance(loggerInstancePreProcessor, propertyInfoProvider, newProps,
					threadLocalCleanupController);
			scanForLogInstance(loggerInstancePreProcessor, propertyInfoProvider, newProps,
					threadLocalCleanupPreProcessor);

			accessorTypeProvider.afterPropertiesSet();
			callingProxyPostProcessor.afterPropertiesSet();
			delegatingConversionHelper.afterPropertiesSet();
			extendableRegistry.afterPropertiesSet();
			garbageProxyFactory.afterPropertiesSet();
			linkController.afterPropertiesSet();
			loggerHistory.afterPropertiesSet();
			beanContextInitializer.afterPropertiesSet();
			threadLocalCleanupController.afterPropertiesSet();
			threadLocalCleanupPreProcessor.afterPropertiesSet();

			PropertiesPreProcessor propertiesPreProcessor = new PropertiesPreProcessor();
			propertiesPreProcessor.setConversionHelper(delegatingConversionHelper);
			propertiesPreProcessor.setPropertyInfoProvider(propertyInfoProvider);
			propertiesPreProcessor.afterPropertiesSet();

			// The DelegatingConversionHelper is functional, but has yet no properties set
			propertiesPreProcessor.preProcessProperties(null, null, newProps,
					"delegatingConversionHelper", delegatingConversionHelper,
					DelegatingConversionHelper.class, null, EmptySet.<String>emptySet(), null);
			delegatingConversionHelper.afterPropertiesSet();

			BeanContextFactory parentContextFactory = new BeanContextFactory(tlObjectCollector,
					linkController, beanContextInitializer, proxyFactory, null, newProps, null);

			parentContextFactory.registerWithLifecycle(loggerHistory).autowireable(ILoggerHistory.class);
			parentContextFactory.registerWithLifecycle(proxyFactory).autowireable(IProxyFactory.class);
			parentContextFactory.registerWithLifecycle(threadLocalCleanupController).autowireable(
					IThreadLocalCleanupController.class, IThreadLocalCleanupBeanExtendable.class);

			if (objectCollector == tlObjectCollector) {
				parentContextFactory.registerWithLifecycle(objectCollector).autowireable(
						IObjectCollector.class, ICollectableControllerExtendable.class,
						IThreadLocalObjectCollector.class);
			}
			else {
				parentContextFactory.registerWithLifecycle(objectCollector)
						.autowireable(IObjectCollector.class, ICollectableControllerExtendable.class);

				parentContextFactory.registerWithLifecycle(tlObjectCollector)
						.autowireable(IThreadLocalObjectCollector.class);
			}

			parentContextFactory.registerExternalBean(classCache).autowireable(IClassCache.class);

			parentContextFactory.registerExternalBean(classLoaderProvider)
					.autowireable(IClassLoaderProvider.class);

			parentContextFactory.registerExternalBean(delegatingConversionHelper)
					.autowireable(IConversionHelper.class, IDedicatedConverterExtendable.class);

			parentContextFactory.registerWithLifecycle(accessorTypeProvider)
					.autowireable(IAccessorTypeProvider.class);

			parentContextFactory.registerExternalBean(interningFeature)
					.autowireable(IInterningFeature.class);

			parentContextFactory.registerExternalBean(loggerInstancePreProcessor)
					.autowireable(ILoggerCache.class);

			parentContextFactory.registerWithLifecycle(extendableRegistry)
					.autowireable(IExtendableRegistry.class);

			parentContextFactory.registerWithLifecycle(garbageProxyFactory)
					.autowireable(IGarbageProxyFactory.class);

			parentContextFactory.registerWithLifecycle(callingProxyPostProcessor)
					.autowireable(CallingProxyPostProcessor.class);

			parentContextFactory.registerWithLifecycle(propertyInfoProvider)
					.autowireable(IPropertyInfoProvider.class);

			parentContextFactory.registerWithLifecycle(delegateFactory)
					.autowireable(IDelegateFactory.class);

			if (bootstrapModules != null) {
				for (int a = 0, size = bootstrapModules.length; a < size; a++) {
					parentContextFactory.registerBean(bootstrapModules[a]);
				}
			}
			if (bootstrapModuleInstances != null) {
				for (int a = 0, size = bootstrapModuleInstances.length; a < size; a++) {
					parentContextFactory.registerExternalBean(bootstrapModuleInstances[a]);
				}
			}
			List<IBeanPreProcessor> preProcessors = new ArrayList<>();
			preProcessors.add(propertiesPreProcessor);
			preProcessors.add(loggerInstancePreProcessor);
			preProcessors.add(threadLocalCleanupPreProcessor);
			IExternalServiceContext externalServiceContext = (IExternalServiceContext) properties
					.get(IocConfigurationConstants.ExternalServiceContext);
			return parentContextFactory.create("bootstrap", null, null, preProcessors, null,
					externalServiceContext);
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		finally {
			if (threadLocalCleanupController != null) {
				threadLocalCleanupController.cleanupThreadLocal();
			}
		}
	}

	protected static void scanForLogInstance(IBeanPreProcessor beanPreProcessor,
			IPropertyInfoProvider propertyInfoProvider, IProperties properties, Object bean) {
		IPropertyInfo[] props = propertyInfoProvider.getProperties(bean.getClass());
		beanPreProcessor.preProcessProperties(null, null, properties, null, bean, bean.getClass(), null,
				EmptySet.<String>emptySet(), props);
	}

	protected List<IBeanConfiguration> beanConfigurations;

	protected IMap<String, IBeanConfiguration> nameToBeanConfMap;

	protected ILinkedMap<String, String> aliasToBeanNameMap;

	protected ILinkedMap<String, List<String>> beanNameToAliasesMap;

	protected ILinkController linkController;

	protected int anonymousCounter = 0;

	protected IThreadLocalObjectCollector objectCollector;

	protected IBeanContextInitializer beanContextInitializer;

	protected BeanContextFactory parent;

	protected IProxyFactory proxyFactory;

	protected ITypeInfoProvider typeInfoProvider;

	protected Properties props;

	public BeanContextFactory(IThreadLocalObjectCollector objectCollector,
			ILinkController linkController, IBeanContextInitializer beanContextInitializer,
			IProxyFactory proxyFactory, ITypeInfoProvider typeInfoProvider, Properties properties,
			BeanContextFactory parent) {
		ParamChecker.assertParamNotNull(objectCollector, "objectCollector");
		ParamChecker.assertParamNotNull(linkController, "linkController");
		ParamChecker.assertParamNotNull(beanContextInitializer, "beanContextInitializer");
		ParamChecker.assertParamNotNull(proxyFactory, "proxyFactory");
		ParamChecker.assertParamNotNull(properties, "properties");

		this.objectCollector = objectCollector;
		this.linkController = linkController;
		this.beanContextInitializer = beanContextInitializer;
		this.proxyFactory = proxyFactory;
		this.typeInfoProvider = typeInfoProvider;
		props = properties;
		this.parent = parent;
	}

	@Override
	public void dispose() {
		beanConfigurations = null;
		nameToBeanConfMap = null;
		aliasToBeanNameMap = null;
		beanNameToAliasesMap = null;
		linkController = null;
		parent = null;
		linkController = null;
		objectCollector = null;
		beanContextInitializer = null;
		typeInfoProvider = null;
		props = null;
	}

	public List<IBeanConfiguration> getBeanConfigurations() {
		return beanConfigurations;
	}

	public ILinkedMap<String, String> getAliasToBeanNameMap() {
		return aliasToBeanNameMap;
	}

	public ILinkedMap<String, List<String>> getBeanNameToAliasesMap() {
		return beanNameToAliasesMap;
	}

	public Properties getProperties() {
		return props;
	}

	public IBeanContextInitializer getBeanContextInitializer() {
		return beanContextInitializer;
	}

	public BeanContextFactory createChildContextFactory(
			IBeanContextInitializer beanContextInitializer, IServiceContext serviceContext) {
		IProxyFactory proxyFactory = serviceContext.getService(IProxyFactory.class);
		ITypeInfoProvider typeInfoProvider = serviceContext.getService(ITypeInfoProvider.class, false);
		IProperties props = serviceContext.getService(IProperties.class);
		Properties newProps = new Properties(props);
		return new BeanContextFactory(objectCollector, linkController, beanContextInitializer,
				proxyFactory, typeInfoProvider, newProps, this);
	}

	public IBeanConfiguration getBeanConfiguration(String beanName) {
		if (nameToBeanConfMap == null || beanName == null) {
			return null;
		}
		IBeanConfiguration beanConf = nameToBeanConfMap.get(beanName);
		if (beanConf == null) {
			if (aliasToBeanNameMap != null) {
				String aliasName = aliasToBeanNameMap.get(beanName);
				if (aliasName != null) {
					beanConf = nameToBeanConfMap.get(aliasName);
				}
			}
		}
		if (beanConf == null && parent != null) {
			beanConf = parent.getBeanConfiguration(beanName);
		}
		return beanConf;
	}

	public String generateBeanName(Class<?> beanType) {
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
		try {
			int anonymousCounter = ++this.anonymousCounter;

			if (typeInfoProvider != null) {
				sb.append(typeInfoProvider.getTypeInfo(beanType).getSimpleName());
			}
			else {
				sb.append(beanType.getSimpleName());
			}
			sb.append('#').append(anonymousCounter);

			return sb.toString();
		}
		finally {
			tlObjectCollector.dispose(sb);
		}
	}

	protected String generateUniqueContextName(String contextName, ServiceContext parent) {
		if (contextName == null) {
			contextName = "c";
		}
		int value = (int) Math.abs(Math.random() * Integer.MAX_VALUE);
		if (parent != null) {
			return parent.getName() + "/" + contextName + " " + value;
		}
		return contextName + " " + value;
	}

	public IServiceContext create(String contextName,
			IBackgroundWorkerParamDelegate<IBeanContextFactory> registerPhaseDelegate,
			List<IBeanInstantiationProcessor> instantiationProcessors,
			List<IBeanPreProcessor> preProcessors, List<IBeanPostProcessor> postProcessors,
			IExternalServiceContext externalServiceContext) {
		return create(contextName, registerPhaseDelegate, instantiationProcessors, preProcessors,
				postProcessors, externalServiceContext, emptyServiceModules);
	}

	public IServiceContext create(String contextName,
			IBackgroundWorkerParamDelegate<IBeanContextFactory> registerPhaseDelegate,
			List<IBeanInstantiationProcessor> instantiationProcessors,
			List<IBeanPreProcessor> preProcessors, List<IBeanPostProcessor> postProcessors,
			IExternalServiceContext externalServiceContext, Class<?>... serviceModuleTypes) {
		ServiceContext context = new ServiceContext(generateUniqueContextName(contextName, null),
				objectCollector, externalServiceContext);

		if (registerPhaseDelegate != null) {
			try {
				registerPhaseDelegate.invoke(this);
			}
			catch (Throwable e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		for (Class<?> serviceModuleType : serviceModuleTypes) {
			registerBean(serviceModuleType);
		}
		if (instantiationProcessors != null) {
			for (int a = 0, size = instantiationProcessors.size(); a < size; a++) {
				context.addInstantiationProcessor(instantiationProcessors.get(a));
			}
		}
		if (preProcessors != null) {
			for (int a = 0, size = preProcessors.size(); a < size; a++) {
				context.addPreProcessor(preProcessors.get(a));
			}
		}
		if (postProcessors != null) {
			for (int a = 0, size = postProcessors.size(); a < size; a++) {
				context.addPostProcessor(postProcessors.get(a));
			}
		}
		beanContextInitializer.initializeBeanContext(context, this);
		return context;
	}

	public IServiceContext create(String contextName, ServiceContext parent,
			IBackgroundWorkerParamDelegate<IBeanContextFactory> registerPhaseDelegate) {
		return create(contextName, parent, registerPhaseDelegate, emptyServiceModules);
	}

	public IServiceContext create(String contextName, ServiceContext parent,
			IBackgroundWorkerParamDelegate<IBeanContextFactory> registerPhaseDelegate,
			Class<?>... serviceModuleTypes) {
		ServiceContext context =
				new ServiceContext(generateUniqueContextName(contextName, parent), parent);

		if (registerPhaseDelegate != null) {
			try {
				registerPhaseDelegate.invoke(this);
			}
			catch (Throwable e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		for (Class<?> serviceModuleType : serviceModuleTypes) {
			registerBean(serviceModuleType);
		}
		List<IBeanInstantiationProcessor> instantiationProcessors = parent.getInstantiationProcessors();
		if (instantiationProcessors != null) {
			for (int a = 0, size = instantiationProcessors.size(); a < size; a++) {
				context.addInstantiationProcessor(instantiationProcessors.get(a));
			}
		}
		List<IBeanPreProcessor> preProcessors = parent.getPreProcessors();
		if (preProcessors != null) {
			for (int a = 0, size = preProcessors.size(); a < size; a++) {
				context.addPreProcessor(preProcessors.get(a));
			}
		}
		List<IBeanPostProcessor> postProcessors = parent.getPostProcessors();
		if (postProcessors != null) {
			for (int a = 0, size = postProcessors.size(); a < size; a++) {
				context.addPostProcessor(postProcessors.get(a));
			}
		}
		beanContextInitializer.initializeBeanContext(context, this);
		return context;
	}

	protected void addBeanConfiguration(IBeanConfiguration beanConfiguration) {
		String beanName = beanConfiguration.getName();
		if (beanName != null && beanName.length() > 0) {
			if (nameToBeanConfMap == null) {
				nameToBeanConfMap = new HashMap<>();
			}
			if (aliasToBeanNameMap != null && aliasToBeanNameMap.containsKey(beanName)) {
				throw new IllegalArgumentException("An alias with the name '" + beanName
						+ "' of this bean is already registered in this context");
			}
			if (!beanConfiguration.isOverridesExisting()) {
				if (!nameToBeanConfMap.putIfNotExists(beanName, beanConfiguration)) {
					IBeanConfiguration existingBeanConfiguration = nameToBeanConfMap.get(beanName);
					if (!existingBeanConfiguration.isOverridesExisting()) {
						throw ServiceContext.createDuplicateBeanNameException(beanName, beanConfiguration,
								existingBeanConfiguration);
					}
					// Existing config requests precedence over every other bean config. This is no error
					return;
				}
			}
			else {
				// Intentionally put the configuration in the map unaware of an existing entry
				nameToBeanConfMap.put(beanName, beanConfiguration);
			}
		}
		if (beanConfigurations == null) {
			beanConfigurations = new ArrayList<>();
		}
		beanConfigurations.add(beanConfiguration);
	}

	@Override
	public void registerAlias(String aliasBeanName, String beanNameToCreateAliasFor) {
		if (aliasToBeanNameMap == null) {
			aliasToBeanNameMap = new LinkedHashMap<>();
			beanNameToAliasesMap = new LinkedHashMap<>();
		}
		if (!aliasToBeanNameMap.putIfNotExists(aliasBeanName, beanNameToCreateAliasFor)) {
			throw new IllegalArgumentException(
					"Alias '" + aliasBeanName + "' has been already specified");
		}
		List<String> aliasList = beanNameToAliasesMap.get(beanNameToCreateAliasFor);
		if (aliasList == null) {
			aliasList = new ArrayList<>();
			beanNameToAliasesMap.put(beanNameToCreateAliasFor, aliasList);
		}
		aliasList.add(aliasBeanName);
	}

	@Override
	public IBeanConfiguration registerBean(String beanName, Class<?> beanType) {
		ParamChecker.assertParamNotNull(beanName, "beanName");
		if (beanType == null) {
			throw new IllegalArgumentException("Parameter must be valid: beanType (" + beanName + ")");
		}
		BeanConfiguration beanConfiguration =
				new BeanConfiguration(beanType, beanName, proxyFactory, props);

		addBeanConfiguration(beanConfiguration);
		return beanConfiguration;
	}

	@Override
	public IBeanConfiguration registerBean(String beanName, String parentBeanName) {
		ParamChecker.assertParamNotNull(beanName, "beanName");
		ParamChecker.assertParamNotNull(parentBeanName, "parentBeanName");
		BeanConfiguration beanConfiguration =
				new BeanConfiguration(null, beanName, proxyFactory, props);
		beanConfiguration.parent(parentBeanName);

		addBeanConfiguration(beanConfiguration);
		return beanConfiguration;
	}

	@Deprecated
	@Override
	public IBeanConfiguration registerAnonymousBean(Class<?> beanType) {
		return registerBean(beanType);
	}

	@Override
	public IBeanConfiguration registerBean(Class<?> beanType) {
		ParamChecker.assertParamNotNull(beanType, "beanType");

		BeanConfiguration beanConfiguration =
				new BeanConfiguration(beanType, generateBeanName(beanType), proxyFactory, props);

		addBeanConfiguration(beanConfiguration);
		return beanConfiguration;
	}

	@Override
	public <I, T extends I> IBeanConfiguration registerAutowireableBean(Class<I> autowiringType,
			Class<T> beanType) {
		ParamChecker.assertParamNotNull(autowiringType, "autowiringType");
		ParamChecker.assertParamNotNull(beanType, "beanType");
		BeanConfiguration beanConfiguration =
				new BeanConfiguration(beanType, generateBeanName(beanType), proxyFactory, props);
		addBeanConfiguration(beanConfiguration);
		beanConfiguration.autowireable(autowiringType);
		return beanConfiguration;
	}

	@Override
	public IBeanConfiguration registerExternalBean(String beanName, Object externalBean) {
		ParamChecker.assertParamNotNull(beanName, "beanName");
		if (externalBean == null) {
			throw new IllegalArgumentException(
					"Bean \"" + beanName + "\" was registered with value NULL.");
		}
		BeanInstanceConfiguration beanConfiguration =
				new BeanInstanceConfiguration(externalBean, beanName, false, props);
		addBeanConfiguration(beanConfiguration);
		return beanConfiguration;
	}

	@Override
	public IBeanConfiguration registerExternalBean(Object externalBean) {
		ParamChecker.assertParamNotNull(externalBean, "externalBean");
		return registerExternalBean(generateBeanName(externalBean.getClass()), externalBean);
	}

	@Override
	public IBeanConfiguration registerWithLifecycle(String beanName, Object externalBean) {
		ParamChecker.assertParamNotNull(beanName, "beanName");
		ParamChecker.assertParamNotNull(externalBean, "externalBean (" + beanName + ")");
		BeanInstanceConfiguration beanConfiguration =
				new BeanInstanceConfiguration(externalBean, beanName, true, props);
		addBeanConfiguration(beanConfiguration);
		return beanConfiguration;
	}

	@Override
	public IBeanConfiguration registerWithLifecycle(Object externalBean) {
		ParamChecker.assertParamNotNull(externalBean, "externalBean");
		return registerWithLifecycle(generateBeanName(externalBean.getClass()), externalBean);
	}

	@Override
	public void registerDisposable(final IDisposable disposable) {
		ParamChecker.assertParamNotNull(disposable, "disposable");
		registerWithLifecycle(new DisposableHook(disposable));
	}

	@Override
	public void registerDisposable(final IDisposableBean disposableBean) {
		ParamChecker.assertParamNotNull(disposableBean, "disposableBean");
		registerWithLifecycle(new DisposableBeanHook(disposableBean));
	}

	@Override
	public ILinkRegistryNeededConfiguration<?> link(String listenerBeanName) {
		LinkConfiguration<Object> linkConfiguration =
				linkController.createLinkConfiguration(listenerBeanName, (String) null);
		addBeanConfiguration(linkConfiguration);
		return linkConfiguration;
	}

	@Override
	public ILinkRegistryNeededConfiguration<?> link(String listenerBeanName, String methodName) {
		LinkConfiguration<Object> linkConfiguration =
				linkController.createLinkConfiguration(listenerBeanName, methodName);
		addBeanConfiguration(linkConfiguration);
		return linkConfiguration;
	}

	@Override
	public ILinkRegistryNeededConfiguration<?> link(IBeanConfiguration listenerBean) {
		LinkConfiguration<Object> linkConfiguration =
				linkController.createLinkConfiguration(listenerBean, null);
		addBeanConfiguration(linkConfiguration);
		return linkConfiguration;
	}

	@Override
	public ILinkRegistryNeededConfiguration<?> link(IBeanConfiguration listenerBean,
			String methodName) {
		LinkConfiguration<Object> linkConfiguration =
				linkController.createLinkConfiguration(listenerBean, methodName);
		addBeanConfiguration(linkConfiguration);
		return linkConfiguration;
	}

	@Override
	public ILinkRegistryNeededConfiguration<?> link(Object listener, String methodName) {
		LinkConfiguration<Object> linkConfiguration =
				linkController.createLinkConfiguration(listener, methodName);
		addBeanConfiguration(linkConfiguration);
		return linkConfiguration;
	}

	@Override
	public <D> ILinkRegistryNeededConfiguration<D> link(D listener) {
		LinkConfiguration<D> linkConfiguration = linkController.createLinkConfiguration(listener);
		addBeanConfiguration(linkConfiguration);
		return linkConfiguration;
	}

	@Override
	public ILinkRegistryNeededRuntime<?> link(IServiceContext serviceContext,
			String listenerBeanName) {
		return linkController.link(serviceContext, listenerBeanName);
	}

	@Override
	public ILinkRegistryNeededRuntime<?> link(IServiceContext serviceContext, String listenerBeanName,
			String methodName) {
		return linkController.link(serviceContext, listenerBeanName, methodName);
	}

	@Override
	public ILinkRegistryNeededRuntime<?> link(IServiceContext serviceContext,
			IBeanConfiguration listenerBean) {
		return linkController.link(serviceContext, listenerBean);
	}

	@Override
	public ILinkRegistryNeededRuntime<?> link(IServiceContext serviceContext,
			IBeanConfiguration listenerBean, String methodName) {
		return linkController.link(serviceContext, listenerBean, methodName);
	}

	@Override
	public ILinkRegistryNeededRuntime<?> link(IServiceContext serviceContext, Object listener,
			String methodName) {
		return linkController.link(serviceContext, listener, methodName);
	}

	@Override
	public <D> ILinkRegistryNeededRuntime<D> link(IServiceContext serviceContext, D listener) {
		return linkController.link(serviceContext, listener);
	}

	@Override
	public LinkConfiguration<Object> createLinkConfiguration(String listenerBeanName,
			String methodName) {
		return linkController.createLinkConfiguration(listenerBeanName, methodName);
	}

	@Override
	public LinkConfiguration<Object> createLinkConfiguration(IBeanConfiguration listenerBean,
			String methodName) {
		return linkController.createLinkConfiguration(listenerBean, methodName);
	}

	@Override
	public LinkConfiguration<Object> createLinkConfiguration(Object listener, String methodName) {
		return linkController.createLinkConfiguration(listener, methodName);
	}

	@Override
	public <D> LinkConfiguration<D> createLinkConfiguration(D listener) {
		return linkController.createLinkConfiguration(listener);
	}

	@Deprecated
	@Override
	public void link(IServiceContext serviceContext, String registryBeanName, String listenerBeanName,
			Class<?> registryClass) {
		linkController.link(serviceContext, registryBeanName, listenerBeanName, registryClass);
	}

	@Deprecated
	@Override
	public void link(IServiceContext serviceContext, String registryBeanName, String listenerBeanName,
			Class<?> registryClass, Object... arguments) {
		linkController.link(serviceContext, registryBeanName, listenerBeanName, registryClass,
				arguments);
	}

	@Deprecated
	@Override
	public void link(IServiceContext serviceContext, IBeanConfiguration listenerBean,
			Class<?> autowiredRegistryClass) {
		linkController.link(serviceContext, listenerBean, autowiredRegistryClass);
	}

	@Deprecated
	@Override
	public void link(IServiceContext serviceContext, IBeanConfiguration listenerBean,
			Class<?> autowiredRegistryClass, Object... arguments) {
		linkController.link(serviceContext, listenerBean, autowiredRegistryClass, arguments);
	}

	@Deprecated
	@Override
	public void link(IServiceContext serviceContext, String listenerBeanName,
			Class<?> autowiredRegistryClass) {
		linkController.link(serviceContext, listenerBeanName, autowiredRegistryClass);
	}

	@Deprecated
	@Override
	public void link(IServiceContext serviceContext, String listenerBeanName,
			Class<?> autowiredRegistryClass, Object... arguments) {
		linkController.link(serviceContext, listenerBeanName, autowiredRegistryClass, arguments);
	}

	@Deprecated
	@Override
	public IBeanConfiguration createLinkConfiguration(String registryBeanName,
			String listenerBeanName, Class<?> registryClass) {
		return createLinkConfiguration(registryBeanName, listenerBeanName, registryClass, emptyArgs);
	}

	@Deprecated
	@Override
	public IBeanConfiguration createLinkConfiguration(String registryBeanName,
			String listenerBeanName, Class<?> registryClass, Object... arguments) {
		return linkController.createLinkConfiguration(registryBeanName, listenerBeanName, registryClass,
				arguments);
	}

	@Deprecated
	@Override
	public IBeanConfiguration createLinkConfiguration(String listenerBeanName,
			Class<?> autowiredRegistryClass) {
		return createLinkConfiguration(listenerBeanName, autowiredRegistryClass, emptyArgs);
	}

	@Deprecated
	@Override
	public IBeanConfiguration createLinkConfiguration(String listenerBeanName,
			Class<?> autowiredRegistryClass, Object... arguments) {
		return linkController.createLinkConfiguration(listenerBeanName, autowiredRegistryClass,
				arguments);
	}

	@Deprecated
	@Override
	public void linkToNamed(String registryBeanName, String listenerBeanName,
			Class<?> registryClass) {
		linkToNamed(registryBeanName, listenerBeanName, registryClass, emptyArgs);
	}

	@Deprecated
	@Override
	public void link(IBeanConfiguration listenerBean, Class<?> autowiredRegistryClass) {
		link(listenerBean, autowiredRegistryClass, emptyArgs);
	}

	@Deprecated
	@Override
	public void link(String listenerBeanName, Class<?> autowiredRegistryClass) {
		link(listenerBeanName, autowiredRegistryClass, emptyArgs);
	}

	@Deprecated
	@Override
	public void linkToNamed(String registryBeanName, String listenerBeanName, Class<?> registryClass,
			Object... arguments) {
		IBeanConfiguration beanConfiguration =
				createLinkConfiguration(registryBeanName, listenerBeanName, registryClass, arguments);
		addBeanConfiguration(beanConfiguration);
	}

	@Deprecated
	@Override
	public void link(IBeanConfiguration listenerBean, Class<?> autowiredRegistryClass,
			Object... arguments) {
		ParamChecker.assertParamNotNull(listenerBean, "listenerBean");
		String listenerBeanName = listenerBean.getName();
		ParamChecker.assertParamNotNull(listenerBeanName, "listenerBean.getName()");

		IBeanConfiguration beanConfiguration =
				createLinkConfiguration(listenerBeanName, autowiredRegistryClass, arguments);
		addBeanConfiguration(beanConfiguration);
	}

	@Deprecated
	@Override
	public void link(String listenerBeanName, Class<?> autowiredRegistryClass, Object... arguments) {
		IBeanConfiguration beanConfiguration =
				createLinkConfiguration(listenerBeanName, autowiredRegistryClass, arguments);
		addBeanConfiguration(beanConfiguration);
	}

	/**
	 * Adds an autowired bean from an existing context as an external bean (without life cycle) to the
	 * new context and autowires it to the same interface.
	 *
	 * @param sourceContext Existing context containing the autowired bean.
	 * @param targetContextFactory Starting context soon containing the bean.
	 * @param autowireableType Interface the bean is and will be autowired to.
	 */
	public static void transfer(IServiceContext sourceContext,
			IBeanContextFactory targetContextFactory, Class<?> autowireableType) {
		ParamChecker.assertParamNotNull(sourceContext, "sourceContext");
		ParamChecker.assertParamNotNull(targetContextFactory, "targetContextFactory");
		ParamChecker.assertParamNotNull(autowireableType, "autowireableType");
		Object bean = sourceContext.getService(autowireableType);
		if (bean == null) {
			throw new IllegalArgumentException(
					"No autowired bean found for type " + autowireableType.getName());
		}
		targetContextFactory.registerExternalBean(bean).autowireable(autowireableType);
	}

	/**
	 * Adds multiple autowired beans from an existing context as external beans (without life cycle)
	 * to the new context and autowires them to the same interface.
	 *
	 * @param sourceContext Existing context containing the autowired beans.
	 * @param targetContextFactory Starting context soon containing the beans.
	 * @param autowireableTypes Interfaces the beans are and will be autowired to.
	 */
	public static void transfer(IServiceContext sourceContext,
			IBeanContextFactory targetContextFactory, Class<?>... autowireableTypes) {
		ParamChecker.assertParamNotNull(sourceContext, "sourceContext");
		ParamChecker.assertParamNotNull(targetContextFactory, "targetContextFactory");
		ParamChecker.assertParamNotNull(autowireableTypes, "autowireableTypes");
		for (int a = autowireableTypes.length; a-- > 0;) {
			transfer(sourceContext, targetContextFactory, autowireableTypes[a]);
		}
	}

	/**
	 * Adds a named bean from an existing context as an external bean with the same name (without life
	 * cycle) to the new context.
	 *
	 * @param sourceContext Existing context containing the named bean.
	 * @param targetContextFactory Starting context soon containing the bean.
	 * @param beanName Now and future name of the bean.
	 */
	public static void transfer(IServiceContext sourceContext,
			IBeanContextFactory targetContextFactory, String beanName) {
		ParamChecker.assertParamNotNull(sourceContext, "sourceContext");
		ParamChecker.assertParamNotNull(targetContextFactory, "targetContextFactory");
		ParamChecker.assertParamNotNull(beanName, "beanName");
		Object bean = sourceContext.getService(beanName);
		if (bean == null) {
			throw new IllegalArgumentException("No bean found with name '" + beanName + "'");
		}
		targetContextFactory.registerExternalBean(beanName, bean);
	}

	/**
	 * Adds multiple named beans from an existing context as external beans with the same names
	 * (without life cycle) to the new context.
	 *
	 * @param sourceContext Existing context containing the named beans.
	 * @param targetContextFactory Starting context soon containing the beans.
	 * @param beanNames Now and future names of the beans.
	 */
	public static void transfer(IServiceContext sourceContext,
			IBeanContextFactory targetContextFactory, String... beanNames) {
		ParamChecker.assertParamNotNull(sourceContext, "sourceContext");
		ParamChecker.assertParamNotNull(targetContextFactory, "targetContextFactory");
		ParamChecker.assertParamNotNull(beanNames, "beanNames");
		for (int a = beanNames.length; a-- > 0;) {
			transfer(sourceContext, targetContextFactory, beanNames[a]);
		}
	}
}
