package de.osthus.ambeth.ioc.factory;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.ILinkedSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.IdentityHashMap;
import de.osthus.ambeth.collections.IdentityHashSet;
import de.osthus.ambeth.collections.IdentityLinkedMap;
import de.osthus.ambeth.collections.IdentityLinkedSet;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.collections.LinkedHashSet;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.IocConfigurationConstants;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.exception.MaskingRuntimeException;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.BeanMonitoringSupport;
import de.osthus.ambeth.ioc.IBeanPostProcessor;
import de.osthus.ambeth.ioc.IBeanPreProcessor;
import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.IPropertyLoadingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.IStartingModule;
import de.osthus.ambeth.ioc.ServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.config.BeanConfiguration;
import de.osthus.ambeth.ioc.config.BeanInstanceConfiguration;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.config.IPropertyConfiguration;
import de.osthus.ambeth.ioc.config.PrecedenceType;
import de.osthus.ambeth.ioc.exception.BeanContextDeclarationException;
import de.osthus.ambeth.ioc.exception.BeanContextInitException;
import de.osthus.ambeth.ioc.hierarchy.SearchType;
import de.osthus.ambeth.ioc.link.AbstractLinkContainer;
import de.osthus.ambeth.ioc.link.ILinkContainer;
import de.osthus.ambeth.ioc.proxy.CallingProxyPostProcessor;
import de.osthus.ambeth.ioc.proxy.Self;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate;
import de.osthus.ambeth.typeinfo.FieldPropertyInfo;
import de.osthus.ambeth.typeinfo.IPropertyInfo;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.ImmutableTypeSet;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.ReflectUtil;
import de.osthus.ambeth.util.StringConversionHelper;
import de.osthus.ambeth.util.SystemUtil;

public class BeanContextInitializer implements IBeanContextInitializer, IInitializingBean
{
	@LogInstance
	private ILogger log;

	protected static final HashSet<Class<?>> primitiveSet = new HashSet<Class<?>>(0.5f);

	protected static final IdentityHashMap<PrecedenceType, Integer> precedenceOrder = new IdentityHashMap<PrecedenceType, Integer>(0.5f);

	// Intentionally no SensitiveThreadLocal. Usage will alLways be cleaned up immediately
	protected static final ThreadLocal<BeanContextInit> currentBeanContextInitTL = new ThreadLocal<BeanContextInit>();

	static
	{
		ImmutableTypeSet.addImmutableTypesTo(primitiveSet);
		primitiveSet.add(Object.class);

		precedenceOrder.put(PrecedenceType.LOWEST, Integer.valueOf(6));
		precedenceOrder.put(PrecedenceType.LOWER, Integer.valueOf(5));
		precedenceOrder.put(PrecedenceType.LOW, Integer.valueOf(4));
		precedenceOrder.put(PrecedenceType.MEDIUM, Integer.valueOf(3));
		precedenceOrder.put(PrecedenceType.DEFAULT, Integer.valueOf(3));
		precedenceOrder.put(PrecedenceType.HIGH, Integer.valueOf(2));
		precedenceOrder.put(PrecedenceType.HIGHER, Integer.valueOf(1));
		precedenceOrder.put(PrecedenceType.HIGHEST, Integer.valueOf(0));
	}

	public static IBeanContextFactory getCurrentBeanContextFactory()
	{
		BeanContextInit beanContextInit = currentBeanContextInitTL.get();
		if (beanContextInit == null)
		{
			return null;
		}
		return beanContextInit.beanContextFactory;
	}

	public static IServiceContext getCurrentBeanContext()
	{
		BeanContextInit beanContextInit = currentBeanContextInitTL.get();
		if (beanContextInit == null)
		{
			return null;
		}
		return beanContextInit.beanContext;
	}

	protected IPropertyInfoProvider propertyInfoProvider;

	protected IThreadLocalObjectCollector objectCollector;

	protected CallingProxyPostProcessor callingProxyPostProcessor;

	protected IConversionHelper conversionHelper;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(callingProxyPostProcessor, "CallingProxyPostProcessor");
		ParamChecker.assertNotNull(propertyInfoProvider, "propertyInfoProvider");
		ParamChecker.assertNotNull(objectCollector, "objectCollector");
		ParamChecker.assertNotNull(conversionHelper, "conversionHelper");
	}

	public void setCallingProxyPostProcessor(CallingProxyPostProcessor callingProxyPostProcessor)
	{
		this.callingProxyPostProcessor = callingProxyPostProcessor;
	}

	public void setPropertyInfoProvider(IPropertyInfoProvider propertyInfoProvider)
	{
		this.propertyInfoProvider = propertyInfoProvider;
	}

	public void setObjectCollector(IThreadLocalObjectCollector objectCollector)
	{
		this.objectCollector = objectCollector;
	}

	public void setConversionHelper(IConversionHelper conversionHelper)
	{
		this.conversionHelper = conversionHelper;
	}

	protected int getBeanConfigurationAmount(BeanContextInit beanContextInit)
	{
		List<IBeanConfiguration> beanConfigurations = beanContextInit.beanContextFactory.getBeanConfigurations();
		if (beanConfigurations == null)
		{
			return 0;
		}
		return beanConfigurations.size();
	}

	@Override
	public void initializeBeanContext(ServiceContext beanContext, BeanContextFactory beanContextFactory)
	{
		beanContext.setBeanContextFactory(beanContextFactory);

		if (beanContextFactory.getBeanConfigurations() == null)
		{
			beanContext.setBeanContextFactory(beanContextFactory);
			return;
		}
		IdentityLinkedMap<Object, IBeanConfiguration> objectToBeanConfigurationMap = new IdentityLinkedMap<Object, IBeanConfiguration>();
		IdentityHashMap<Object, IBeanConfiguration> objectToHandledBeanConfigurationMap = new IdentityHashMap<Object, IBeanConfiguration>();
		LinkedHashMap<String, IBeanConfiguration> nameToBeanConfigurationMap = new LinkedHashMap<String, IBeanConfiguration>();
		IdentityLinkedSet<Object> allLifeCycledBeansSet = new IdentityLinkedSet<Object>();
		IdentityHashSet<IBeanConfiguration> alreadyHandledConfigsSet = new IdentityHashSet<IBeanConfiguration>();
		ArrayList<Object> initializedOrdering = new ArrayList<Object>();
		BeanContextInit beanContextInit = new BeanContextInit();
		BeanContextInit oldBeanContextInit = currentBeanContextInitTL.get();
		try
		{
			currentBeanContextInitTL.set(beanContextInit);
			beanContextInit.beanContext = beanContext;
			beanContextInit.beanContextFactory = beanContextFactory;
			beanContextInit.objectToBeanConfigurationMap = objectToBeanConfigurationMap;
			beanContextInit.objectToHandledBeanConfigurationMap = objectToHandledBeanConfigurationMap;
			beanContextInit.allLifeCycledBeansSet = allLifeCycledBeansSet;
			beanContextInit.initializedOrdering = initializedOrdering;

			Properties contextProps = beanContextFactory.getProperties();
			beanContextInit.properties = contextProps;

			beanContextFactory.registerExternalBean("properties", contextProps).autowireable(IProperties.class, Properties.class);

			Object priorityBean;
			do
			{
				priorityBean = null;
				int highestPriority = 0;

				instantiateBeans(beanContextInit, nameToBeanConfigurationMap, alreadyHandledConfigsSet, true);

				for (Entry<Object, IBeanConfiguration> entry : objectToBeanConfigurationMap)
				{
					Object bean = entry.getKey();

					int priorityOfBean = getPriorityOfBean(bean.getClass());
					if (priorityOfBean > highestPriority)
					{
						highestPriority = priorityOfBean;
						priorityBean = bean;
						continue;
					}
				}
				if (priorityBean != null)
				{
					initializeBean(beanContextInit, priorityBean);
				}
			}
			while (priorityBean != null);

			while (true)
			{
				int beanConfigurationCountBefore = getBeanConfigurationAmount(beanContextInit);
				instantiateBeans(beanContextInit, nameToBeanConfigurationMap, alreadyHandledConfigsSet, false);

				// Now load properties-service from the current context (it may be
				// another)
				beanContextInit.properties = beanContext.getService(Properties.class, false);

				resolveBeansInSequence(beanContextInit);
				int beanConfigurationCountAfter = getBeanConfigurationAmount(beanContextInit);
				if (beanConfigurationCountAfter == beanConfigurationCountBefore)
				{
					break;
				}
			}
			checkIfAllBeanConfigsAreHandledCorrectly(beanContextInit, alreadyHandledConfigsSet);

			// Notify first all modules that this context is now ready
			for (int a = 0, size = initializedOrdering.size(); a < size; a++)
			{
				Object bean = initializedOrdering.get(a);
				if (bean instanceof IStartingModule)
				{
					((IStartingModule) bean).afterStarted(beanContext);
				}
			}
			// Then notify all link containers that this context is now ready for
			// linking
			for (int a = 0, size = initializedOrdering.size(); a < size; a++)
			{
				Object bean = initializedOrdering.get(a);
				if (bean instanceof ILinkContainer)
				{
					ILinkContainer linkContainer = (ILinkContainer) bean;

					try
					{
						linkContainer.link();
					}
					catch (Throwable e)
					{
						throw maskBeanBasedException(e, beanContextInit, objectToHandledBeanConfigurationMap.get(linkContainer), null, linkContainer);
					}
				}
			}
			List<ILinkContainer> linkContainers = beanContext.getLinkContainers();
			if (linkContainers != null)
			{
				for (int a = 0, size = linkContainers.size(); a < size; a++)
				{
					ILinkContainer linkContainer = linkContainers.get(a);
					if (allLifeCycledBeansSet.contains(linkContainer))
					{
						// Nothing to do because this container has already been handled some lines before
						continue;
					}
					linkContainer.link();
				}
			}
			beanContext.setRunning();
			// Then notify all "normal" beans that this context is now ready
			for (int a = 0, size = initializedOrdering.size(); a < size; a++)
			{
				Object bean = initializedOrdering.get(a);
				if (bean instanceof IStartingBean)
				{
					((IStartingBean) bean).afterStarted();
				}
			}
			publishMonitorableBeans(beanContextInit, initializedOrdering);
		}
		catch (Throwable e)
		{
			ArrayList<IDisposableBean> toDestroyOnError = beanContextInit.toDestroyOnError;
			for (int a = 0, size = toDestroyOnError.size(); a < size; a++)
			{
				try
				{
					toDestroyOnError.get(a).destroy();
				}
				catch (Throwable ex)
				{
					throw RuntimeExceptionUtil.mask(ex);
				}
			}
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			if (oldBeanContextInit == null)
			{
				currentBeanContextInitTL.remove();
			}
			else
			{
				currentBeanContextInitTL.set(oldBeanContextInit);
			}
		}
	}

	protected StringBuilder convertBeanContextName(String beanContextName)
	{
		String[] split = beanContextName.split(Pattern.quote("/"));
		StringBuilder sb = new StringBuilder();
		sb.append("de.osthus.ambeth:module=ioc,context=");
		for (int b = 0, sizeB = split.length; b < sizeB; b++)
		{
			if (b > 0)
			{
				sb.append(",context");
				sb.append(b);
				sb.append("=");
			}
			sb.append(split[b]);
		}
		sb.append(",name=");
		return sb;
	}

	protected ObjectName createMonitoringNameOfBean(StringBuilder beanContextName, IBeanConfiguration beanConfiguration)
	{
		int oldLength = beanContextName.length();
		beanContextName.append(beanConfiguration.getName());
		try
		{
			return new ObjectName(beanContextName.toString());
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			beanContextName.setLength(oldLength);
		}
	}

	protected void publishMonitorableBeans(BeanContextInit beanContextInit, List<Object> initializedOrdering)
	{
		boolean monitorBeansActive = Boolean.parseBoolean(beanContextInit.properties.getString(IocConfigurationConstants.MonitorBeansActive, "true"));
		if (!monitorBeansActive)
		{
			return;
		}
		IPropertyInfoProvider propertyInfoProvider = beanContextInit.beanContext.getService(IPropertyInfoProvider.class, false);
		if (propertyInfoProvider == null)
		{
			return;
		}
		IServiceContext beanContext = beanContextInit.beanContext;
		IdentityHashMap<Object, IBeanConfiguration> objectToHandledBeanConfigurationMap = beanContextInit.objectToHandledBeanConfigurationMap;
		final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		if (mbs == null)
		{
			// JMX not activated
			return;
		}
		final List<ObjectName> mBeans = new ArrayList<ObjectName>();
		boolean success = false;
		try
		{
			StringBuilder beanContextName = convertBeanContextName(beanContext.getName());
			for (int a = 0, size = initializedOrdering.size(); a < size; a++)
			{
				final Object bean = initializedOrdering.get(a);
				IBeanConfiguration beanConfiguration = objectToHandledBeanConfigurationMap.get(bean);
				if (beanConfiguration == null || beanConfiguration.getName() == null)
				{
					// beans without a name will not be browsable
					continue;
				}
				BeanMonitoringSupport mBean = new BeanMonitoringSupport(bean, beanContext);
				if (mBean.getMBeanInfo().getAttributes().length == 0)
				{
					continue;
				}
				try
				{
					ObjectName name = createMonitoringNameOfBean(beanContextName, beanConfiguration);
					mbs.registerMBean(mBean, name);
					mBeans.add(name);
				}
				catch (Throwable e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
			}
			success = true;
		}
		finally
		{
			if (!success)
			{
				for (int a = mBeans.size(); a-- > 0;)
				{
					ObjectName name = mBeans.get(a);
					try
					{
						mbs.unregisterMBean(name);
					}
					catch (Throwable e)
					{
						throw RuntimeExceptionUtil.mask(e);
					}
				}
			}
		}
		beanContext.registerDisposeHook(new IBackgroundWorkerParamDelegate<IServiceContext>()
		{
			@Override
			public void invoke(IServiceContext beanContext) throws Throwable
			{
				for (int a = mBeans.size(); a-- > 0;)
				{
					ObjectName name = mBeans.get(a);
					mbs.unregisterMBean(name);
				}
			}
		});
	}

	protected void resolveBeansInSequence(BeanContextInit beanContextInit)
	{
		ILinkedMap<Object, IBeanConfiguration> objectToBeanConfigurationMap = beanContextInit.objectToBeanConfigurationMap;

		while (objectToBeanConfigurationMap.size() > 0)
		{
			for (Entry<Object, IBeanConfiguration> entry : objectToBeanConfigurationMap)
			{
				Object bean = entry.getKey();

				initializeBean(beanContextInit, bean);
				break;
			}
		}
	}

	protected void checkIfAllBeanConfigsAreHandledCorrectly(BeanContextInit beanContextInit, Set<IBeanConfiguration> alreadyHandledConfigsSet)
	{
		BeanContextFactory beanContextFactory = beanContextInit.beanContextFactory;
		// ServiceContext beanContext = beanContextInit.beanContext;
		List<IBeanConfiguration> basicBeanConfigurations = beanContextFactory.getBeanConfigurations();
		if (basicBeanConfigurations != null)
		{
			for (int a = basicBeanConfigurations.size(); a-- > 0;)
			{
				IBeanConfiguration beanConfiguration = basicBeanConfigurations.get(a);
				if (alreadyHandledConfigsSet.contains(beanConfiguration))
				{
					continue;
				}
				List<IBeanConfiguration> hierarchy = new ArrayList<IBeanConfiguration>();
				String missingBeanName = fillParentHierarchyIfValid(beanContextInit, beanConfiguration, hierarchy);

				throw maskBeanBasedException("Parent bean definition '" + missingBeanName + "' not found", beanConfiguration, null);
			}
		}
	}

	protected void initializeAutowiring(BeanContextInit beanContextInit, IBeanConfiguration beanConfiguration, Object bean, Class<?> beanType,
			IPropertyInfo[] propertyInfos, Set<String> alreadySpecifiedPropertyNamesSet, Set<String> ignoredPropertyNamesSet)
	{
		boolean highPriorityBean = isHighPriorityBean(bean);
		for (IPropertyInfo prop : propertyInfos)
		{
			String propertyName = prop.getName();
			if (!prop.isWritable() || alreadySpecifiedPropertyNamesSet.contains(propertyName))
			{
				// Property already explicitly specified. No
				// auto-wiring necessary here
				continue;
			}
			if (prop.isAnnotationPresent(Self.class))
			{
				// Self-annotated properties are not considered for auto-wiring
				continue;
			}
			if (ignoredPropertyNamesSet.contains(propertyName))
			{
				// Property marked as ignored. No auto-wiring wanted here
				continue;
			}
			Class<?> propertyType = prop.getPropertyType();
			if (primitiveSet.contains(propertyType) || propertyType.isArray() && primitiveSet.contains(propertyType.getComponentType()))
			{
				continue;
			}
			Autowired autowired = prop.getAnnotation(Autowired.class);
			if (autowired == null && prop instanceof FieldPropertyInfo)
			{
				// Handle fields only if they are explicitly annotated
				continue;
			}
			String beanName = autowired != null ? autowired.value() : null;
			if (beanName != null && beanName.length() == 0)
			{
				beanName = null;
			}
			Object refBean = resolveBean(beanName, propertyType, highPriorityBean, beanContextInit);
			if (refBean == null)
			{
				if (autowired != null && !autowired.optional())
				{
					throw maskBeanBasedException("Could not resolve mandatory autowiring constraint on property '" + prop.getName() + "' of type '"
							+ propertyType.getName() + "'", beanConfiguration, null);
				}
				continue;
			}
			prop.setValue(bean, refBean);
		}
	}

	protected Object resolveBean(String beanName, Class<?> propertyType, boolean isHighPriorityBean, BeanContextInit beanContextInit)
	{
		ServiceContext beanContext = beanContextInit.beanContext;
		ILinkedMap<Object, IBeanConfiguration> objectToBeanConfigurationMap = beanContextInit.objectToBeanConfigurationMap;
		// Module beans are only allowed to demand beans from the parent
		// context

		Object refBean = beanName != null ? beanContext.getDirectBean(beanName) : beanContext.getDirectBean(propertyType);
		if (refBean != null && objectToBeanConfigurationMap != null && objectToBeanConfigurationMap.containsKey(refBean))
		{
			initializeBean(beanContextInit, refBean);
		}
		if (beanName != null)
		{
			return beanContext.getServiceIntern(beanName, propertyType, isHighPriorityBean ? SearchType.PARENT : SearchType.CASCADE);
		}
		return beanContext.getServiceIntern(propertyType, isHighPriorityBean ? SearchType.PARENT : SearchType.CASCADE);
	}

	protected void callInitializingCallbacks(BeanContextInit beanContextInit, Object bean, boolean joinLifecycle)
	{
		ServiceContext beanContext = beanContextInit.beanContext;
		List<Object> initializedOrdering = beanContextInit.initializedOrdering;

		try
		{
			if (bean instanceof IInitializingBean)
			{
				((IInitializingBean) bean).afterPropertiesSet();
				if (bean instanceof IDisposableBean)
				{
					beanContextInit.toDestroyOnError.add((IDisposableBean) bean);
				}
			}
			if (bean instanceof IPropertyLoadingBean)
			{
				((IPropertyLoadingBean) bean).applyProperties(beanContextInit.properties);
			}
			if (bean instanceof IInitializingModule)
			{
				((IInitializingModule) bean).afterPropertiesSet(beanContextInit.beanContextFactory);
				if (bean instanceof IDisposableBean && !(bean instanceof IInitializingBean))
				{
					// it is a module (and only a module) so it has not been added yet
					beanContextInit.toDestroyOnError.add((IDisposableBean) bean);
				}
			}
			if (bean instanceof IBeanPreProcessor)
			{
				beanContext.addPreProcessor((IBeanPreProcessor) bean);
			}
			if (bean instanceof IBeanPostProcessor)
			{
				beanContext.addPostProcessor((IBeanPostProcessor) bean);
			}
			if (bean instanceof ILinkContainer)
			{
				beanContext.addLinkContainer((ILinkContainer) bean);
				if (beanContext.isRunning())
				{
					((ILinkContainer) bean).link();
				}
			}
			if (joinLifecycle && bean instanceof IDisposableBean)
			{
				beanContext.registerDisposable((IDisposableBean) bean);
			}
			if (initializedOrdering != null)
			{
				initializedOrdering.add(bean);
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public Object initializeBean(ServiceContext beanContext, BeanContextFactory beanContextFactory, IBeanConfiguration beanConfiguration, Object bean,
			List<IBeanConfiguration> beanConfHierarchy, boolean joinLifecycle)
	{
		BeanContextInit currentBeanContextInit = currentBeanContextInitTL.get();
		if (currentBeanContextInit == null)
		{
			currentBeanContextInit = new BeanContextInit();
			currentBeanContextInit.beanContext = beanContext;
			currentBeanContextInit.beanContextFactory = beanContextFactory;
			currentBeanContextInit.properties = beanContext.getService(Properties.class);
			currentBeanContextInit.objectToBeanConfigurationMap = new IdentityLinkedMap<Object, IBeanConfiguration>();
			currentBeanContextInit.objectToHandledBeanConfigurationMap = new IdentityHashMap<Object, IBeanConfiguration>();
		}
		initializeBean(currentBeanContextInit, beanConfiguration, bean, beanConfHierarchy, joinLifecycle);
		if (joinLifecycle && bean instanceof IStartingBean)
		{
			try
			{
				((IStartingBean) bean).afterStarted();
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		return postProcessBean(currentBeanContextInit, beanConfiguration, beanConfiguration.getBeanType(), bean, beanConfHierarchy);
	}

	public void initializeBean(BeanContextInit beanContextInit, Object bean)
	{
		IBeanConfiguration beanConfiguration = beanContextInit.objectToBeanConfigurationMap.remove(bean);
		beanContextInit.objectToHandledBeanConfigurationMap.put(bean, beanConfiguration);
		ILinkedSet<Object> allLifeCycledBeansSet = beanContextInit.allLifeCycledBeansSet;

		ArrayList<IBeanConfiguration> beanConfHierarchy = new ArrayList<IBeanConfiguration>(3);
		if (fillParentHierarchyIfValid(beanContextInit, beanConfiguration, beanConfHierarchy) != null)
		{
			throw maskBeanBasedException("Must never happen at this point", beanConfiguration, null);
		}
		allLifeCycledBeansSet.add(bean);

		initializeBean(beanContextInit, beanConfiguration, bean, beanConfHierarchy, true);
	}

	public void initializeBean(BeanContextInit beanContextInit, IBeanConfiguration beanConfiguration, Object bean, List<IBeanConfiguration> beanConfHierarchy,
			boolean joinLifecycle)
	{
		if (!(bean instanceof IInitializingModule) && !beanConfiguration.isWithLifecycle())
		{
			if (bean instanceof IPropertyLoadingBean)
			{
				((IPropertyLoadingBean) bean).applyProperties(beanContextInit.properties);
			}
			return;
		}
		ServiceContext beanContext = beanContextInit.beanContext;
		BeanContextFactory beanContextFactory = beanContextInit.beanContextFactory;
		List<IBeanPreProcessor> preProcessors = beanContext.getPreProcessors();

		ArrayList<IPropertyConfiguration> propertyConfigurations = new ArrayList<IPropertyConfiguration>();
		LinkedHashSet<String> ignoredPropertyNames = new LinkedHashSet<String>();
		HashSet<String> alreadySpecifiedPropertyNamesSet = new HashSet<String>();

		try
		{
			Class<?> beanType = resolveTypeInHierarchy(beanConfHierarchy);
			resolveAllBeanConfInHierarchy(beanConfHierarchy, propertyConfigurations);

			IPropertyInfo[] propertyInfos = propertyInfoProvider.getProperties(beanType);

			if (preProcessors != null)
			{
				String beanName = beanConfiguration.getName();
				Properties properties = beanContextInit.properties;
				for (int a = 0, size = preProcessors.size(); a < size; a++)
				{
					IBeanPreProcessor preProcessor = preProcessors.get(a);
					preProcessor.preProcessProperties(beanContextFactory, properties, beanName, bean, beanType, propertyConfigurations, propertyInfos);
				}
			}
			initializeDefining(beanContextInit, beanConfiguration, bean, beanType, propertyInfos, propertyConfigurations, alreadySpecifiedPropertyNamesSet);
			resolveAllIgnoredPropertiesInHierarchy(beanConfHierarchy, beanType, ignoredPropertyNames);

			initializeAutowiring(beanContextInit, beanConfiguration, bean, beanType, propertyInfos, alreadySpecifiedPropertyNamesSet, ignoredPropertyNames);
			callInitializingCallbacks(beanContextInit, bean, joinLifecycle);
		}
		catch (Throwable e)
		{
			throw maskBeanBasedException(e, beanContextInit, beanConfiguration, null, bean);
		}
	}

	protected Throwable createBeanContextDeclarationExceptionIfPossible(Throwable e, IBeanConfiguration beanConfiguration,
			IPropertyConfiguration propertyConfiguration)
	{
		if (e instanceof BeanContextDeclarationException || e instanceof BeanContextInitException)
		{
			return e;
		}
		StackTraceElement[] declarationStackTrace = null;
		if (propertyConfiguration != null)
		{
			declarationStackTrace = propertyConfiguration.getDeclarationStackTrace();
			if (declarationStackTrace == null)
			{
				declarationStackTrace = propertyConfiguration.getBeanConfiguration().getDeclarationStackTrace();
			}
		}
		if (declarationStackTrace == null && beanConfiguration != null)
		{
			declarationStackTrace = beanConfiguration.getDeclarationStackTrace();
		}
		if (declarationStackTrace == null)
		{
			return e;
		}
		if (e != null)
		{
			while (e instanceof MaskingRuntimeException && e.getMessage() == null)
			{
				e = e.getCause();
			}
			return new BeanContextDeclarationException(declarationStackTrace, e);
		}
		return new BeanContextDeclarationException(declarationStackTrace);
	}

	protected RuntimeException maskBeanBasedException(Throwable e, BeanContextInit beanContextInit, IBeanConfiguration beanConfiguration,
			IPropertyConfiguration propertyConfiguration, Object bean)
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
		try
		{
			Class<?> beanType = null;
			if (bean != null)
			{
				beanType = bean.getClass();
			}
			else
			{
				ArrayList<IBeanConfiguration> beanConfHierarchy = new ArrayList<IBeanConfiguration>();
				fillParentHierarchyIfValid(beanContextInit, beanConfiguration, beanConfHierarchy);
				beanType = resolveTypeInHierarchy(beanConfHierarchy);
			}
			if (beanType != null && AbstractLinkContainer.class.isAssignableFrom(beanType))
			{
				sb.append("Error occured while executing link operation");
			}
			else if (beanConfiguration.getName() == null)
			{
				sb.append("Error occured while handling anonymous bean of type ").append(beanType != null ? beanType.getName() : "<unknown>");
			}
			else
			{
				sb.append("Error occured while handling bean '").append(beanConfiguration.getName()).append("' of type ")
						.append(beanType != null ? beanType.getName() : "<unknown>");
			}
			return maskBeanBasedException(sb, e, beanConfiguration, propertyConfiguration);
		}
		finally
		{
			tlObjectCollector.dispose(sb);
		}
	}

	protected RuntimeException maskBeanBasedException(CharSequence message, IBeanConfiguration beanConfiguration, IPropertyConfiguration propertyConfiguration)
	{
		return maskBeanBasedException(message, null, beanConfiguration, propertyConfiguration);
	}

	protected RuntimeException maskBeanBasedException(CharSequence message, Throwable e, IBeanConfiguration beanConfiguration,
			IPropertyConfiguration propertyConfiguration)
	{
		e = createBeanContextDeclarationExceptionIfPossible(e, beanConfiguration, propertyConfiguration);

		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
		try
		{
			sb.append(message);
			if (!(e instanceof BeanContextInitException))
			{
				BeanContextInitException beanContextInitException = new BeanContextInitException(sb.toString(), e);
				beanContextInitException.setStackTrace(RuntimeExceptionUtil.EMPTY_STACK_TRACE);
				return beanContextInitException;
			}
			sb.insert(0, SystemUtil.lineSeparator());
			sb.insert(0, e.getMessage());
			BeanContextInitException beanContextInitException = new BeanContextInitException(sb.toString(), e.getCause());
			beanContextInitException.setStackTrace(e.getStackTrace());
			return beanContextInitException;
		}
		finally
		{
			tlObjectCollector.dispose(sb);
		}
	}

	protected IPropertyInfo autoResolveProperty(Class<?> beanType, IPropertyConfiguration propertyConf, Set<String> alreadySpecifiedPropertyNamesSet)
	{
		return autoResolveAndSetPropertyIntern(null, beanType, null, propertyConf, null, null, alreadySpecifiedPropertyNamesSet);
	}

	protected void autoResolveAndSetProperties(Object bean, Class<?> beanType, IPropertyInfo[] properties, IPropertyConfiguration propertyConf,
			String beanName, Object refBean, Set<String> alreadySpecifiedPropertyNamesSet)
	{
		autoResolveAndSetPropertyIntern(bean, beanType, properties, propertyConf, beanName, refBean, alreadySpecifiedPropertyNamesSet);
	}

	protected IPropertyInfo autoResolveAndSetPropertyIntern(Object bean, Class<?> beanType, IPropertyInfo[] properties, IPropertyConfiguration propertyConf,
			String beanName, Object refBean, Set<String> alreadySpecifiedPropertyNamesSet)
	{
		String propertyName = propertyConf.getPropertyName();
		if (propertyName != null)
		{
			IPropertyInfo property = propertyInfoProvider.getProperty(beanType, propertyName);
			if (property == null)
			{
				property = propertyInfoProvider.getProperty(beanType, StringConversionHelper.upperCaseFirst(objectCollector, propertyName));
				if (property == null)
				{
					Field[] fields = ReflectUtil.getDeclaredFieldInHierarchy(beanType, propertyName);
					if (fields.length == 0)
					{
						throw maskBeanBasedException("Bean property " + beanType.getName() + "." + propertyName + " not found", null, propertyConf);
					}
					return new FieldPropertyInfo(beanType, propertyInfoProvider.getPropertyNameFor(fields[0]), fields[0], null);
				}
			}
			return property;
		}
		Class<?> refBeanClass = refBean.getClass();
		boolean atLeastOnePropertyFound = false;
		// Autoresolve property name by type of the requested bean
		for (IPropertyInfo property : properties)
		{
			if (!property.isWritable())
			{
				continue;
			}
			if (alreadySpecifiedPropertyNamesSet.contains(property.getName()))
			{
				// Ignore all already handled properties for potential
				// autoresolving
				continue;
			}
			if (!property.getPropertyType().isAssignableFrom(refBeanClass))
			{
				continue;
			}
			// At this point the property WILL match and we intend to see this
			// as property found
			// even if it has already been matched (and done) by another
			// propertyRef-definition before
			try
			{
				property.setValue(bean, refBean);
				atLeastOnePropertyFound = true;
			}
			catch (Throwable e)
			{
				throw maskBeanBasedException("Propertyrefs did not work on type \"" + beanType + "\". Tried to set refbean \"" + refBean + "\" of type: \""
						+ refBeanClass + "\"", e, null, propertyConf);
			}
			alreadySpecifiedPropertyNamesSet.add(property.getName());
		}
		if (!atLeastOnePropertyFound)
		{
			Class<?> currType = beanType;
			while (currType != Object.class)
			{
				Field[] fields = ReflectUtil.getDeclaredFields(currType);
				for (Field field : fields)
				{
					String fieldName = field.getName();
					if (alreadySpecifiedPropertyNamesSet.contains(fieldName))
					{
						// Ignore all already handled properties for potential
						// autoresolving
						continue;
					}
					if (!field.getType().isAssignableFrom(refBeanClass))
					{
						continue;
					}
					try
					{
						field.set(bean, refBean);
						atLeastOnePropertyFound = true;
					}
					catch (Throwable e)
					{
						throw maskBeanBasedException("Propertyrefs did not work on type \"" + beanType + "\". Tried to set refbean \"" + refBean
								+ "\" of type: \"" + refBeanClass + "\"", e, null, propertyConf);
					}
					alreadySpecifiedPropertyNamesSet.add(fieldName);
				}
				currType = currType.getSuperclass();
			}
		}
		if (!atLeastOnePropertyFound)
		{
			throw maskBeanBasedException(
					"Impossible autoresolve property scenario: There is no property which accepts a bean of type " + refBeanClass.getName()
							+ "' as represented by bean name '" + beanName + "'", null, propertyConf);
		}
		return null;
	}

	protected void initializeDefining(BeanContextInit beanContextInit, IBeanConfiguration beanConfiguration, Object bean, Class<?> beanType,
			IPropertyInfo[] propertyInfos, List<IPropertyConfiguration> propertyConfigurations, Set<String> alreadySpecifiedPropertyNamesSet)
	{
		for (int a = propertyConfigurations.size(); a-- > 0;)
		{
			IPropertyConfiguration propertyConf = propertyConfigurations.get(a);

			String refBeanName = propertyConf.getBeanName();
			if (refBeanName == null)
			{
				initializePrimitive(beanContextInit, bean, beanType, propertyConf, alreadySpecifiedPropertyNamesSet);
				continue;
			}
			initializeRelation(beanContextInit, beanConfiguration, bean, beanType, propertyConf, propertyInfos, alreadySpecifiedPropertyNamesSet);
		}
	}

	protected void initializePrimitive(BeanContextInit beanContextInit, Object bean, Class<?> beanType, IPropertyConfiguration propertyConf,
			Set<String> alreadySpecifiedPropertyNamesSet)
	{
		Object value = propertyConf.getValue();
		IProperties properties = beanContextInit.properties;

		if (value instanceof String)
		{
			value = properties.resolvePropertyParts((String) value);

			if (value == null)
			{
				throw maskBeanBasedException("Environmental property '" + propertyConf.getValue() + "' could not be resolved while configuring bean property '"
						+ propertyConf.getPropertyName() + "'", null, propertyConf);
			}
		}
		IPropertyInfo primitiveProperty = autoResolveProperty(beanType, propertyConf, alreadySpecifiedPropertyNamesSet);

		Object convertedValue = conversionHelper.convertValueToType(primitiveProperty.getPropertyType(), value);

		if (!alreadySpecifiedPropertyNamesSet.add(propertyConf.getPropertyName()))
		{
			log.debug("Property '" + propertyConf.getPropertyName()
					+ "' already specified by higher priorized configuration. Ignoring setting property with value '" + convertedValue + "'");
			return;
		}
		primitiveProperty.setValue(bean, convertedValue);
	}

	protected boolean isHighPriorityBean(Object bean)
	{
		return isHighPriorityBean(bean.getClass());
	}

	protected boolean isHighPriorityBean(Class<?> beanType)
	{
		return getPriorityOfBean(beanType) != 0;
	}

	protected int getPriorityOfBean(Class<?> beanType)
	{
		if (IPropertyLoadingBean.class.isAssignableFrom(beanType))
		{
			return 3;
		}
		else if (IBeanPreProcessor.class.isAssignableFrom(beanType) || IBeanPostProcessor.class.isAssignableFrom(beanType))
		{
			return 2;
		}
		else if (IInitializingModule.class.isAssignableFrom(beanType))
		{
			return 1;
		}
		return 0;
	}

	protected void initializeRelation(BeanContextInit beanContextInit, IBeanConfiguration beanConfiguration, Object bean, Class<?> beanType,
			IPropertyConfiguration propertyConf, IPropertyInfo[] propertyInfos, Set<String> alreadySpecifiedPropertyNamesSet)
	{
		ServiceContext beanContext = beanContextInit.beanContext;
		ILinkedMap<Object, IBeanConfiguration> objectToBeanConfigurationMap = beanContextInit.objectToBeanConfigurationMap;

		String refBeanName = propertyConf.getBeanName();

		// Module beans are only allowed to demand beans from the parent
		// context
		Object refBean = beanContext.getDirectBean(refBeanName);
		if (refBean != null && objectToBeanConfigurationMap != null && objectToBeanConfigurationMap.containsKey(refBean))
		{
			initializeBean(beanContextInit, refBean);
		}
		refBean = beanContext.getServiceIntern(refBeanName, Object.class, isHighPriorityBean(bean) ? SearchType.PARENT : SearchType.CASCADE);
		if (refBean == null)
		{
			if (propertyConf.isOptional())
			{
				return;
			}
			String message;
			if (propertyConf.getPropertyName() != null)
			{
				message = "Bean '" + refBeanName + "' not found to set bean property '" + propertyConf.getPropertyName() + "'";
			}
			else
			{
				message = "Bean '" + refBeanName + "' not found to look for autoresolve property";
			}
			throw maskBeanBasedException(message, beanConfiguration, propertyConf);
		}
		IBeanConfiguration refBeanConfiguration = beanContextInit.objectToBeanConfigurationMap.get(refBean);
		if (refBeanConfiguration != null)
		{
			// Object is not yet initialized. We try to do this before we use it
			initializeBean(beanContextInit, refBean);
		}
		if (propertyConf.getPropertyName() == null)
		{
			autoResolveAndSetProperties(bean, beanType, propertyInfos, propertyConf, refBeanName, refBean, alreadySpecifiedPropertyNamesSet);
			return;
		}
		IPropertyInfo refProperty = autoResolveProperty(beanType, propertyConf, alreadySpecifiedPropertyNamesSet);

		if (!alreadySpecifiedPropertyNamesSet.add(refProperty.getName()))
		{
			log.debug("Property '" + refProperty.getName()
					+ "' already specified by higher priorized configuration. Ignoring setting property with ref to bean '" + refBeanName + "'");
			return;
		}
		if (!refProperty.getPropertyType().isAssignableFrom(refBean.getClass()))
		{
			throw maskBeanBasedException("Impossible property scenario: Property '" + propertyConf.getPropertyName() + "' does not accept a bean of type '"
					+ refBean.getClass().getName() + "' as represented by bean name '" + refBeanName + "'", beanConfiguration, propertyConf);
		}
		refProperty.setValue(bean, refBean);
	}

	protected void resolveAllBeanConfInHierarchy(List<IBeanConfiguration> beanConfigurations, List<IPropertyConfiguration> propertyConfs)
	{
		for (int a = 0, size = beanConfigurations.size(); a < size; a++)
		{
			IBeanConfiguration beanConfiguration = beanConfigurations.get(a);
			List<IPropertyConfiguration> propertyConfigurations = beanConfiguration.getPropertyConfigurations();
			if (propertyConfigurations != null)
			{
				propertyConfs.addAll(propertyConfigurations);
			}
		}
	}

	protected void instantiateBeans(BeanContextInit beanContextInit, IMap<String, IBeanConfiguration> nameToBeanConfigurationMap,
			Set<IBeanConfiguration> alreadyHandledConfigsSet, boolean highPriorityOnly)
	{
		BeanContextFactory beanContextFactory = beanContextInit.beanContextFactory;
		List<IBeanConfiguration> beanConfigurations = beanContextFactory.getBeanConfigurations();
		if (beanConfigurations == null || beanConfigurations.size() == 0)
		{
			return;
		}
		ServiceContext beanContext = beanContextInit.beanContext;
		ILinkedMap<Object, IBeanConfiguration> objectToBeanConfigurationMap = beanContextInit.objectToBeanConfigurationMap;

		HashMap<Integer, OrderState> orderToHighBeanConfigurations = new HashMap<Integer, OrderState>();
		HashMap<Integer, OrderState> orderToLowBeanConfigurations = new HashMap<Integer, OrderState>();

		sortBeanConfigurations(beanContextInit, beanConfigurations, alreadyHandledConfigsSet, orderToHighBeanConfigurations, orderToLowBeanConfigurations,
				highPriorityOnly);

		ArrayList<IBeanConfiguration> beanConfHierarchy = new ArrayList<IBeanConfiguration>();

		boolean atLeastOneHandled = true;
		while (atLeastOneHandled)
		{
			atLeastOneHandled = false;

			while (true)
			{
				BeanConfigState beanConfigState = resolveNextPrecedenceBean(beanContextInit, orderToHighBeanConfigurations, orderToLowBeanConfigurations,
						highPriorityOnly);
				if (beanConfigState == null)
				{
					break;
				}
				IBeanConfiguration beanConfiguration = beanConfigState.getBeanConfiguration();
				Class<?> beanType = beanConfigState.getBeanType();
				Object bean = null;
				try
				{
					if (beanConfiguration instanceof BeanConfiguration)
					{
						bean = beanConfiguration.getInstance(beanType);
					}
					else if (beanConfiguration instanceof BeanInstanceConfiguration)
					{
						bean = beanConfiguration.getInstance();
					}
					else
					{
						throw maskBeanBasedException("Instance of '" + beanConfiguration.getClass() + "' not supported here", beanConfiguration, null);
					}
					alreadyHandledConfigsSet.add(beanConfiguration);
					atLeastOneHandled = true;

					if (!objectToBeanConfigurationMap.putIfNotExists(bean, beanConfiguration))
					{
						throw maskBeanBasedException("Bean instance " + bean + " registered twice.", beanConfiguration, null);
					}
					if (fillParentHierarchyIfValid(beanContextInit, beanConfiguration, beanConfHierarchy) != null)
					{
						throw new IllegalStateException("Bean configuration must be valid at this point");
					}
					bean = postProcessBean(beanContextInit, beanConfiguration, beanType, bean, beanConfHierarchy);
					beanConfHierarchy.clear();

					publishNamesAndAliasesAndTypes(beanContextInit, beanConfiguration, bean);

					IBeanConfiguration currBeanConf = beanConfiguration;
					while (currBeanConf.getParentName() != null)
					{
						IBeanConfiguration parentBeanConf = beanContext.getBeanConfiguration(beanContextFactory, currBeanConf.getParentName());

						if (parentBeanConf == null)
						{
							throw maskBeanBasedException("Parent bean with name '" + currBeanConf.getParentName() + "' not found", beanConfiguration, null);
						}
						if (!parentBeanConf.isAbstract())
						{
							// The parent bean definition is a valid bean by
							// itself. So the parent hierarchy will not be
							// handled here
							break;
						}
						publishNamesAndAliasesAndTypes(beanContextInit, parentBeanConf, bean);
						currBeanConf = parentBeanConf;
					}
				}
				catch (Throwable e)
				{
					throw maskBeanBasedException(e, beanContextInit, beanConfiguration, null, bean);
				}
			}
		}
	}

	protected Object postProcessBean(BeanContextInit beanContextInit, IBeanConfiguration beanConfiguration, Class<?> beanType, Object bean,
			List<IBeanConfiguration> beanConfHierarchy)
	{
		ServiceContext beanContext = beanContextInit.beanContext;
		BeanContextFactory beanContextFactory = beanContextInit.beanContextFactory;
		List<IBeanPostProcessor> postProcessors = beanContext.getPostProcessors();
		if (postProcessors == null)
		{
			return bean;
		}
		HashSet<Class<?>> allAutowireableTypes = new HashSet<Class<?>>();
		resolveAllAutowireableInterfacesInHierarchy(beanConfHierarchy, allAutowireableTypes);

		Class<?>[] allInterfaces = bean.getClass().getInterfaces();
		for (int b = allInterfaces.length; b-- > 0;)
		{
			Class<?> implementingInterface = allInterfaces[b];
			allAutowireableTypes.add(implementingInterface);
		}
		// Do not manipulate the bean variable until all
		// postprocessors are called without failure
		Object currBean = bean;

		for (int b = 0, sizeB = postProcessors.size(); b < sizeB; b++)
		{
			IBeanPostProcessor postProcessor = postProcessors.get(b);
			try
			{
				currBean = postProcessor.postProcessBean(beanContextFactory, beanContext, beanConfiguration, beanType, currBean, allAutowireableTypes);
			}
			catch (Throwable e)
			{
				throw maskBeanBasedException("Error occured while post-processing with '" + postProcessor + "'", e, beanConfiguration, null);
			}
		}
		callingProxyPostProcessor.beanPostProcessed(beanContextFactory, beanContext, beanConfiguration, beanType, currBean, bean);
		return currBean;
	}

	protected void sortBeanConfigurations(BeanContextInit beanContextInit, List<IBeanConfiguration> beanConfigurations,
			Set<IBeanConfiguration> alreadyHandledConfigsSet, Map<Integer, OrderState> orderToHighBeanConfigurations,
			Map<Integer, OrderState> orderToLowBeanConfigurations, boolean highPriorityOnly)
	{
		ArrayList<IBeanConfiguration> beanConfHierarchy = new ArrayList<IBeanConfiguration>();
		for (int a = 0, size = beanConfigurations.size(); a < size; a++)
		{
			IBeanConfiguration beanConfiguration = beanConfigurations.get(a);
			if (alreadyHandledConfigsSet.contains(beanConfiguration))
			{
				// Already handled so we do not bother anymore
				continue;
			}
			if (beanConfiguration.isAbstract())
			{
				// Abstract bean configurations will not be instantiated -
				// they are templates for other beans
				alreadyHandledConfigsSet.add(beanConfiguration);
				continue;
			}
			beanConfHierarchy.clear();
			if (fillParentHierarchyIfValid(beanContextInit, beanConfiguration, beanConfHierarchy) != null)
			{
				// Something in the hierarchy is currently not valid
				// Maybe with another module in this context the parent bean
				// definitions can be resolved later
				continue;
			}
			Class<?> currentBeanType = resolveTypeInHierarchy(beanConfHierarchy);
			boolean highPriority = isHighPriorityBean(currentBeanType);
			if (highPriorityOnly && !highPriority)
			{
				continue;
			}
			Map<Integer, OrderState> orderToBeanConfigurations = highPriority ? orderToHighBeanConfigurations : orderToLowBeanConfigurations;

			PrecedenceType currentPrecedenceType = beanConfiguration.getPrecedence();
			Integer order = precedenceOrder.get(currentPrecedenceType);

			OrderState list = orderToBeanConfigurations.get(order);
			if (list == null)
			{
				list = new OrderState();
				orderToBeanConfigurations.put(order, list);
			}
			list.add(new BeanConfigState(beanConfiguration, currentBeanType));
		}
	}

	protected BeanConfigState resolveNextPrecedenceBean(BeanContextInit beanContextInit, Map<Integer, OrderState> orderToHighBeanConfigurations,
			Map<Integer, OrderState> orderToLowBeanConfigurations, boolean highPriorityOnly)
	{
		ArrayList<Integer> orders = new ArrayList<Integer>(orderToHighBeanConfigurations.keySet());
		Collections.sort(orders);
		for (int a = 0, size = orders.size(); a < size; a++)
		{
			OrderState list = orderToHighBeanConfigurations.get(orders.get(a));
			BeanConfigState beanConfigState = list.consumeBeanConfigState();
			if (beanConfigState != null)
			{
				return beanConfigState;
			}
		}
		orders = new ArrayList<Integer>(orderToLowBeanConfigurations.keySet());
		Collections.sort(orders);
		for (int a = 0, size = orders.size(); a < size; a++)
		{
			OrderState list = orderToLowBeanConfigurations.get(orders.get(a));
			BeanConfigState beanConfigState = list.consumeBeanConfigState();
			if (beanConfigState != null)
			{
				return beanConfigState;
			}
		}
		return null;
	}

	@Override
	public IList<IBeanConfiguration> fillParentHierarchyIfValid(ServiceContext beanContext, BeanContextFactory beanContextFactory,
			IBeanConfiguration beanConfiguration)
	{
		BeanContextInit beanContextInit = new BeanContextInit();
		beanContextInit.beanContext = beanContext;
		beanContextInit.beanContextFactory = beanContextFactory;
		beanContextInit.properties = beanContext.getService(Properties.class);

		ArrayList<IBeanConfiguration> beanConfHierarchy = new ArrayList<IBeanConfiguration>();
		String missingBeanName = fillParentHierarchyIfValid(beanContextInit, beanConfiguration, beanConfHierarchy);
		if (missingBeanName == null)
		{
			return beanConfHierarchy;
		}
		throw maskBeanBasedException("Illegal bean hierarchy: Bean '" + missingBeanName + "' not found", beanConfiguration, null);
	}

	public String fillParentHierarchyIfValid(BeanContextInit beanContextInit, IBeanConfiguration beanConfiguration, List<IBeanConfiguration> targetBeanList)
	{
		targetBeanList.add(beanConfiguration);
		IBeanConfiguration currBeanConf = beanConfiguration;
		while (currBeanConf.getParentName() != null)
		{
			IBeanConfiguration parentBeanConf = beanContextInit.beanContext.getBeanConfiguration(beanContextInit.beanContextFactory,
					currBeanConf.getParentName());

			if (parentBeanConf == null)
			{
				targetBeanList.clear();
				return currBeanConf.getParentName();
			}
			targetBeanList.add(parentBeanConf);

			currBeanConf = parentBeanConf;
		}
		return null;
	}

	protected void publishNamesAndAliasesAndTypes(BeanContextInit beanContextInit, IBeanConfiguration beanConfiguration, Object bean)
	{
		ServiceContext beanContext = beanContextInit.beanContext;
		BeanContextFactory beanContextFactory = beanContextInit.beanContextFactory;

		String beanName = beanConfiguration.getName();
		if (beanName != null && beanName.length() > 0)
		{
			if (!beanConfiguration.isAbstract())
			{
				beanContext.addNamedBean(beanName, bean);
			}
			ILinkedMap<String, List<String>> beanNameToAliasesMap = beanContextFactory.getBeanNameToAliasesMap();
			if (beanNameToAliasesMap != null)
			{
				List<String> aliasList = beanNameToAliasesMap.get(beanName);
				if (aliasList != null)
				{
					for (int a = aliasList.size(); a-- > 0;)
					{
						String aliasName = aliasList.get(a);
						beanContext.addNamedBean(aliasName, bean);
					}
				}
			}
		}
		List<Class<?>> autowireableTypes = beanConfiguration.getAutowireableTypes();
		if (autowireableTypes != null)
		{
			for (int autowireableIndex = autowireableTypes.size(); autowireableIndex-- > 0;)
			{
				Class<?> autowireableType = autowireableTypes.get(autowireableIndex);
				beanContext.addAutowiredBean(autowireableType, bean);
			}
		}
	}

	@Override
	public Class<?> resolveTypeInHierarchy(List<IBeanConfiguration> beanConfigurations)
	{
		for (int a = 0, size = beanConfigurations.size(); a < size; a++)
		{
			IBeanConfiguration beanConfiguration = beanConfigurations.get(a);
			Class<?> type = beanConfiguration.getBeanType();
			if (type != null)
			{
				return type;
			}
		}
		return null;
	}

	protected void resolveAllIgnoredPropertiesInHierarchy(List<IBeanConfiguration> beanConfHierarchy, Class<?> beanType, Set<String> ignoredProperties)
	{
		Map<String, IPropertyInfo> propertyMap = propertyInfoProvider.getPropertyMap(beanType);
		for (int a = 0, size = beanConfHierarchy.size(); a < size; a++)
		{
			IBeanConfiguration beanConfiguration = beanConfHierarchy.get(a);
			List<String> ignoredPropertyNames = beanConfiguration.getIgnoredPropertyNames();
			if (ignoredPropertyNames == null)
			{
				continue;
			}
			for (int b = ignoredPropertyNames.size(); b-- > 0;)
			{
				String ignoredPropertyName = ignoredPropertyNames.get(b);

				if (!propertyMap.containsKey(ignoredPropertyName))
				{
					String uppercaseFirst = StringConversionHelper.upperCaseFirst(objectCollector, ignoredPropertyName);
					if (!propertyMap.containsKey(uppercaseFirst))
					{
						throw maskBeanBasedException("Property '" + ignoredPropertyName
								+ "' not found to ignore. This is only a check for consistency. However the following list of properties has been found: "
								+ propertyMap.keySet(), beanConfiguration, null);
					}
					ignoredPropertyName = uppercaseFirst;
				}
				ignoredProperties.add(ignoredPropertyName);
			}
		}
	}

	protected void resolveAllAutowireableInterfacesInHierarchy(List<IBeanConfiguration> beanConfHierarchy, Set<Class<?>> autowireableInterfaces)
	{
		for (int a = 0, size = beanConfHierarchy.size(); a < size; a++)
		{
			IBeanConfiguration beanConfiguration = beanConfHierarchy.get(a);
			List<Class<?>> autowireableTypes = beanConfiguration.getAutowireableTypes();
			if (autowireableTypes != null)
			{
				autowireableInterfaces.addAll(autowireableTypes);
			}
		}
	}
}