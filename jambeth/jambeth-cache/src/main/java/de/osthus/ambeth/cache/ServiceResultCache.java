package de.osthus.ambeth.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.cache.config.CacheConfigurationConstants;
import de.osthus.ambeth.cache.model.IServiceResult;
import de.osthus.ambeth.cache.transfer.ServiceResult;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.model.IServiceDescription;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.security.ISecurityActivation;
import de.osthus.ambeth.security.ISecurityManager;
import de.osthus.ambeth.service.IServiceByNameProvider;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;

public class ServiceResultCache implements IServiceResultCache
{
	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired(optional = true)
	protected ISecurityActivation securityActivation;

	@Autowired(optional = true)
	protected ISecurityManager securityManager;

	@Autowired
	protected IServiceByNameProvider serviceByNameProvider;

	@Property(name = CacheConfigurationConstants.ServiceResultCacheActive, defaultValue = "false")
	protected boolean useResultCache;

	protected final HashMap<ServiceResultCacheKey, IServiceResult> serviceCallToResult = new HashMap<ServiceResultCacheKey, IServiceResult>();

	protected final HashSet<ServiceResultCacheKey> serviceCallToPendingResult = new HashSet<ServiceResultCacheKey>();

	protected final Lock writeLock = new ReentrantLock();

	protected final Condition serviceCallPending = writeLock.newCondition();

	protected ServiceResultCacheKey buildKey(IServiceDescription serviceDescription)
	{
		Object service = serviceByNameProvider.getService(serviceDescription.getServiceName());
		ServiceResultCacheKey key = new ServiceResultCacheKey();
		key.arguments = serviceDescription.getArguments();
		key.method = serviceDescription.getMethod(service.getClass(), objectCollector);
		key.serviceName = serviceDescription.getServiceName();
		if (key.method == null || key.serviceName == null)
		{
			throw new IllegalArgumentException("ServiceDescription not legal " + serviceDescription);
		}
		return key;
	}

	@Override
	public IServiceResult getORIsOfService(final IServiceDescription serviceDescription, final ExecuteServiceDelegate executeServiceDelegate)
	{
		if (!useResultCache)
		{
			return executeServiceDelegate.invoke(serviceDescription);
		}
		ServiceResultCacheKey key = buildKey(serviceDescription);
		Lock writeLock = this.writeLock;
		IServiceResult serviceResult;
		writeLock.lock();
		try
		{
			serviceResult = serviceCallToResult.get(key);
			if (serviceResult != null)
			{
				// Important to clone the ori list, because potential
				// (user-dependent) security logic may truncate this list
				return createServiceResult(serviceResult);
			}
			while (serviceCallToPendingResult.contains(key))
			{
				serviceCallPending.awaitUninterruptibly();
			}
			serviceResult = serviceCallToResult.get(key);
			if (serviceResult != null)
			{
				return createServiceResult(serviceResult);
			}
			serviceCallToPendingResult.add(key);
		}
		finally
		{
			writeLock.unlock();
		}
		boolean success = false;
		try
		{
			if (securityActivation != null)
			{
				serviceResult = securityActivation.executeWithoutFiltering(new IResultingBackgroundWorkerDelegate<IServiceResult>()
				{
					@Override
					public IServiceResult invoke() throws Throwable
					{
						return executeServiceDelegate.invoke(serviceDescription);
					}
				});
			}
			else
			{
				serviceResult = executeServiceDelegate.invoke(serviceDescription);
			}
			success = true;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			writeLock.lock();
			try
			{
				serviceCallToPendingResult.remove(key);

				if (success)
				{
					serviceCallToResult.put(key, serviceResult);
				}
				serviceCallPending.signalAll();
			}
			finally
			{
				writeLock.unlock();
			}
		}
		return createServiceResult(serviceResult);
	}

	protected IServiceResult createServiceResult(IServiceResult cachedServiceResult)
	{
		// Important to clone the ori list, because potential (user-dependent)
		// security logic may truncate this list (original must remain unmodified)
		ArrayList<IObjRef> list = new ArrayList<IObjRef>();
		list.addAll(cachedServiceResult.getObjRefs());

		List<IObjRef> filteredList;
		if (securityManager != null)
		{
			filteredList = securityManager.filterValue(list);
		}
		else
		{
			filteredList = list;
		}

		if (list != filteredList)
		{
			list = null;
		}

		ServiceResult serviceResult = new ServiceResult();
		serviceResult.setAdditionalInformation(cachedServiceResult.getAdditionalInformation());
		serviceResult.setObjRefs(filteredList);
		return serviceResult;
	}

	public void handleClearAllCaches(ClearAllCachesEvent evnt)
	{
		invalidateAll();
	}

	@Override
	public void invalidateAll()
	{
		writeLock.lock();
		try
		{
			serviceCallToResult.clear();
		}
		finally
		{
			writeLock.unlock();
		}
	}
}
