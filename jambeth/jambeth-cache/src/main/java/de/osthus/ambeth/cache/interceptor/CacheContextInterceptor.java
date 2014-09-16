package de.osthus.ambeth.cache.interceptor;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.cache.ICacheContext;
import de.osthus.ambeth.cache.ICacheFactory;
import de.osthus.ambeth.cache.ICacheProvider;
import de.osthus.ambeth.cache.ISingleCacheRunnable;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.CascadedInterceptor;

public class CacheContextInterceptor extends CascadedInterceptor
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICacheContext cacheContext;

	@Autowired
	protected ICacheFactory cacheFactory;

	@Autowired
	protected ICacheProvider cacheProvider;

	@Override
	protected Object interceptIntern(final Object obj, final Method method, final Object[] args, final MethodProxy proxy) throws Throwable
	{
		if (method.getDeclaringClass().equals(Object.class))
		{
			return invokeTarget(obj, method, args, proxy);
		}
		return cacheContext.executeWithCache(cacheProvider, new ISingleCacheRunnable<Object>()
		{
			@Override
			public Object run() throws Throwable
			{
				try
				{
					return invokeTarget(obj, method, args, proxy);
				}
				catch (Throwable e)
				{
					throw RuntimeExceptionUtil.mask(e, method.getExceptionTypes());
				}
			}
		});
	}
}
