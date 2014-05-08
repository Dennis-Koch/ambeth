package de.osthus.ambeth.cache.interceptor;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.cache.ICacheContext;
import de.osthus.ambeth.cache.ICacheFactory;
import de.osthus.ambeth.cache.ICacheProvider;
import de.osthus.ambeth.cache.ISingleCacheRunnable;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.CascadedInterceptor;
import de.osthus.ambeth.util.ParamChecker;

public class CacheContextInterceptor extends CascadedInterceptor implements IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance(CacheContextInterceptor.class)
	private ILogger log;

	protected ICacheContext cacheContext;

	protected ICacheFactory cacheFactory;

	protected ICacheProvider cacheProvider;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(cacheContext, "cacheContext");
		ParamChecker.assertNotNull(cacheFactory, "cacheFactory");
		ParamChecker.assertNotNull(cacheProvider, "cacheProvider");
	}

	public void setCacheContext(ICacheContext cacheContext)
	{
		this.cacheContext = cacheContext;
	}

	public void setCacheFactory(ICacheFactory cacheFactory)
	{
		this.cacheFactory = cacheFactory;
	}

	public void setCacheProvider(ICacheProvider cacheProvider)
	{
		this.cacheProvider = cacheProvider;
	}

	@Override
	public Object intercept(final Object obj, final Method method, final Object[] args, final MethodProxy proxy) throws Throwable
	{
		if (CascadedInterceptor.finalizeMethod.equals(method))
		{
			return null;
		}
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
