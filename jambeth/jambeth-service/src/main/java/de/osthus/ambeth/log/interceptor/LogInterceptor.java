package de.osthus.ambeth.log.interceptor;

import java.lang.reflect.Method;
import java.util.Collection;

import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.log.LogTypesUtil;
import de.osthus.ambeth.log.LoggerFactory;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.proxy.CascadedInterceptor;
import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.threading.SensitiveThreadLocal;

public class LogInterceptor extends CascadedInterceptor
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

	// Important to load the foreign static field to this static field on startup because of potential unnecessary classloading issues on finalize()
	private static final Method finalizeMethod = CascadedInterceptor.finalizeMethod;

	@LogInstance
	private ILogger log;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IProperties properties;

	@Property(name = ConfigurationConstants.LogShortNames, defaultValue = "false")
	protected boolean printShortStringNames;

	@Property(name = ConfigurationConstants.NetworkClientMode, defaultValue = "false")
	protected boolean isClientLogger;

	@SuppressWarnings("rawtypes")
	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		if (finalizeMethod.equals(method))
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
