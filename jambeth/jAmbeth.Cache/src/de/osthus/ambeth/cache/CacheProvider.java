package de.osthus.ambeth.cache;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.SensitiveThreadLocal;
import de.osthus.ambeth.util.ParamChecker;

public class CacheProvider implements IInitializingBean, IThreadLocalCleanupBean, ICacheProvider
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected CacheType cacheType;

	protected ICacheFactory cacheFactory;

	protected volatile ICache singletonCache;

	protected IRootCache rootCache;

	protected ThreadLocal<IDisposableCache> cacheTL;

	protected final Lock lock = new ReentrantLock();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(cacheFactory, "cacheFactory");
		ParamChecker.assertNotNull(cacheType, "CacheType");
		ParamChecker.assertNotNull(rootCache, "rootCache");

		switch (cacheType)
		{
			case PROTOTYPE:
			{
				break;
			}
			case SINGLETON:
			{
				break;
			}
			case THREAD_LOCAL:
			{
				cacheTL = new SensitiveThreadLocal<IDisposableCache>();
				break;
			}
			default:
				throw new IllegalStateException("Not supported type: " + cacheType);
		}
	}

	public void setCacheFactory(ICacheFactory cacheFactory)
	{
		this.cacheFactory = cacheFactory;
	}

	public void setCacheType(CacheType cacheType)
	{
		this.cacheType = cacheType;
	}

	public void setRootCache(IRootCache rootCache)
	{
		this.rootCache = rootCache;
	}

	@Override
	public void cleanupThreadLocal()
	{
		if (cacheTL != null)
		{
			IDisposableCache cache = cacheTL.get();
			if (cache != null)
			{
				cacheTL.remove();
				cache.dispose();
			}
		}
	}

	@Override
	public boolean isNewInstanceOnCall()
	{
		switch (cacheType)
		{
			case PROTOTYPE:
			{
				return true;
			}
			case SINGLETON:
			case THREAD_LOCAL:
			{
				return false;
			}
			default:
				throw new RuntimeException("Not supported type: " + cacheType);
		}
	}

	@Override
	public ICache getCurrentCache()
	{
		switch (cacheType)
		{
			case PROTOTYPE:
			{
				return cacheFactory.create(CacheFactoryDirective.SubscribeTransactionalDCE, false, null);
			}
			case SINGLETON:
			{
				lock.lock();
				try
				{
					if (singletonCache == null)
					{
						singletonCache = cacheFactory.create(CacheFactoryDirective.SubscribeTransactionalDCE, true, null);
					}
					return singletonCache;
				}
				finally
				{
					lock.unlock();
				}
			}
			case THREAD_LOCAL:
			{
				IDisposableCache cache = cacheTL.get();
				if (cache == null)
				{
					cache = cacheFactory.create(CacheFactoryDirective.SubscribeTransactionalDCE, false, Boolean.FALSE);
					cacheTL.set(cache);
				}
				return cache;
			}
			default:
				throw new IllegalStateException("Not supported type: " + cacheType);
		}
	}
}
