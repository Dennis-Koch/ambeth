package de.osthus.ambeth.proxy;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.ParamChecker;

public class InterfaceEntityInterceptor implements IInitializingBean, MethodInterceptor
{
	public static final int ID_INDEX = 0;

	private static final Method hashCodeMethod;

	private static final Method equalsMethod;

	// Important to load the foreign static field to this static field on startup because of potential unnecessary classloading issues on finalize()
	private static final Method finalizeMethod = CascadedInterceptor.finalizeMethod;

	static
	{
		try
		{
			hashCodeMethod = Object.class.getMethod("hashCode");
			equalsMethod = Object.class.getMethod("equals", Object.class);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected HashMap<Method, Integer> methodToIndex;

	protected Object[] propertyArray;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(methodToIndex, "MethodToIndex");
		propertyArray = new Object[methodToIndex.size() / 2]; // 2 entries per property
	}

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		if (finalizeMethod.equals(method))
		{
			return null;
		}
		Integer indexHolder = methodToIndex.get(method);
		if (indexHolder == null)
		{
			if (method.equals(hashCodeMethod))
			{
				Object id = propertyArray[ID_INDEX];
				if (id != null)
				{
					return obj.getClass().hashCode() ^ id.hashCode();
				}
				return hashCode(); // entity without id is not equal with anything beside itself
			}
			else if (method.equals(equalsMethod))
			{
				Object other = args[0];
				if (other == obj)
				{
					return Boolean.TRUE;
				}
				if (other == null)
				{
					return Boolean.FALSE;
				}
				if (!obj.getClass().equals(other.getClass()))
				{
					// Proxies must be of the same class
					return Boolean.FALSE;
				}
				Object id = propertyArray[ID_INDEX];
				if (id == null)
				{
					return Boolean.FALSE;
				}
				Object otherId = ((InterfaceEntityInterceptor) ((Factory) obj).getCallback(0)).propertyArray[ID_INDEX];
				return id.equals(otherId);
			}
			throw new IllegalStateException("Method '" + method + "' is not supported by this interceptor");
		}
		int index = indexHolder.intValue();
		if (index < 0) // Negative values tell us that it is a setter call
		{
			propertyArray[-index - 1] = args[0]; // Substract 1 to allow "-0" semantic
			return null;
		}
		else
		{
			return propertyArray[index];
		}
	}
}
