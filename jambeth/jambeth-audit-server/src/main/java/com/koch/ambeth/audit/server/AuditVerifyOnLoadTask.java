package com.koch.ambeth.audit.server;

/*-
 * #%L
 * jambeth-audit-server
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.audit.IAuditEntryVerifier;
import com.koch.ambeth.audit.server.config.AuditConfigurationConstants;
import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IocModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.ILightweightTransaction;
import com.koch.ambeth.merge.cache.CacheFactoryDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.cache.ICacheContext;
import com.koch.ambeth.merge.cache.ICacheFactory;
import com.koch.ambeth.merge.cache.IDisposableCache;
import com.koch.ambeth.merge.security.ISecurityActivation;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

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

	@Autowired
	protected IThreadLocalCleanupController threadLocalCleanupController;

	@Autowired
	protected ILightweightTransaction transaction;

	@Autowired(value = IocModule.THREAD_POOL_NAME)
	protected Executor executor;

	@Property(name = AuditConfigurationConstants.VerifyEntitiesMaxTransactionTime, defaultValue = "30000")
	protected long verifyEntitiesMaxTransactionTime;

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
		final ArrayList<IObjRef> objRefsToVerify = pullObjRefsToVerify();
		if (objRefsToVerify == null)
		{
			return;
		}
		final long openTransactionUntil = System.currentTimeMillis() + verifyEntitiesMaxTransactionTime;
		Boolean reQueue;
		Thread currentThread = Thread.currentThread();
		String oldName = currentThread.getName();
		currentThread.setName(getClass().getSimpleName());
		try
		{
			reQueue = transaction.runInLazyTransaction(new IResultingBackgroundWorkerDelegate<Boolean>()
			{
				@Override
				public Boolean invoke() throws Throwable
				{
					ArrayList<IObjRef> currObjRefsToVerify = objRefsToVerify;
					while (true)
					{
						try
						{
							verifyEntitiesSync(currObjRefsToVerify);
							if (System.currentTimeMillis() > openTransactionUntil)
							{
								return Boolean.TRUE;
							}
						}
						finally
						{
							currObjRefsToVerify = pullObjRefsToVerify();
							if (currObjRefsToVerify == null)
							{
								return Boolean.FALSE;
							}
						}
					}
				}
			});
		}
		finally
		{
			try
			{
				threadLocalCleanupController.cleanupThreadLocal();
			}
			finally
			{
				currentThread.setName(oldName);
			}
		}
		if (Boolean.TRUE.equals(reQueue))
		{
			writeLock.lock();
			try
			{
				isActive = true;
				executor.execute(this);
			}
			finally
			{
				writeLock.unlock();
			}
		}
	}

	private ArrayList<IObjRef> pullObjRefsToVerify()
	{
		writeLock.lock();
		try
		{
			if (queuedObjRefs.size() == 0 || isDestroyed)
			{
				isActive = false;
				return null;
			}
			ArrayList<IObjRef> objRefsToVerify = new ArrayList<IObjRef>(queuedObjRefs);
			queuedObjRefs.clear();
			return objRefsToVerify;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void verifyEntitiesSync(final IList<IObjRef> objRefsToVerify)
	{
		try
		{
			transaction.runInLazyTransaction(new IResultingBackgroundWorkerDelegate<Object>()
			{
				@Override
				public Object invoke() throws Throwable
				{
					runInLazyTransaction(objRefsToVerify);
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

	}

	protected void runInLazyTransaction(final IList<IObjRef> objRefsToVerify) throws Throwable
	{
		IDisposableCache cache = cacheFactory.createPrivileged(CacheFactoryDirective.NoDCE, false, Boolean.FALSE, "AuditEntryVerifier");
		try
		{
			cacheContext.executeWithCache(cache, new IResultingBackgroundWorkerDelegate<Object>()
			{
				@Override
				public Object invoke() throws Throwable
				{
					executeWithCache(objRefsToVerify);
					return null;
				}
			});
		}
		finally
		{
			cache.dispose();
		}
	}

	protected void executeWithCache(final IList<IObjRef> objRefsToVerify) throws Throwable
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
	}
}
