package de.osthus.ambeth.cache;

import java.lang.reflect.Method;
import java.util.List;

import net.sf.cglib.proxy.Factory;
import de.osthus.ambeth.annotation.AnnotationCache;
import de.osthus.ambeth.annotation.AnnotationUtil;
import de.osthus.ambeth.annotation.QueryBehavior;
import de.osthus.ambeth.annotation.QueryBehaviorType;
import de.osthus.ambeth.cache.config.CacheConfigurationConstants;
import de.osthus.ambeth.cache.interceptor.CacheInterceptor;
import de.osthus.ambeth.cache.model.ILoadContainer;
import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.cache.model.IObjRelationResult;
import de.osthus.ambeth.cache.model.IServiceResult;
import de.osthus.ambeth.cache.transfer.ServiceResult;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.model.IDirectObjRef;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.model.IServiceDescription;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.proxy.ICascadedInterceptor;
import de.osthus.ambeth.security.ISecurityScopeProvider;
import de.osthus.ambeth.service.ICacheService;
import de.osthus.ambeth.service.IServiceByNameProvider;
import de.osthus.ambeth.util.EqualsUtil;
import de.osthus.ambeth.util.ParamChecker;

public class CacheService implements ICacheService, IInitializingBean, ExecuteServiceDelegate
{
	@LogInstance
	private ILogger log;

	protected final AnnotationCache<QueryBehavior> queryBehaviorCache = new AnnotationCache<QueryBehavior>(QueryBehavior.class)
	{
		@Override
		protected boolean annotationEquals(QueryBehavior left, QueryBehavior right)
		{
			return EqualsUtil.equals(left.value(), right.value());
		}
	};

	protected ICache cache;

	protected IThreadLocalObjectCollector objectCollector;

	protected IObjRefHelper oriHelper;

	protected IServiceResultHolder oriResultHolder;

	protected ISecurityScopeProvider securityScopeProvider;

	protected IServiceByNameProvider serviceByNameProvider;

	protected IServiceResultCache serviceResultCache;

	protected IServiceResultProcessorRegistry serviceResultProcessorRegistry;

	protected QueryBehaviorType defaultQueryBehaviorType;

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertNotNull(cache, "cache");
		ParamChecker.assertNotNull(objectCollector, "objectCollector");
		ParamChecker.assertNotNull(oriHelper, "oriHelper");
		ParamChecker.assertNotNull(oriResultHolder, "oriResultHolder");
		ParamChecker.assertNotNull(serviceByNameProvider, "serviceByNameProvider");
		ParamChecker.assertNotNull(serviceResultCache, "serviceResultCache");
		ParamChecker.assertNotNull(serviceResultProcessorRegistry, "serviceResultProcessorRegistry");
		if (defaultQueryBehaviorType == null)
		{
			defaultQueryBehaviorType = QueryBehaviorType.DEFAULT;
		}
		if (securityScopeProvider == null)
		{
			if (log.isDebugEnabled())
			{
				log.debug("No securityScopeProvider found. Recovering work without security scope");
			}
		}
	}

	@Property(name = CacheConfigurationConstants.QueryBehavior, mandatory = false)
	public void setDefaultQueryBehaviorType(QueryBehaviorType defaultQueryBehaviorType)
	{
		this.defaultQueryBehaviorType = defaultQueryBehaviorType;
	}

	public void setSecurityScopeProvider(ISecurityScopeProvider securityScopeProvider)
	{
		this.securityScopeProvider = securityScopeProvider;
	}

	public void setServiceByNameProvider(IServiceByNameProvider serviceByNameProvider)
	{
		this.serviceByNameProvider = serviceByNameProvider;
	}

	public void setOriResultHolder(IServiceResultHolder oriResultHolder)
	{
		this.oriResultHolder = oriResultHolder;
	}

	public void setServiceResultCache(IServiceResultCache serviceResultCache)
	{
		this.serviceResultCache = serviceResultCache;
	}

	public void setServiceResultProcessorRegistry(IServiceResultProcessorRegistry serviceResultProcessorRegistry)
	{
		this.serviceResultProcessorRegistry = serviceResultProcessorRegistry;
	}

	public void setOriHelper(IObjRefHelper oriHelper)
	{
		this.oriHelper = oriHelper;
	}

	public void setObjectCollector(IThreadLocalObjectCollector objectCollector)
	{
		this.objectCollector = objectCollector;
	}

	public void setCache(ICache cache)
	{
		this.cache = cache;
	}

	@Override
	public IList<IObjRelationResult> getRelations(List<IObjRelation> objRelations)
	{
		ParamChecker.assertParamNotNull(objRelations, "objRelations");

		return cache.getObjRelations(objRelations, CacheDirective.none());
	}

	@Override
	public IList<ILoadContainer> getEntities(List<IObjRef> orisToLoad)
	{
		ParamChecker.assertParamNotNull(orisToLoad, "orisToLoad");

		IThreadLocalObjectCollector current = objectCollector.getCurrent();

		if (log.isDebugEnabled())
		{
			StringBuilder sb = current.create(StringBuilder.class);
			try
			{
				int count = orisToLoad.size();
				sb.append("List<IObjRef> : ").append(count).append(" item");
				if (count != 1)
				{
					sb.append('s');
				}
				sb.append(" [");

				if (orisToLoad.size() < 100)
				{
					for (int a = orisToLoad.size(); a-- > 0;)
					{
						IObjRef oriToLoad = orisToLoad.get(a);
						if (count > 1)
						{
							sb.append("\r\n\t");
						}
						sb.append(oriToLoad.toString());
					}
				}
				else
				{
					sb.append("<skipped details>");
				}
				sb.append("]");

				log.debug(sb.toString());
			}
			finally
			{
				current.dispose(sb);
			}
		}
		List<Object> result = cache.getObjects(orisToLoad, CacheDirective.loadContainerResult());
		try
		{
			ArrayList<ILoadContainer> lcResult = new ArrayList<ILoadContainer>();
			for (int a = 0, size = result.size(); a < size; a++)
			{
				lcResult.add((ILoadContainer) result.get(a));
			}
			return lcResult;
		}
		finally
		{
			result.clear();
			result = null;
		}
	}

	@Override
	public IServiceResult getORIsForServiceRequest(IServiceDescription serviceDescription)
	{
		ParamChecker.assertParamNotNull(serviceDescription, "serviceDescription");

		if (securityScopeProvider != null)
		{
			securityScopeProvider.setSecurityScopes(serviceDescription.getSecurityScopes());
		}
		return serviceResultCache.getORIsOfService(serviceDescription, this);
	}

	@Override
	public IServiceResult invoke(IServiceDescription serviceDescription)
	{
		ParamChecker.assertParamNotNull(serviceDescription, "serviceDescription");

		Object service = serviceByNameProvider.getService(serviceDescription.getServiceName());

		if (service == null)
		{
			throw new IllegalStateException("No service with name '" + serviceDescription.getServiceName() + "' found");
		}
		Method method = serviceDescription.getMethod(service.getClass(), objectCollector);
		if (method == null)
		{
			throw new IllegalStateException("Requested method not found on service '" + serviceDescription.getServiceName() + "'");
		}
		// Look first at the real instance to support annotations of implementing classes
		Object realService = getRealTargetOfService(service);

		QueryBehaviorType queryBehaviorType = findQueryBehaviorType(realService, method);

		boolean useFastOriResultFeature;
		switch (queryBehaviorType)
		{
			case DEFAULT:
				useFastOriResultFeature = false;
				break;
			case OBJREF_ONLY:
				useFastOriResultFeature = true;
				break;
			default:
				throw RuntimeExceptionUtil.createEnumNotSupportedException(queryBehaviorType);
		}
		IServiceResult preCallServiceResult = oriResultHolder.getServiceResult();
		boolean preCallExpectServiceResult = oriResultHolder.isExpectServiceResult();
		oriResultHolder.setExpectServiceResult(useFastOriResultFeature);
		ThreadLocal<Boolean> pauseCache = CacheInterceptor.pauseCache;
		Boolean oldValue = pauseCache.get();
		pauseCache.set(Boolean.TRUE);
		try
		{
			Object result = method.invoke(service, serviceDescription.getArguments());
			IServiceResult postCallServiceResult = oriResultHolder.getServiceResult();
			oriResultHolder.setServiceResult(null);

			if (postCallServiceResult == null)
			{
				List<IObjRef> oris = oriHelper.extractObjRefList(result, null, null);
				for (int a = oris.size(); a-- > 0;)
				{
					if (oris.get(a) instanceof IDirectObjRef)
					{
						oris.remove(a);
					}
				}
				postCallServiceResult = new ServiceResult(oris);
			}
			if (result != null)
			{
				IServiceResultProcessor serviceResultProcessor = serviceResultProcessorRegistry.getServiceResultProcessor(result.getClass());
				if (serviceResultProcessor != null)
				{
					// If the given result can be processed, set the result as additional information
					((ServiceResult) postCallServiceResult).setAdditionalInformation(result);
				}
			}
			return postCallServiceResult;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			if (oldValue != null)
			{
				pauseCache.set(oldValue);
			}
			else
			{
				pauseCache.remove();
			}
			oriResultHolder.clearResult();
			oriResultHolder.setExpectServiceResult(preCallExpectServiceResult);
			if (preCallServiceResult != null)
			{
				oriResultHolder.setServiceResult(preCallServiceResult);
			}
		}
	}

	protected Object getRealTargetOfService(Object service)
	{
		Object realTarget = service;

		while (realTarget instanceof Factory)
		{
			Factory factory = (Factory) realTarget;
			Object callback = factory.getCallback(0);
			boolean breakLoop = true;
			while (callback instanceof ICascadedInterceptor)
			{
				breakLoop = false;
				// Move the target-pointer to the target of the interceptor
				realTarget = ((ICascadedInterceptor) callback).getTarget();
				// Move callback pointer to support interceptor-chains
				callback = realTarget;
			}
			if (breakLoop)
			{
				break;
			}
		}
		return realTarget;
	}

	protected QueryBehaviorType findQueryBehaviorType(Object service, Method method)
	{
		Method methodOnTarget;
		try
		{
			methodOnTarget = service.getClass().getMethod(method.getName(), method.getParameterTypes());
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		QueryBehavior queryBehavior = AnnotationUtil.getFirstAnnotation(queryBehaviorCache, methodOnTarget, service.getClass(), method,
				method.getDeclaringClass());

		if (queryBehavior == null)
		{
			return defaultQueryBehaviorType;
		}
		return queryBehavior.value();
	}

}
