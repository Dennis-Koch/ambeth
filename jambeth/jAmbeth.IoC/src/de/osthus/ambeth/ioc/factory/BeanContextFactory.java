package de.osthus.ambeth.ioc.factory;

import java.util.List;

import de.osthus.ambeth.accessor.AccessorTypeProvider;
import de.osthus.ambeth.accessor.IAccessorTypeProvider;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.IocConfigurationConstants;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.config.PropertiesPreProcessor;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IBeanPostProcessor;
import de.osthus.ambeth.ioc.IBeanPreProcessor;
import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.RegisterPhaseDelegate;
import de.osthus.ambeth.ioc.ServiceContext;
import de.osthus.ambeth.ioc.config.BeanConfiguration;
import de.osthus.ambeth.ioc.config.BeanInstanceConfiguration;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.extendable.ExtendableRegistry;
import de.osthus.ambeth.ioc.extendable.IExtendableRegistry;
import de.osthus.ambeth.ioc.link.ILinkController;
import de.osthus.ambeth.ioc.link.ILinkRegistryNeededConfiguration;
import de.osthus.ambeth.ioc.link.ILinkRegistryNeededRuntime;
import de.osthus.ambeth.ioc.link.LinkConfiguration;
import de.osthus.ambeth.ioc.link.LinkController;
import de.osthus.ambeth.ioc.proxy.CallingProxyPostProcessor;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBeanExtendable;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import de.osthus.ambeth.ioc.threadlocal.ThreadLocalCleanupController;
import de.osthus.ambeth.ioc.threadlocal.ThreadLocalCleanupPreProcessor;
import de.osthus.ambeth.log.ILoggerCache;
import de.osthus.ambeth.log.ILoggerHistory;
import de.osthus.ambeth.log.LoggerHistory;
import de.osthus.ambeth.log.LoggerInstancePreProcessor;
import de.osthus.ambeth.objectcollector.ICollectableControllerExtendable;
import de.osthus.ambeth.objectcollector.IObjectCollector;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.objectcollector.NoOpObjectCollector;
import de.osthus.ambeth.objectcollector.ObjectCollector;
import de.osthus.ambeth.objectcollector.ThreadLocalObjectCollector;
import de.osthus.ambeth.proxy.IProxyFactory;
import de.osthus.ambeth.proxy.ProxyFactory;
import de.osthus.ambeth.threading.SensitiveThreadLocal;
import de.osthus.ambeth.typeinfo.IPropertyInfo;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;
import de.osthus.ambeth.typeinfo.ITypeInfoProvider;
import de.osthus.ambeth.typeinfo.PropertyInfoProvider;
import de.osthus.ambeth.util.ConversionHelper;
import de.osthus.ambeth.util.DelegateFactory;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.IDelegateFactory;
import de.osthus.ambeth.util.IDisposable;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.StringBuilderCollectableController;

public class BeanContextFactory implements IBeanContextFactory, ILinkController, IDisposable
{
	public static final Object[] emptyArgs = new Object[0];

	public static final Class<?>[] emptyServiceModules = new Class<?>[0];

	public static final ThreadLocal<ILinkedMap<Object, IBeanConfiguration>> pendingConfigurationMapTL = new SensitiveThreadLocal<ILinkedMap<Object, IBeanConfiguration>>();

	/**
	 * Creates an IoC context. The content is defined by the bootstrap modules.
	 * 
	 * @param bootstrapModules
	 *            Initializing modules defining the content of the new context.
	 * @return New IoC context.
	 */
	public static IServiceContext createBootstrap(Class<?>... bootstrapModules)
	{
		return createBootstrap(null, bootstrapModules);
	}

	/**
	 * Creates an IoC context. The content is defined by the properties and bootstrap modules.
	 * 
	 * @param properties
	 *            Properties for the new context.
	 * @param bootstrapModules
	 *            Initializing modules defining the content of the new context.
	 * @return New IoC context.
	 */
	public static IServiceContext createBootstrap(IProperties properties, Class<?>... bootstrapModules)
	{
		return createBootstrap(properties, bootstrapModules, emptyArgs);
	}

	/**
	 * Creates an IoC context. The content is defined by the properties and bootstrap modules.
	 * 
	 * @param properties
	 *            Properties for the new context.
	 * @param bootstrapModules
	 *            Initializing modules defining the content of the new context.
	 * @param bootstrapModuleInstances
	 *            Instantiated modules. E.g. containing resource instances from Servlet containers or application servers.
	 * @return New IoC context.
	 */
	public static IServiceContext createBootstrap(IProperties properties, Class<?>[] bootstrapModules, Object[] bootstrapModuleInstances)
	{
		ThreadLocalCleanupController threadLocalCleanupController = null;
		try
		{
			if (properties == null)
			{
				properties = Properties.getApplication();
			}
			// create own sub-instance of properties
			Properties newProps = new Properties(properties);

			boolean objectCollectorActive = Boolean.parseBoolean(newProps.getString(IocConfigurationConstants.UseObjectCollector, "true"));

			threadLocalCleanupController = new ThreadLocalCleanupController();

			IObjectCollector objectCollector;
			IThreadLocalObjectCollector tlObjectCollector;
			if (objectCollectorActive)
			{
				ThreadLocalObjectCollector tempTlLocalObjectCollector = new ThreadLocalObjectCollector();
				ObjectCollector tempObjectCollector = new ObjectCollector();
				tempObjectCollector.setThreadLocalObjectCollector(tempTlLocalObjectCollector);

				tempObjectCollector.registerCollectableController(new StringBuilderCollectableController(), StringBuilder.class);

				tlObjectCollector = tempTlLocalObjectCollector;
				objectCollector = tempObjectCollector;

				threadLocalCleanupController.setObjectCollector(tempTlLocalObjectCollector);
			}
			else
			{
				NoOpObjectCollector tempObjectCollector = new NoOpObjectCollector();
				tlObjectCollector = tempObjectCollector;
				objectCollector = tempObjectCollector;
			}

			ConversionHelper conversionHelper = new ConversionHelper();
			LinkController linkController = new LinkController();
			LoggerHistory loggerHistory = new LoggerHistory();
			AccessorTypeProvider accessorTypeProvider = new AccessorTypeProvider();
			ExtendableRegistry extendableRegistry = new ExtendableRegistry();
			PropertyInfoProvider propertyInfoProvider = new PropertyInfoProvider();
			BeanContextInitializer beanContextInitializer = new BeanContextInitializer();
			CallingProxyPostProcessor callingProxyPostProcessor = new CallingProxyPostProcessor();
			ProxyFactory proxyFactory = new ProxyFactory();
			DelegateFactory delegateFactory = new DelegateFactory();

			callingProxyPostProcessor.setPropertyInfoProvider(propertyInfoProvider);
			extendableRegistry.setObjectCollector(tlObjectCollector);
			linkController.setExtendableRegistry(extendableRegistry);
			linkController.setProps(newProps);
			linkController.setProxyFactory(proxyFactory);
			beanContextInitializer.setCallingProxyPostProcessor(callingProxyPostProcessor);
			beanContextInitializer.setConversionHelper(conversionHelper);
			beanContextInitializer.setObjectCollector(tlObjectCollector);
			beanContextInitializer.setPropertyInfoProvider(propertyInfoProvider);
			propertyInfoProvider.setAccessorTypeProvider(accessorTypeProvider);
			propertyInfoProvider.setObjectCollector(tlObjectCollector);

			LoggerInstancePreProcessor loggerInstancePreProcessor = new LoggerInstancePreProcessor();
			loggerInstancePreProcessor.afterPropertiesSet();

			ThreadLocalCleanupPreProcessor threadLocalCleanupPreProcessor = new ThreadLocalCleanupPreProcessor();
			threadLocalCleanupPreProcessor.setThreadLocalCleanupBeanExtendable(threadLocalCleanupController);
			threadLocalCleanupPreProcessor.afterPropertiesSet();

			propertyInfoProvider.afterPropertiesSet();

			scanForLogInstance(loggerInstancePreProcessor, propertyInfoProvider, newProps, accessorTypeProvider);
			scanForLogInstance(loggerInstancePreProcessor, propertyInfoProvider, newProps, callingProxyPostProcessor);
			scanForLogInstance(loggerInstancePreProcessor, propertyInfoProvider, newProps, extendableRegistry);
			scanForLogInstance(loggerInstancePreProcessor, propertyInfoProvider, newProps, linkController);
			scanForLogInstance(loggerInstancePreProcessor, propertyInfoProvider, newProps, loggerHistory);
			scanForLogInstance(loggerInstancePreProcessor, propertyInfoProvider, newProps, beanContextInitializer);
			scanForLogInstance(loggerInstancePreProcessor, propertyInfoProvider, newProps, propertyInfoProvider);
			scanForLogInstance(loggerInstancePreProcessor, propertyInfoProvider, newProps, threadLocalCleanupController);
			scanForLogInstance(loggerInstancePreProcessor, propertyInfoProvider, newProps, threadLocalCleanupPreProcessor);

			accessorTypeProvider.afterPropertiesSet();
			callingProxyPostProcessor.afterPropertiesSet();
			extendableRegistry.afterPropertiesSet();
			linkController.afterPropertiesSet();
			loggerHistory.afterPropertiesSet();
			beanContextInitializer.afterPropertiesSet();
			threadLocalCleanupController.afterPropertiesSet();

			PropertiesPreProcessor propertiesPreProcessor = new PropertiesPreProcessor();
			propertiesPreProcessor.setConversionHelper(conversionHelper);
			propertiesPreProcessor.setPropertyInfoProvider(propertyInfoProvider);
			propertiesPreProcessor.afterPropertiesSet();

			BeanContextFactory parentContextFactory = new BeanContextFactory(tlObjectCollector, linkController, beanContextInitializer, proxyFactory, null,
					newProps, null);

			parentContextFactory.registerWithLifecycle("loggerHistory", loggerHistory).autowireable(ILoggerHistory.class);
			parentContextFactory.registerWithLifecycle("proxyFactory", proxyFactory).autowireable(IProxyFactory.class);
			parentContextFactory.registerWithLifecycle(threadLocalCleanupController).autowireable(IThreadLocalCleanupController.class,
					IThreadLocalCleanupBeanExtendable.class);

			if (objectCollector == tlObjectCollector)
			{
				parentContextFactory.registerWithLifecycle(objectCollector).autowireable(IObjectCollector.class, ICollectableControllerExtendable.class,
						IThreadLocalObjectCollector.class);
			}
			else
			{
				parentContextFactory.registerWithLifecycle(objectCollector).autowireable(IObjectCollector.class, ICollectableControllerExtendable.class);

				parentContextFactory.registerWithLifecycle(tlObjectCollector).autowireable(IThreadLocalObjectCollector.class);
			}

			parentContextFactory.registerWithLifecycle("accessorTypeProvider", accessorTypeProvider).autowireable(IAccessorTypeProvider.class);

			parentContextFactory.registerWithLifecycle(loggerInstancePreProcessor).autowireable(ILoggerCache.class);

			parentContextFactory.registerWithLifecycle("extendableRegistry", extendableRegistry).autowireable(IExtendableRegistry.class);

			parentContextFactory.registerWithLifecycle("callingProxyPostProcessor", callingProxyPostProcessor).autowireable(CallingProxyPostProcessor.class);

			parentContextFactory.registerWithLifecycle("propertyInfoProvider", propertyInfoProvider).autowireable(IPropertyInfoProvider.class);

			parentContextFactory.registerWithLifecycle("conversionHelper", conversionHelper).autowireable(IConversionHelper.class);

			parentContextFactory.registerWithLifecycle("delegateFactory", delegateFactory).autowireable(IDelegateFactory.class);

			if (bootstrapModules != null)
			{
				for (int a = 0, size = bootstrapModules.length; a < size; a++)
				{
					parentContextFactory.registerAnonymousBean(bootstrapModules[a]);
				}
			}
			if (bootstrapModuleInstances != null)
			{
				for (int a = 0, size = bootstrapModuleInstances.length; a < size; a++)
				{
					parentContextFactory.registerExternalBean(bootstrapModuleInstances[a]);
				}
			}
			List<IBeanPreProcessor> preProcessors = new ArrayList<IBeanPreProcessor>();
			preProcessors.add(propertiesPreProcessor);
			preProcessors.add(loggerInstancePreProcessor);
			preProcessors.add(threadLocalCleanupPreProcessor);
			return parentContextFactory.create("bootstrap", null, preProcessors, null);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			if (threadLocalCleanupController != null)
			{
				threadLocalCleanupController.cleanupThreadLocal();
			}
		}
	}

	protected static void scanForLogInstance(IBeanPreProcessor beanPreProcessor, IPropertyInfoProvider propertyInfoProvider, IProperties properties, Object bean)
	{
		IPropertyInfo[] props = propertyInfoProvider.getProperties(bean.getClass());
		beanPreProcessor.preProcessProperties(null, properties, null, bean, bean.getClass(), null, props);
	}

	protected List<IBeanConfiguration> beanConfigurations;

	protected IMap<String, IBeanConfiguration> nameToBeanConfMap;

	protected List<Object> disposableObjects;

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

	public BeanContextFactory(IThreadLocalObjectCollector objectCollector, ILinkController linkController, IBeanContextInitializer beanContextInitializer,
			IProxyFactory proxyFactory, ITypeInfoProvider typeInfoProvider, Properties properties, BeanContextFactory parent)
	{
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
		this.props = properties;
		this.parent = parent;
	}

	@Override
	public void dispose()
	{
		beanConfigurations = null;
		nameToBeanConfMap = null;
		disposableObjects = null;
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

	public List<IBeanConfiguration> getBeanConfigurations()
	{
		return beanConfigurations;
	}

	public ILinkedMap<String, String> getAliasToBeanNameMap()
	{
		return aliasToBeanNameMap;
	}

	public ILinkedMap<String, List<String>> getBeanNameToAliasesMap()
	{
		return beanNameToAliasesMap;
	}

	public Properties getProperties()
	{
		return props;
	}

	public IBeanContextInitializer getBeanContextInitializer()
	{
		return beanContextInitializer;
	}

	public BeanContextFactory createChildContextFactory(IBeanContextInitializer beanContextInitializer, IServiceContext serviceContext)
	{
		IProxyFactory proxyFactory = serviceContext.getService(IProxyFactory.class);
		ITypeInfoProvider typeInfoProvider = serviceContext.getService(ITypeInfoProvider.class, false);
		IProperties props = serviceContext.getService(IProperties.class);
		Properties newProps = new Properties(props);
		return new BeanContextFactory(objectCollector, linkController, beanContextInitializer, proxyFactory, typeInfoProvider, newProps, this);
	}

	public IBeanConfiguration getBeanConfiguration(String beanName)
	{
		if (nameToBeanConfMap == null || beanName == null)
		{
			return null;
		}
		IBeanConfiguration beanConf = nameToBeanConfMap.get(beanName);
		if (beanConf == null)
		{
			if (aliasToBeanNameMap != null)
			{
				String aliasName = aliasToBeanNameMap.get(beanName);
				if (aliasName != null)
				{
					beanConf = nameToBeanConfMap.get(aliasName);
				}
			}
		}
		if (beanConf == null && parent != null)
		{
			beanConf = parent.getBeanConfiguration(beanName);
		}
		return beanConf;
	}

	public String generateBeanName(Class<?> beanType)
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
		try
		{
			int anonymousCounter = ++this.anonymousCounter;

			if (typeInfoProvider != null)
			{
				sb.append(typeInfoProvider.getTypeInfo(beanType).getSimpleName());
			}
			else
			{
				sb.append(beanType.getSimpleName());
			}
			sb.append('#').append(anonymousCounter);

			return sb.toString();
		}
		finally
		{
			tlObjectCollector.dispose(sb);
		}
	}

	protected String generateUniqueContextName(String contextName, ServiceContext parent)
	{
		if (contextName == null)
		{
			contextName = "c";
		}
		int value = (int) Math.abs(Math.random() * Integer.MAX_VALUE);
		if (parent != null)
		{
			return parent.getName() + "/" + contextName + " " + value;
		}
		return contextName + " " + value;
	}

	public IServiceContext create(String contextName, RegisterPhaseDelegate registerPhaseDelegate, List<IBeanPreProcessor> preProcessors,
			List<IBeanPostProcessor> postProcessors)
	{
		return create(contextName, registerPhaseDelegate, preProcessors, postProcessors, emptyServiceModules);
	}

	public IServiceContext create(String contextName, RegisterPhaseDelegate registerPhaseDelegate, List<IBeanPreProcessor> preProcessors,
			List<IBeanPostProcessor> postProcessors, Class<?>... serviceModuleTypes)
	{
		ServiceContext context = new ServiceContext(generateUniqueContextName(contextName, null), objectCollector);

		if (registerPhaseDelegate != null)
		{
			registerPhaseDelegate.invoke(this);
		}
		for (Class<?> serviceModuleType : serviceModuleTypes)
		{
			registerAnonymousBean(serviceModuleType);
		}
		if (preProcessors != null)
		{
			for (int a = 0, size = preProcessors.size(); a < size; a++)
			{
				context.addPreProcessor(preProcessors.get(a));
			}
		}
		if (postProcessors != null)
		{
			for (int a = 0, size = postProcessors.size(); a < size; a++)
			{
				context.addPostProcessor(postProcessors.get(a));
			}
		}
		beanContextInitializer.initializeBeanContext(context, this);
		return context;
	}

	public IServiceContext create(String contextName, ServiceContext parent, RegisterPhaseDelegate registerPhaseDelegate)
	{
		return create(contextName, parent, registerPhaseDelegate, emptyServiceModules);
	}

	public IServiceContext create(String contextName, ServiceContext parent, RegisterPhaseDelegate registerPhaseDelegate, Class<?>... serviceModuleTypes)
	{
		ServiceContext context = new ServiceContext(generateUniqueContextName(contextName, parent), parent);

		if (registerPhaseDelegate != null)
		{
			registerPhaseDelegate.invoke(this);
		}
		for (Class<?> serviceModuleType : serviceModuleTypes)
		{
			registerAnonymousBean(serviceModuleType);
		}
		List<IBeanPreProcessor> preProcessors = parent.getPreProcessors();
		if (preProcessors != null)
		{
			for (int a = 0, size = preProcessors.size(); a < size; a++)
			{
				context.addPreProcessor(preProcessors.get(a));
			}
		}
		List<IBeanPostProcessor> postProcessors = parent.getPostProcessors();
		if (postProcessors != null)
		{
			for (int a = 0, size = postProcessors.size(); a < size; a++)
			{
				context.addPostProcessor(postProcessors.get(a));
			}
		}
		beanContextInitializer.initializeBeanContext(context, this);
		return context;
	}

	protected void addBeanConfiguration(IBeanConfiguration beanConfiguration)
	{
		String beanName = beanConfiguration.getName();
		if (beanName != null && beanName.length() > 0)
		{
			if (nameToBeanConfMap == null)
			{
				nameToBeanConfMap = new HashMap<String, IBeanConfiguration>();
			}
			if (aliasToBeanNameMap != null && aliasToBeanNameMap.containsKey(beanName))
			{
				throw new IllegalArgumentException("An alias with the name '" + beanName + "' of this bean is already registered in this context");
			}
			if (!beanConfiguration.isOverridesExisting())
			{
				if (!nameToBeanConfMap.putIfNotExists(beanName, beanConfiguration))
				{
					IBeanConfiguration existingBeanConfiguration = nameToBeanConfMap.get(beanName);
					if (!existingBeanConfiguration.isOverridesExisting())
					{
						throw new IllegalArgumentException("A bean with name '" + beanName + "' is already registered in this context");
					}
					// Existing config requests precedence over every other bean config. This is no error
					return;
				}
			}
			else
			{
				// Intentionally put the configuration in the map unaware of an existing entry
				nameToBeanConfMap.put(beanName, beanConfiguration);
			}
		}
		if (beanConfigurations == null)
		{
			beanConfigurations = new ArrayList<IBeanConfiguration>();
		}
		beanConfigurations.add(beanConfiguration);
	}

	@Override
	public void registerAlias(String aliasBeanName, String beanNameToCreateAliasFor)
	{
		if (aliasToBeanNameMap == null)
		{
			aliasToBeanNameMap = new LinkedHashMap<String, String>();
			beanNameToAliasesMap = new LinkedHashMap<String, List<String>>();
		}
		if (!aliasToBeanNameMap.putIfNotExists(aliasBeanName, beanNameToCreateAliasFor))
		{
			throw new IllegalArgumentException("Alias '" + aliasBeanName + "' has been already specified");
		}
		List<String> aliasList = beanNameToAliasesMap.get(beanNameToCreateAliasFor);
		if (aliasList == null)
		{
			aliasList = new ArrayList<String>();
			beanNameToAliasesMap.put(beanNameToCreateAliasFor, aliasList);
		}
		aliasList.add(aliasBeanName);
	}

	@Override
	public IBeanConfiguration registerBean(String beanName, Class<?> beanType)
	{
		ParamChecker.assertParamNotNull(beanName, "beanName");
		if (beanType == null)
		{
			throw new IllegalArgumentException("Parameter must be valid: beanType (" + beanName + ")");
		}
		BeanConfiguration beanConfiguration = new BeanConfiguration(beanType, beanName, proxyFactory, props);

		addBeanConfiguration(beanConfiguration);
		return beanConfiguration;
	}

	@Override
	public IBeanConfiguration registerBean(String beanName, String parentBeanName)
	{
		ParamChecker.assertParamNotNull(beanName, "beanName");
		ParamChecker.assertParamNotNull(parentBeanName, "parentBeanName");
		BeanConfiguration beanConfiguration = new BeanConfiguration(null, beanName, proxyFactory, props);
		beanConfiguration.parent(parentBeanName);

		addBeanConfiguration(beanConfiguration);
		return beanConfiguration;
	}

	@Override
	public IBeanConfiguration registerAnonymousBean(Class<?> beanType)
	{
		ParamChecker.assertParamNotNull(beanType, "beanType");

		BeanConfiguration beanConfiguration = new BeanConfiguration(beanType, generateBeanName(beanType), proxyFactory, props);

		addBeanConfiguration(beanConfiguration);
		return beanConfiguration;
	}

	@Override
	public <I, T extends I> IBeanConfiguration registerAutowireableBean(Class<I> autowiringType, Class<T> beanType)
	{
		ParamChecker.assertParamNotNull(autowiringType, "autowiringType");
		ParamChecker.assertParamNotNull(beanType, "beanType");
		BeanConfiguration beanConfiguration = new BeanConfiguration(beanType, generateBeanName(beanType), proxyFactory, props);
		addBeanConfiguration(beanConfiguration);
		beanConfiguration.autowireable(autowiringType);
		return beanConfiguration;
	}

	@Override
	public IBeanConfiguration registerExternalBean(String beanName, Object externalBean)
	{
		ParamChecker.assertParamNotNull(beanName, "beanName");
		if (externalBean == null)
		{
			throw new IllegalArgumentException("Bean \"" + beanName + "\" was registered with value NULL.");
		}
		BeanInstanceConfiguration beanConfiguration = new BeanInstanceConfiguration(externalBean, beanName, false, props);
		addBeanConfiguration(beanConfiguration);
		return beanConfiguration;
	}

	@Override
	public IBeanConfiguration registerExternalBean(Object externalBean)
	{
		ParamChecker.assertParamNotNull(externalBean, "externalBean");
		return registerExternalBean(generateBeanName(externalBean.getClass()), externalBean);
	}

	@Override
	public IBeanConfiguration registerWithLifecycle(String beanName, Object externalBean)
	{
		ParamChecker.assertParamNotNull(beanName, "beanName");
		ParamChecker.assertParamNotNull(externalBean, "externalBean (" + beanName + ")");
		BeanInstanceConfiguration beanConfiguration = new BeanInstanceConfiguration(externalBean, beanName, true, props);
		addBeanConfiguration(beanConfiguration);
		return beanConfiguration;
	}

	@Override
	public IBeanConfiguration registerWithLifecycle(Object externalBean)
	{
		ParamChecker.assertParamNotNull(externalBean, "externalBean");
		return registerWithLifecycle(generateBeanName(externalBean.getClass()), externalBean);
	}

	@Override
	public void registerDisposable(IDisposable disposable)
	{
		ParamChecker.assertParamNotNull(disposable, "disposable");
		if (disposableObjects == null)
		{
			disposableObjects = new ArrayList<Object>();
		}
		disposableObjects.add(disposable);
	}

	@Override
	public void registerDisposable(IDisposableBean disposableBean)
	{
		ParamChecker.assertParamNotNull(disposableBean, "disposableBean");
		if (disposableObjects == null)
		{
			disposableObjects = new ArrayList<Object>();
		}
		disposableObjects.add(disposableBean);
	}

	@Override
	public ILinkRegistryNeededConfiguration<?> link(String listenerBeanName)
	{
		LinkConfiguration<Object> linkConfiguration = linkController.createLinkConfiguration(listenerBeanName, (String) null);
		addBeanConfiguration(linkConfiguration);
		return linkConfiguration;
	}

	@Override
	public ILinkRegistryNeededConfiguration<?> link(String listenerBeanName, String methodName)
	{
		LinkConfiguration<Object> linkConfiguration = linkController.createLinkConfiguration(listenerBeanName, methodName);
		addBeanConfiguration(linkConfiguration);
		return linkConfiguration;
	}

	@Override
	public ILinkRegistryNeededConfiguration<?> link(IBeanConfiguration listenerBean)
	{
		LinkConfiguration<Object> linkConfiguration = linkController.createLinkConfiguration(listenerBean, null);
		addBeanConfiguration(linkConfiguration);
		return linkConfiguration;
	}

	@Override
	public ILinkRegistryNeededConfiguration<?> link(IBeanConfiguration listenerBean, String methodName)
	{
		LinkConfiguration<Object> linkConfiguration = linkController.createLinkConfiguration(listenerBean, methodName);
		addBeanConfiguration(linkConfiguration);
		return linkConfiguration;
	}

	@Override
	public ILinkRegistryNeededConfiguration<?> link(Object listener, String methodName)
	{
		LinkConfiguration<Object> linkConfiguration = linkController.createLinkConfiguration(listener, methodName);
		addBeanConfiguration(linkConfiguration);
		return linkConfiguration;
	}

	@Override
	public <D> ILinkRegistryNeededConfiguration<D> link(D listener)
	{
		LinkConfiguration<D> linkConfiguration = linkController.createLinkConfiguration(listener);
		addBeanConfiguration(linkConfiguration);
		return linkConfiguration;
	}

	@Override
	public ILinkRegistryNeededRuntime<?> link(IServiceContext serviceContext, String listenerBeanName)
	{
		return linkController.link(serviceContext, listenerBeanName);
	}

	@Override
	public ILinkRegistryNeededRuntime<?> link(IServiceContext serviceContext, String listenerBeanName, String methodName)
	{
		return linkController.link(serviceContext, listenerBeanName, methodName);
	}

	@Override
	public ILinkRegistryNeededRuntime<?> link(IServiceContext serviceContext, IBeanConfiguration listenerBean)
	{
		return linkController.link(serviceContext, listenerBean);
	}

	@Override
	public ILinkRegistryNeededRuntime<?> link(IServiceContext serviceContext, IBeanConfiguration listenerBean, String methodName)
	{
		return linkController.link(serviceContext, listenerBean, methodName);
	}

	@Override
	public ILinkRegistryNeededRuntime<?> link(IServiceContext serviceContext, Object listener, String methodName)
	{
		return linkController.link(serviceContext, listener, methodName);
	}

	@Override
	public <D> ILinkRegistryNeededRuntime<D> link(IServiceContext serviceContext, D listener)
	{
		return linkController.link(serviceContext, listener);
	}

	@Override
	public LinkConfiguration<Object> createLinkConfiguration(String listenerBeanName, String methodName)
	{
		return linkController.createLinkConfiguration(listenerBeanName, methodName);
	}

	@Override
	public LinkConfiguration<Object> createLinkConfiguration(IBeanConfiguration listenerBean, String methodName)
	{
		return linkController.createLinkConfiguration(listenerBean, methodName);
	}

	@Override
	public LinkConfiguration<Object> createLinkConfiguration(Object listener, String methodName)
	{
		return linkController.createLinkConfiguration(listener, methodName);
	}

	@Override
	public <D> LinkConfiguration<D> createLinkConfiguration(D listener)
	{
		return linkController.createLinkConfiguration(listener);
	}

	@Deprecated
	@Override
	public void link(IServiceContext serviceContext, String registryBeanName, String listenerBeanName, Class<?> registryClass)
	{
		linkController.link(serviceContext, registryBeanName, listenerBeanName, registryClass);
	}

	@Deprecated
	@Override
	public void link(IServiceContext serviceContext, String registryBeanName, String listenerBeanName, Class<?> registryClass, Object... arguments)
	{
		linkController.link(serviceContext, registryBeanName, listenerBeanName, registryClass, arguments);
	}

	@Deprecated
	@Override
	public void link(IServiceContext serviceContext, IBeanConfiguration listenerBean, Class<?> autowiredRegistryClass)
	{
		linkController.link(serviceContext, listenerBean, autowiredRegistryClass);
	}

	@Deprecated
	@Override
	public void link(IServiceContext serviceContext, IBeanConfiguration listenerBean, Class<?> autowiredRegistryClass, Object... arguments)
	{
		linkController.link(serviceContext, listenerBean, autowiredRegistryClass, arguments);
	}

	@Deprecated
	@Override
	public void link(IServiceContext serviceContext, String listenerBeanName, Class<?> autowiredRegistryClass)
	{
		linkController.link(serviceContext, listenerBeanName, autowiredRegistryClass);
	}

	@Deprecated
	@Override
	public void link(IServiceContext serviceContext, String listenerBeanName, Class<?> autowiredRegistryClass, Object... arguments)
	{
		linkController.link(serviceContext, listenerBeanName, autowiredRegistryClass, arguments);
	}

	@Deprecated
	@Override
	public IBeanConfiguration createLinkConfiguration(String registryBeanName, String listenerBeanName, Class<?> registryClass)
	{
		return createLinkConfiguration(registryBeanName, listenerBeanName, registryClass, emptyArgs);
	}

	@Deprecated
	@Override
	public IBeanConfiguration createLinkConfiguration(String registryBeanName, String listenerBeanName, Class<?> registryClass, Object... arguments)
	{
		return linkController.createLinkConfiguration(registryBeanName, listenerBeanName, registryClass, arguments);
	}

	@Deprecated
	@Override
	public IBeanConfiguration createLinkConfiguration(String listenerBeanName, Class<?> autowiredRegistryClass)
	{
		return createLinkConfiguration(listenerBeanName, autowiredRegistryClass, emptyArgs);
	}

	@Deprecated
	@Override
	public IBeanConfiguration createLinkConfiguration(String listenerBeanName, Class<?> autowiredRegistryClass, Object... arguments)
	{
		return linkController.createLinkConfiguration(listenerBeanName, autowiredRegistryClass, arguments);
	}

	@Deprecated
	@Override
	public void linkToNamed(String registryBeanName, String listenerBeanName, Class<?> registryClass)
	{
		linkToNamed(registryBeanName, listenerBeanName, registryClass, emptyArgs);
	}

	@Deprecated
	@Override
	public void link(IBeanConfiguration listenerBean, Class<?> autowiredRegistryClass)
	{
		link(listenerBean, autowiredRegistryClass, emptyArgs);
	}

	@Deprecated
	@Override
	public void link(String listenerBeanName, Class<?> autowiredRegistryClass)
	{
		link(listenerBeanName, autowiredRegistryClass, emptyArgs);
	}

	@Deprecated
	@Override
	public void linkToNamed(String registryBeanName, String listenerBeanName, Class<?> registryClass, Object... arguments)
	{
		IBeanConfiguration beanConfiguration = createLinkConfiguration(registryBeanName, listenerBeanName, registryClass, arguments);
		addBeanConfiguration(beanConfiguration);
	}

	@Deprecated
	@Override
	public void link(IBeanConfiguration listenerBean, Class<?> autowiredRegistryClass, Object... arguments)
	{
		ParamChecker.assertParamNotNull(listenerBean, "listenerBean");
		String listenerBeanName = listenerBean.getName();
		ParamChecker.assertParamNotNull(listenerBeanName, "listenerBean.getName()");

		IBeanConfiguration beanConfiguration = createLinkConfiguration(listenerBeanName, autowiredRegistryClass, arguments);
		addBeanConfiguration(beanConfiguration);
	}

	@Deprecated
	@Override
	public void link(String listenerBeanName, Class<?> autowiredRegistryClass, Object... arguments)
	{
		IBeanConfiguration beanConfiguration = createLinkConfiguration(listenerBeanName, autowiredRegistryClass, arguments);
		addBeanConfiguration(beanConfiguration);
	}

	/**
	 * Adds an autowired bean from an existing context as an external bean (without life cycle) to the new context and autowires it to the same interface.
	 * 
	 * @param sourceContext
	 *            Existing context containing the autowired bean.
	 * @param targetContextFactory
	 *            Starting context soon containing the bean.
	 * @param autowireableType
	 *            Interface the bean is and will be autowired to.
	 */
	public static void transfer(IServiceContext sourceContext, IBeanContextFactory targetContextFactory, Class<?> autowireableType)
	{
		ParamChecker.assertParamNotNull(sourceContext, "sourceContext");
		ParamChecker.assertParamNotNull(targetContextFactory, "targetContextFactory");
		ParamChecker.assertParamNotNull(autowireableType, "autowireableType");
		Object bean = sourceContext.getService(autowireableType);
		if (bean == null)
		{
			throw new IllegalArgumentException("No autowired bean found for type " + autowireableType.getName());
		}
		targetContextFactory.registerExternalBean(bean).autowireable(autowireableType);
	}

	/**
	 * Adds multiple autowired beans from an existing context as external beans (without life cycle) to the new context and autowires them to the same
	 * interface.
	 * 
	 * @param sourceContext
	 *            Existing context containing the autowired beans.
	 * @param targetContextFactory
	 *            Starting context soon containing the beans.
	 * @param autowireableTypes
	 *            Interfaces the beans are and will be autowired to.
	 */
	public static void transfer(IServiceContext sourceContext, IBeanContextFactory targetContextFactory, Class<?>... autowireableTypes)
	{
		ParamChecker.assertParamNotNull(sourceContext, "sourceContext");
		ParamChecker.assertParamNotNull(targetContextFactory, "targetContextFactory");
		ParamChecker.assertParamNotNull(autowireableTypes, "autowireableTypes");
		for (int a = autowireableTypes.length; a-- > 0;)
		{
			transfer(sourceContext, targetContextFactory, autowireableTypes[a]);
		}
	}

	/**
	 * Adds a named bean from an existing context as an external bean with the same name (without life cycle) to the new context.
	 * 
	 * @param sourceContext
	 *            Existing context containing the named bean.
	 * @param targetContextFactory
	 *            Starting context soon containing the bean.
	 * @param beanName
	 *            Now and future name of the bean.
	 */
	public static void transfer(IServiceContext sourceContext, IBeanContextFactory targetContextFactory, String beanName)
	{
		ParamChecker.assertParamNotNull(sourceContext, "sourceContext");
		ParamChecker.assertParamNotNull(targetContextFactory, "targetContextFactory");
		ParamChecker.assertParamNotNull(beanName, "beanName");
		Object bean = sourceContext.getService(beanName);
		if (bean == null)
		{
			throw new IllegalArgumentException("No bean found with name '" + beanName + "'");
		}
		targetContextFactory.registerExternalBean(beanName, bean);
	}

	/**
	 * Adds multiple named beans from an existing context as external beans with the same names (without life cycle) to the new context.
	 * 
	 * @param sourceContext
	 *            Existing context containing the named beans.
	 * @param targetContextFactory
	 *            Starting context soon containing the beans.
	 * @param beanNames
	 *            Now and future names of the beans.
	 */
	public static void transfer(IServiceContext sourceContext, IBeanContextFactory targetContextFactory, String... beanNames)
	{
		ParamChecker.assertParamNotNull(sourceContext, "sourceContext");
		ParamChecker.assertParamNotNull(targetContextFactory, "targetContextFactory");
		ParamChecker.assertParamNotNull(beanNames, "beanNames");
		for (int a = beanNames.length; a-- > 0;)
		{
			transfer(sourceContext, targetContextFactory, beanNames[a]);
		}
	}
}
