package de.osthus.ambeth.log;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import de.osthus.ambeth.annotation.AnnotationCache;
import de.osthus.ambeth.collections.WeakSmartCopyMap;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IBeanPreProcessor;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.config.IPropertyConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.typeinfo.IPropertyInfo;
import de.osthus.ambeth.util.EqualsUtil;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.ReflectUtil;

public class LoggerInstancePreProcessor extends WeakSmartCopyMap<Class<?>, ILogger> implements IBeanPreProcessor, ILoggerCache, IInitializingBean
{
	protected final AnnotationCache<LogInstance> logInstanceCache = new AnnotationCache<LogInstance>(LogInstance.class)
	{
		@Override
		protected boolean annotationEquals(LogInstance left, LogInstance right)
		{
			return EqualsUtil.equals(left.value(), right.value());
		}
	};

	protected IThreadLocalObjectCollector objectCollector;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(objectCollector, "objectCollector");
	}

	public void setObjectCollector(IThreadLocalObjectCollector objectCollector)
	{
		this.objectCollector = objectCollector;
	}

	@Override
	public void preProcessProperties(IBeanContextFactory beanContextFactory, IServiceContext beanContext, IProperties props, String beanName, Object service,
			Class<?> beanType, List<IPropertyConfiguration> propertyConfigs, Set<String> ignoredPropertyNames, IPropertyInfo[] properties)
	{
		scanForLogField(props, service, beanType, service.getClass(), ignoredPropertyNames);
	}

	protected void scanForLogField(IProperties props, Object service, Class<?> beanType, Class<?> type, Set<String> ignoredPropertyNames)
	{
		if (type == null || Object.class.equals(type))
		{
			return;
		}
		scanForLogField(props, service, beanType, type.getSuperclass(), ignoredPropertyNames);
		Field[] fields = ReflectUtil.getDeclaredFields(type);
		for (int a = fields.length; a-- > 0;)
		{
			Field field = fields[a];
			if (!field.getType().equals(ILogger.class))
			{
				continue;
			}
			if (ignoredPropertyNames.contains(field.getName()))
			{
				// do not handle this property
				continue;
			}
			ILogger logger = getLoggerIfNecessary(props, beanType, field);
			if (logger == null)
			{
				continue;
			}
			try
			{
				field.set(service, logger);
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
	}

	protected ILogger getLoggerIfNecessary(IProperties props, Class<?> beanType, Field memberInfo)
	{
		LogInstance logInstance = logInstanceCache.getAnnotation(memberInfo);
		if (logInstance == null)
		{
			return null;
		}
		Class<?> loggerBeanType = memberInfo.getDeclaringClass();
		if (!void.class.equals(logInstance.value()))
		{
			loggerBeanType = logInstance.value();
		}
		return getCachedLogger(props, loggerBeanType);
	}

	@Override
	public ILogger getCachedLogger(IServiceContext beanContext, Class<?> loggerBeanType)
	{
		ILogger logger = get(loggerBeanType);
		if (logger != null)
		{
			return logger;
		}
		return getCachedLogger(beanContext.getService(IProperties.class), loggerBeanType);
	}

	@Override
	public ILogger getCachedLogger(IProperties properties, Class<?> loggerBeanType)
	{
		ILogger logger = get(loggerBeanType);
		if (logger != null)
		{
			return logger;
		}
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			logger = get(loggerBeanType);
			if (logger != null)
			{
				// Concurrent thread might have been faster
				return logger;
			}
			logger = LoggerFactory.getLogger(loggerBeanType, properties);
			if (logger instanceof IConfigurableLogger)
			{
				((IConfigurableLogger) logger).setObjectCollector(objectCollector);
			}
			put(loggerBeanType, logger);
			return logger;
		}
		finally
		{
			writeLock.unlock();
		}
	}
}