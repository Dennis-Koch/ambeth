package com.koch.ambeth.cache.server;

/*-
 * #%L
 * jambeth-cache-server
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

import java.lang.reflect.Method;
import java.util.List;

import com.koch.ambeth.cache.ExecuteServiceDelegate;
import com.koch.ambeth.cache.IServiceResultCache;
import com.koch.ambeth.cache.annotation.QueryBehavior;
import com.koch.ambeth.cache.annotation.QueryBehaviorType;
import com.koch.ambeth.cache.config.CacheConfigurationConstants;
import com.koch.ambeth.cache.interceptor.CacheInterceptor;
import com.koch.ambeth.cache.service.ICacheService;
import com.koch.ambeth.filter.IPagingResponse;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IObjRefHelper;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.model.IDirectObjRef;
import com.koch.ambeth.merge.security.ISecurityScopeProvider;
import com.koch.ambeth.service.IServiceByNameProvider;
import com.koch.ambeth.service.cache.IServiceResultHolder;
import com.koch.ambeth.service.cache.IServiceResultProcessor;
import com.koch.ambeth.service.cache.IServiceResultProcessorRegistry;
import com.koch.ambeth.service.cache.model.ILoadContainer;
import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.cache.model.IObjRelationResult;
import com.koch.ambeth.service.cache.model.IServiceResult;
import com.koch.ambeth.service.cache.transfer.ServiceResult;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.model.ISecurityScope;
import com.koch.ambeth.service.model.IServiceDescription;
import com.koch.ambeth.util.EqualsUtil;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.annotation.AnnotationCache;
import com.koch.ambeth.util.annotation.AnnotationUtil;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import com.koch.ambeth.util.proxy.ICascadedInterceptor;

import net.sf.cglib.proxy.Factory;

public class CacheService implements ICacheService, IInitializingBean, ExecuteServiceDelegate {
	@LogInstance
	private ILogger log;

	protected final AnnotationCache<QueryBehavior> queryBehaviorCache =
			new AnnotationCache<QueryBehavior>(QueryBehavior.class) {
				@Override
				protected boolean annotationEquals(QueryBehavior left, QueryBehavior right) {
					return EqualsUtil.equals(left.value(), right.value());
				}
			};

	@Autowired
	protected ICache cache;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IObjRefHelper oriHelper;

	@Autowired
	protected IServiceResultHolder oriResultHolder;

	@Autowired(optional = true)
	protected ISecurityScopeProvider securityScopeProvider;

	@Autowired
	protected IServiceByNameProvider serviceByNameProvider;

	@Autowired
	protected IServiceResultCache serviceResultCache;

	@Autowired
	protected IServiceResultProcessorRegistry serviceResultProcessorRegistry;

	@Property(name = CacheConfigurationConstants.QueryBehavior, mandatory = false)
	protected QueryBehaviorType defaultQueryBehaviorType;

	@Override
	public void afterPropertiesSet() {
		if (defaultQueryBehaviorType == null) {
			defaultQueryBehaviorType = QueryBehaviorType.DEFAULT;
		}
		if (securityScopeProvider == null) {
			if (log.isDebugEnabled()) {
				log.debug("No securityScopeProvider found. Recovering work without security scope");
			}
		}
	}

	@Override
	public IList<IObjRelationResult> getRelations(List<IObjRelation> objRelations) {
		ParamChecker.assertParamNotNull(objRelations, "objRelations");

		return cache.getObjRelations(objRelations, CacheDirective.none());
	}

	@Override
	public IList<ILoadContainer> getEntities(List<IObjRef> orisToLoad) {
		ParamChecker.assertParamNotNull(orisToLoad, "orisToLoad");

		IThreadLocalObjectCollector current = objectCollector.getCurrent();

		if (log.isDebugEnabled()) {
			StringBuilder sb = current.create(StringBuilder.class);
			try {
				int count = orisToLoad.size();
				sb.append("List<IObjRef> : ").append(count).append(" item");
				if (count != 1) {
					sb.append('s');
				}
				sb.append(" [");

				if (orisToLoad.size() < 100) {
					for (int a = orisToLoad.size(); a-- > 0;) {
						IObjRef oriToLoad = orisToLoad.get(a);
						if (count > 1) {
							sb.append("\r\n\t");
						}
						sb.append(oriToLoad.toString());
					}
				}
				else {
					sb.append("<skipped details>");
				}
				sb.append("]");

				log.debug(sb.toString());
			}
			finally {
				current.dispose(sb);
			}
		}
		List<Object> result = cache.getObjects(orisToLoad, CacheDirective.loadContainerResult());
		try {
			ArrayList<ILoadContainer> lcResult = new ArrayList<>();
			for (int a = 0, size = result.size(); a < size; a++) {
				lcResult.add((ILoadContainer) result.get(a));
			}
			return lcResult;
		}
		finally {
			result.clear();
			result = null;
		}
	}

	@Override
	public IServiceResult getORIsForServiceRequest(IServiceDescription serviceDescription) {
		ParamChecker.assertParamNotNull(serviceDescription, "serviceDescription");

		ISecurityScope[] oldSecurityScopes = null;
		if (securityScopeProvider != null) {
			oldSecurityScopes = securityScopeProvider.getSecurityScopes();
			securityScopeProvider.setSecurityScopes(serviceDescription.getSecurityScopes());
		}
		try {
			return serviceResultCache.getORIsOfService(serviceDescription, this);
		}
		finally {
			if (securityScopeProvider != null) {
				securityScopeProvider.setSecurityScopes(oldSecurityScopes);
			}
		}
	}

	@Override
	public IServiceResult invoke(IServiceDescription serviceDescription) {
		ParamChecker.assertParamNotNull(serviceDescription, "serviceDescription");

		Object service = serviceByNameProvider.getService(serviceDescription.getServiceName());

		if (service == null) {
			throw new IllegalStateException(
					"No service with name '" + serviceDescription.getServiceName() + "' found");
		}
		Method method = serviceDescription.getMethod(service.getClass(), objectCollector);
		if (method == null) {
			throw new IllegalStateException(
					"Requested method not found on service '" + serviceDescription.getServiceName() + "'");
		}
		// Look first at the real instance to support annotations of implementing classes
		Object realService = getRealTargetOfService(service);

		QueryBehaviorType queryBehaviorType = findQueryBehaviorType(realService, method);

		boolean useFastOriResultFeature;
		switch (queryBehaviorType) {
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
		try {
			Object result = method.invoke(service, serviceDescription.getArguments());
			IServiceResult postCallServiceResult = oriResultHolder.getServiceResult();
			oriResultHolder.setServiceResult(null);

			if (postCallServiceResult == null) {
				Object currResult = result;
				if (currResult instanceof IPagingResponse) {
					IPagingResponse<?> pr = (IPagingResponse<?>) currResult;
					List<IObjRef> refResult = pr.getRefResult();
					if (refResult != null) {
						currResult = refResult;
					}
					else {
						currResult = pr.getResult();
					}
				}
				List<IObjRef> oris = oriHelper.extractObjRefList(currResult, null, null);
				for (int a = oris.size(); a-- > 0;) {
					if (oris.get(a) instanceof IDirectObjRef) {
						oris.remove(a);
					}
				}
				postCallServiceResult = new ServiceResult(oris);
			}
			if (result != null) {
				IServiceResultProcessor serviceResultProcessor =
						serviceResultProcessorRegistry.getServiceResultProcessor(result.getClass());
				if (serviceResultProcessor != null) {
					// If the given result can be processed, set the result as additional information
					((ServiceResult) postCallServiceResult).setAdditionalInformation(result);
				}
			}
			return postCallServiceResult;
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		finally {
			if (oldValue != null) {
				pauseCache.set(oldValue);
			}
			else {
				pauseCache.remove();
			}
			oriResultHolder.clearResult();
			oriResultHolder.setExpectServiceResult(preCallExpectServiceResult);
			if (preCallServiceResult != null) {
				oriResultHolder.setServiceResult(preCallServiceResult);
			}
		}
	}

	protected Object getRealTargetOfService(Object service) {
		Object realTarget = service;

		while (realTarget instanceof Factory) {
			Factory factory = (Factory) realTarget;
			Object callback = factory.getCallback(0);
			boolean breakLoop = true;
			while (callback instanceof ICascadedInterceptor) {
				breakLoop = false;
				// Move the target-pointer to the target of the interceptor
				realTarget = ((ICascadedInterceptor) callback).getTarget();
				// Move callback pointer to support interceptor-chains
				callback = realTarget;
			}
			if (breakLoop) {
				break;
			}
		}
		return realTarget;
	}

	protected QueryBehaviorType findQueryBehaviorType(Object service, Method method) {
		Method methodOnTarget;
		try {
			methodOnTarget = service.getClass().getMethod(method.getName(), method.getParameterTypes());
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		QueryBehavior queryBehavior = AnnotationUtil.getFirstAnnotation(queryBehaviorCache,
				methodOnTarget, service.getClass(), method, method.getDeclaringClass());

		if (queryBehavior == null) {
			return defaultQueryBehaviorType;
		}
		return queryBehavior.value();
	}

}
