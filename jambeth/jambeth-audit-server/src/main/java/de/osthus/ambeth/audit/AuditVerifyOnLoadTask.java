package de.osthus.ambeth.audit;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.cache.CacheFactoryDirective;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.ICacheContext;
import de.osthus.ambeth.cache.ICacheFactory;
import de.osthus.ambeth.cache.IDisposableCache;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.ioc.IocModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.security.ISecurityActivation;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;

public class AuditVerifyOnLoadTask implements Runnable, IAuditVerifyOnLoadTask, IDisposableBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IAuditEntryVerifier auditEntryVerifier;

	@Autowired
	protected ICache cache;

	@Autowired
	protected ICacheContext cacheContext;

	@Autowired
	protected ICacheFactory cacheFactory;

	@Autowired
	protected ISecurityActivation securityActivation;

	@Autowired(value = IocModule.THREAD_POOL_NAME)
	protected Executor executor;

	protected final ArrayList<IObjRef> queuedObjRefs = new ArrayList<IObjRef>();

	protected boolean isActive, isDestroyed;

	protected final Lock writeLock = new ReentrantLock();

	@Override
	public void destroy() throws Throwable
	{
		isDestroyed = true;
	}

	@Override
	public void verifyEntitiesAsync(IList<IObjRef> objRefs)
	{
		writeLock.lock();
		try
		{
			queuedObjRefs.addAll(objRefs);
			if (isActive)
			{
				return;
			}
			isActive = true;
			executor.execute(this);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void run()
	{
		if (isDestroyed)
		{
			return;
		}
		final ArrayList<IObjRef> objRefsToVerify;
		writeLock.lock();
		try
		{
			if (queuedObjRefs.size() == 0)
			{
				isActive = false;
				return;
			}
			objRefsToVerify = new ArrayList<IObjRef>(queuedObjRefs);
			queuedObjRefs.clear();
		}
		finally
		{
			writeLock.unlock();
		}
		try
		{
			verifyEntitiesSync(objRefsToVerify);
		}
		finally
		{
			writeLock.lock();
			try
			{
				if (queuedObjRefs.size() == 0 || isDestroyed)
				{
					isActive = false;
					return;
				}
				executor.execute(this);
			}
			finally
			{
				writeLock.unlock();
			}
		}
	}

	@Override
	public void verifyEntitiesSync(final IList<IObjRef> objRefsToVerify)
	{
		IDisposableCache cache = cacheFactory.createPrivileged(CacheFactoryDirective.NoDCE, false, Boolean.FALSE, "AuditEntryVerifier");
		try
		{
			cacheContext.executeWithCache(cache, new IResultingBackgroundWorkerDelegate<Object>()
			{
				@Override
				public Object invoke() throws Throwable
				{
					securityActivation.executeWithoutSecurity(new IBackgroundWorkerDelegate()
					{
						@Override
						public void invoke() throws Throwable
						{
							if (!auditEntryVerifier.verifyEntities(objRefsToVerify))
							{
								log.error("Audit entry verification failed: " + Arrays.toString(objRefsToVerify.toArray()));
							}
						}
					});
					return null;
				}
			});
		}
		catch (Throwable e)
		{
			if (!isDestroyed)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		finally
		{
			cache.dispose();
		}
	}
}
