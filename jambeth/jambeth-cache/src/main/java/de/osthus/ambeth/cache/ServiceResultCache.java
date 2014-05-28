package de.osthus.ambeth.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.osthus.ambeth.cache.config.CacheConfigurationConstants;
import de.osthus.ambeth.cache.model.IServiceResult;
import de.osthus.ambeth.cache.transfer.ServiceResult;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.model.IServiceDescription;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.security.ISecurityActivation;
import de.osthus.ambeth.security.ISecurityManager;
import de.osthus.ambeth.service.IServiceByNameProvider;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.util.ParamChecker;

public class ServiceResultCache implements IServiceResultCache, IInitializingBean
{
	protected Map<ServiceResultCacheKey, IServiceResult> serviceCallToResult = new HashMap<ServiceResultCacheKey, IServiceResult>();

	protected Set<ServiceResultCacheKey> serviceCallToPendingResult = new HashSet<ServiceResultCacheKey>();

	protected boolean useResultCache;

	protected IThreadLocalObjectCollector objectCollector;

	protected ISecurityActivation securityActivation;

	protected ISecurityManager securityManager;

	protected IServiceByNameProvider serviceByNameProvider;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(objectCollector, "objectCollector");
		ParamChecker.assertNotNull(serviceByNameProvider, "serviceByNameProvider");
	}

	public void setObjectCollector(IThreadLocalObjectCollector objectCollector)
	{
		this.objectCollector = objectCollector;
	}

	public void setSecurityActivation(ISecurityActivation securityActivation)
	{
		this.securityActivation = securityActivation;
	}

	public void setSecurityManager(ISecurityManager securityManager)
	{
		this.securityManager = securityManager;
	}

	public void setServiceByNameProvider(IServiceByNameProvider serviceByNameProvider)
	{
		this.serviceByNameProvider = serviceByNameProvider;
	}

	@Property(name = CacheConfigurationConstants.ServiceResultCacheActive, defaultValue = "false")
	public void setUseResultCache(boolean useResultCache)
	{
		this.useResultCache = useResultCache;
	}

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
		IServiceResult serviceResult;
		synchronized (serviceCallToPendingResult)
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
				try
				{
					serviceCallToPendingResult.wait();
				}
				catch (InterruptedException e)
				{
					// Intended blank
				}
			}
			serviceResult = serviceCallToResult.get(key);
			if (serviceResult != null)
			{
				return createServiceResult(serviceResult);
			}
			serviceCallToPendingResult.add(key);
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
			synchronized (serviceCallToPendingResult)
			{
				serviceCallToPendingResult.remove(key);

				if (success)
				{
					serviceCallToResult.put(key, serviceResult);
				}
				serviceCallToPendingResult.notifyAll();
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

	@Override
	public void invalidateAll()
	{
		synchronized (serviceCallToPendingResult)
		{
			serviceCallToResult.clear();
		}
	}
}
