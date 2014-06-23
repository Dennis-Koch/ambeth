package de.osthus.ambeth.cache;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.security.ISecurityActivation;
import de.osthus.ambeth.threading.SensitiveThreadLocal;

public class CacheProvider implements IInitializingBean, IThreadLocalCleanupBean, ICacheProvider
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICacheFactory cacheFactory;

	@Autowired
	protected IRootCache rootCache;

	@Autowired(optional = true)
	protected ISecurityActivation securityActivation;

	@Property
	protected CacheType cacheType;

	protected volatile ICache singletonCache;

	protected volatile ICache privilegedSingletonCache;

	protected ThreadLocal<IDisposableCache> cacheTL;

	protected ThreadLocal<IDisposableCache> privilegedCacheTL;

	protected final Lock lock = new ReentrantLock();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
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
				if (securityActivation != null)
				{
					privilegedCacheTL = new SensitiveThreadLocal<IDisposableCache>();
				}
				break;
			}
			default:
				throw new IllegalStateException("Not supported type: " + cacheType);
		}
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
			cache = privilegedCacheTL.get();
			if (cache != null)
			{
				privilegedCacheTL.remove();
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
					if (securityActivation != null && !securityActivation.isFilterActivated())
					{
						if (privilegedSingletonCache == null)
						{
							privilegedSingletonCache = cacheFactory.create(CacheFactoryDirective.SubscribeTransactionalDCE, true, null);
						}
						return privilegedSingletonCache;
					}
					else
					{
						if (singletonCache == null)
						{
							singletonCache = cacheFactory.create(CacheFactoryDirective.SubscribeTransactionalDCE, true, null);
						}
						return singletonCache;
					}
				}
				finally
				{
					lock.unlock();
				}
			}
			case THREAD_LOCAL:
			{
				if (securityActivation != null && !securityActivation.isFilterActivated())
				{
					IDisposableCache cache = privilegedCacheTL.get();
					if (cache == null)
					{
						cache = cacheFactory.create(CacheFactoryDirective.SubscribeTransactionalDCE, false, Boolean.FALSE);
						privilegedCacheTL.set(cache);
					}
					return cache;
				}
				else
				{
					IDisposableCache cache = cacheTL.get();
					if (cache == null)
					{
						cache = cacheFactory.create(CacheFactoryDirective.SubscribeTransactionalDCE, false, Boolean.FALSE);
						cacheTL.set(cache);
					}
					return cache;
				}
			}
			default:
				throw new IllegalStateException("Not supported type: " + cacheType);
		}
	}
}
