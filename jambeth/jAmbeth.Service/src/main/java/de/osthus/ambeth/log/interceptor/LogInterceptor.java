package de.osthus.ambeth.log.interceptor;

import java.lang.reflect.Method;
import java.util.Collection;

import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.log.LogTypesUtil;
import de.osthus.ambeth.log.LoggerFactory;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.proxy.CascadedInterceptor;
import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.threading.SensitiveThreadLocal;
import de.osthus.ambeth.util.ParamChecker;

public class LogInterceptor extends CascadedInterceptor implements IInitializingBean
{
	public static class IntContainer
	{
		public int stackLevel;
	}

	private static final ThreadLocal<IntContainer> stackValueTL = new SensitiveThreadLocal<IntContainer>()
	{
		@Override
		protected IntContainer initialValue()
		{
			return new IntContainer();
		};
	};

	@LogInstance
	private ILogger log;

	protected IThreadLocalObjectCollector objectCollector;

	protected IProperties properties;

	protected boolean printShortStringNames;

	protected boolean isClientLogger;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(objectCollector, "objectCollector");
		ParamChecker.assertNotNull(properties, "Properties");
	}

	@Property(name = ConfigurationConstants.LogShortNames, defaultValue = "false")
	public void setPrintShortStringNames(boolean printShortStringNames)
	{
		this.printShortStringNames = printShortStringNames;
	}

	@Property(name = ConfigurationConstants.NetworkClientMode, defaultValue = "false")
	public void setIsClientLogger(boolean isClientLogger)
	{
		this.isClientLogger = isClientLogger;
	}

	public void setObjectCollector(IThreadLocalObjectCollector objectCollector)
	{
		this.objectCollector = objectCollector;
	}

	public void setProperties(IProperties properties)
	{
		this.properties = properties;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		if (CascadedInterceptor.finalizeMethod.equals(method))
		{
			return null;
		}
		Class<?> declaringClass = method.getDeclaringClass();
		if (Object.class.equals(declaringClass))
		{
			return invokeTarget(obj, method, args, proxy);
		}
		IThreadLocalObjectCollector current = objectCollector.getCurrent();
		StringBuilder sb = objectCollector.create(StringBuilder.class);
		IntContainer stackValueContainer = stackValueTL.get();
		stackValueContainer.stackLevel++;
		try
		{
			long startTicks = 0;
			ILogger loggerOfMethod = LoggerFactory.getLogger(declaringClass, properties);
			boolean debugEnabled = log.isDebugEnabled() && loggerOfMethod.isDebugEnabled();
			if (debugEnabled)
			{
				if (!isClientLogger)
				{
					sb.append("Start:     ");
				}
				else
				{
					sb.append("Start(S):  ");
				}
				int level = stackValueContainer.stackLevel;
				while (level-- > 1)
				{
					sb.append(".");
				}
				LogTypesUtil.printMethod(method, printShortStringNames, sb);
				loggerOfMethod.debug(sb.toString());
				sb.setLength(0);

				startTicks = System.currentTimeMillis();
			}
			Object returnValue = invokeTarget(obj, method, args, proxy);
			if (debugEnabled)
			{
				long endTicks = System.currentTimeMillis();

				int resultCount = returnValue instanceof Collection ? ((Collection) returnValue).size() : returnValue != null ? 1 : -1;
				String resultString = resultCount >= 0 ? "" + resultCount : "no";
				String itemsString = void.class.equals(method.getReturnType()) ? "" : " with " + resultString + (resultCount != 1 ? " items" : " item");

				if (isClientLogger)
				{
					sb.append("Finish:    ");
				}
				else
				{
					sb.append("Finish(S): ");
				}
				int level = stackValueContainer.stackLevel;
				while (level-- > 1)
				{
					sb.append(".");
				}
				LogTypesUtil.printMethod(method, printShortStringNames, sb);
				sb.append(itemsString).append(" (").append(endTicks - startTicks).append(" ms)");
				loggerOfMethod.debug(sb.toString());
			}
			return returnValue;
		}
		catch (Throwable e)
		{
			if (log.isErrorEnabled())
			{
				log.error(e);
			}
			throw e;
		}
		finally
		{
			stackValueContainer.stackLevel--;
			current.dispose(sb);
		}
	}

	@Override
	public String toString()
	{
		return getClass().getName() + ": " + getTarget();
	}
}
