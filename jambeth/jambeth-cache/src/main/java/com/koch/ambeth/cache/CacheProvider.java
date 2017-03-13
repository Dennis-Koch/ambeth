package com.koch.ambeth.cache;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.threadlocal.Forkable;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.cache.CacheFactoryDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.cache.ICacheFactory;
import com.koch.ambeth.merge.cache.ICacheProvider;
import com.koch.ambeth.merge.cache.IDisposableCache;
import com.koch.ambeth.merge.config.MergeConfigurationConstants;
import com.koch.ambeth.merge.security.ISecurityActivation;
import com.koch.ambeth.util.threading.SensitiveThreadLocal;

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

	@Property(name = MergeConfigurationConstants.SecurityActive, defaultValue = "false")
	protected boolean securityActive;

	@Property
	protected CacheType cacheType;

	protected volatile ICache singletonCache;

	protected volatile ICache privilegedSingletonCache;

	@Forkable
	protected ThreadLocal<IDisposableCache> cacheTL;

	@Forkable
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
				cacheTL.set(null);
				cache.dispose();
			}
		}
		if (privilegedCacheTL != null)
		{
			IDisposableCache cache = privilegedCacheTL.get();
			if (cache != null)
			{
				privilegedCacheTL.set(null);
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
				if (!securityActive || !securityActivation.isFilterActivated())
				{
					return cacheFactory.createPrivileged(CacheFactoryDirective.SubscribeTransactionalDCE, false, null, "CacheProvider.PROTOTYPE");
				}
				return cacheFactory.create(CacheFactoryDirective.SubscribeTransactionalDCE, false, null, "CacheProvider.PROTOTYPE");
			}
			case SINGLETON:
			{
				lock.lock();
				try
				{
					if (!securityActive || !securityActivation.isFilterActivated())
					{
						if (privilegedSingletonCache == null)
						{
							privilegedSingletonCache = cacheFactory.createPrivileged(CacheFactoryDirective.SubscribeTransactionalDCE, true, null,
									"CacheProvider.SINGLETON");
						}
						return privilegedSingletonCache;
					}
					else
					{
						if (singletonCache == null)
						{
							singletonCache = cacheFactory.create(CacheFactoryDirective.SubscribeTransactionalDCE, true, null, "CacheProvider.SINGLETON");
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
				if (!securityActive || !securityActivation.isFilterActivated())
				{
					IDisposableCache cache = privilegedCacheTL.get();
					if (cache == null)
					{
						cache = cacheFactory.createPrivileged(CacheFactoryDirective.SubscribeTransactionalDCE, false, Boolean.FALSE,
								"CacheProvider.THREAD_LOCAL");
						privilegedCacheTL.set(cache);
					}
					return cache;
				}
				else
				{
					IDisposableCache cache = cacheTL.get();
					if (cache == null)
					{
						cache = cacheFactory.create(CacheFactoryDirective.SubscribeTransactionalDCE, false, Boolean.FALSE, "CacheProvider.THREAD_LOCAL");
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
