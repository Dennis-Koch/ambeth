package com.koch.ambeth.cache.interceptor;

/*-
 * #%L
 * jambeth-cache
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.koch.ambeth.cache.Cached;
import com.koch.ambeth.cache.service.ICacheService;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.typeinfo.TypeInfoItemUtil;
import com.koch.ambeth.ioc.util.ImmutableTypeSet;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.interceptor.MergeInterceptor;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.service.SyncToAsyncUtil;
import com.koch.ambeth.service.cache.IServiceResultHolder;
import com.koch.ambeth.service.cache.IServiceResultProcessor;
import com.koch.ambeth.service.cache.IServiceResultProcessorRegistry;
import com.koch.ambeth.service.cache.model.IServiceResult;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.service.model.ISecurityScope;
import com.koch.ambeth.service.transfer.ServiceDescription;
import com.koch.ambeth.util.ListUtil;
import com.koch.ambeth.util.annotation.Find;
import com.koch.ambeth.util.annotation.QueryResultType;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.threading.SensitiveThreadLocal;

import net.sf.cglib.proxy.MethodProxy;

public class CacheInterceptor extends MergeInterceptor {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	public static final ThreadLocal<Boolean> pauseCache = new SensitiveThreadLocal<>();

	@Autowired
	protected ICacheService cacheService;

	@Autowired
	protected ICache cache;

	@Autowired
	protected IServiceResultHolder serviceResultHolder;

	@Autowired
	protected IServiceResultProcessorRegistry serviceResultProcessorRegistry;

	@Override
	protected Object interceptLoad(Object obj, Method method, Object[] args, MethodProxy proxy,
			Annotation annotation, Boolean isAsyncBegin) throws Throwable {
		ServiceDescription serviceDescription;
		IServiceResult serviceResult;

		Cached cached = annotation instanceof Cached ? (Cached) annotation : null;
		if (cached == null && (Boolean.TRUE.equals(pauseCache.get())
				|| !serviceResultHolder.isExpectServiceResult())) {
			return super.interceptLoad(obj, method, args, proxy, annotation, isAsyncBegin);
		}

		Class<?> returnType = method.getReturnType();
		if (ImmutableTypeSet.isImmutableType(returnType)) {
			// No possible result which might been read by cache
			return super.interceptLoad(obj, method, args, proxy, annotation, isAsyncBegin);
		}
		if (cached == null) {
			ISecurityScope[] securityScopes = securityScopeProvider.getSecurityScopes();
			serviceDescription =
					SyncToAsyncUtil.createServiceDescription(serviceName, method, args, securityScopes);
			serviceResult = cacheService.getORIsForServiceRequest(serviceDescription);
			return createResultObject(serviceResult, returnType, args, annotation);
		}

		if (args.length != 1) {
			throw new IllegalArgumentException(
					"This annotation is only allowed on methods with exactly 1 argument. Please check your "
							+ Cached.class.toString() + " annotation on method " + method.toString());
		}
		Class<?> entityType = cached.type();
		if (entityType == null || void.class.equals(entityType)) {
			entityType =
					TypeInfoItemUtil.getElementTypeUsingReflection(returnType, method.getGenericReturnType());
		}
		if (entityType == null || void.class.equals(entityType)) {
			throw new IllegalArgumentException("Please specify a valid returnType for the "
					+ Cached.class.getSimpleName() + " annotation on method " + method.toString());
		}
		IEntityMetaData metaData = getSpecifiedMetaData(method, Cached.class, entityType);
		Member member = getSpecifiedMember(method, Cached.class, metaData, cached.alternateIdName());

		byte idIndex;
		try {
			idIndex = metaData.getIdIndexByMemberName(member.getName());
		}
		catch (RuntimeException e) {
			throw new IllegalArgumentException(
					"Member " + entityType.getName() + "." + cached.alternateIdName()
							+ " is not configured as an alternate ID member. There must be a single-column unique constraint on the respective table column. Please check your "
							+ Cached.class.toString() + " annotation on method " + method.toString(),
					e);
		}
		boolean returnMisses = cached.returnMisses();
		ArrayList<IObjRef> orisToGet = new ArrayList<>();
		fillOrisToGet(orisToGet, args, entityType, idIndex, returnMisses);
		return createResultObject(orisToGet, returnType, returnMisses, annotation);
	}

	protected void fillOrisToGet(List<IObjRef> orisToGet, Object[] args, Class<?> entityType,
			byte idIndex, boolean returnMisses) {
		Object argument = args[0];
		if (argument instanceof List) {
			List<?> list = (List<?>) argument;
			for (int a = 0, size = list.size(); a < size; a++) {
				Object id = list.get(a);
				if (id == null) {
					if (returnMisses) {
						orisToGet.add(null);
					}
					else {
						continue;
					}
				}
				ObjRef objRef = new ObjRef(entityType, idIndex, id, null);
				orisToGet.add(objRef);
			}
		}
		else if (argument instanceof Collection) {
			Iterator<?> iter = ((Collection<?>) argument).iterator();
			while (iter.hasNext()) {
				Object id = iter.next();
				if (id == null) {
					if (returnMisses) {
						orisToGet.add(null);
					}
					else {
						continue;
					}
				}
				ObjRef objRef = new ObjRef(entityType, idIndex, id, null);
				orisToGet.add(objRef);
			}
		}

		else if (argument.getClass().isArray()) {
			for (int a = 0, size = Array.getLength(argument); a < size; a++) {
				Object id = Array.get(argument, a);
				if (id == null) {
					if (returnMisses) {
						orisToGet.add(null);
					}
					else {
						continue;
					}
				}
				ObjRef objRef = new ObjRef(entityType, idIndex, id, null);
				orisToGet.add(objRef);
			}
		}
		else {
			ObjRef objRef = new ObjRef(entityType, idIndex, argument, null);
			orisToGet.add(objRef);
		}
	}

	protected Object createResultObject(IServiceResult serviceResult, Class<?> expectedType,
			Object[] originalArgs, Annotation annotation) {
		List<IObjRef> objRefs = serviceResult.getObjRefs();
		IList<Object> syncObjects = null;
		if (!(annotation instanceof Find)
				|| ((Find) annotation).resultType() != QueryResultType.REFERENCES) {
			syncObjects = cache.getObjects(objRefs, CacheDirective.none());
		}
		return postProcessCacheResult(objRefs, syncObjects, expectedType, serviceResult, originalArgs,
				annotation);
	}

	protected Object createResultObject(List<IObjRef> objRefs, Class<?> expectedType,
			boolean returnMisses, Annotation annotation) {
		IList<Object> syncObjects = cache.getObjects(objRefs,
				returnMisses ? CacheDirective.returnMisses() : CacheDirective.none());
		return postProcessCacheResult(objRefs, syncObjects, expectedType, null, null, annotation);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	protected Object postProcessCacheResult(List<IObjRef> objRefs, IList<Object> cacheResult,
			Class<?> expectedType, IServiceResult serviceResult, Object[] originalArgs,
			Annotation annotation) {
		int cacheResultSize = cacheResult != null ? cacheResult.size() : objRefs.size();
		if (Collection.class.isAssignableFrom(expectedType)) {
			Collection targetCollection = ListUtil.createCollectionOfType(expectedType, cacheResultSize);

			if (cacheResult != null) {
				for (int a = 0; a < cacheResultSize; a++) {
					targetCollection.add(cacheResult.get(a));
				}
			}
			else {
				for (int a = 0; a < cacheResultSize; a++) {
					targetCollection.add(objRefs.get(a));
				}
			}
			return targetCollection;
		}
		else if (expectedType.isArray()) {
			Object[] array =
					(Object[]) Array.newInstance(expectedType.getComponentType(), cacheResultSize);

			if (cacheResult != null) {
				for (int a = 0; a < cacheResultSize; a++) {
					array[a] = cacheResult.get(a);
				}
			}
			else {
				for (int a = 0; a < cacheResultSize; a++) {
					array[a] = objRefs.get(a);
				}
			}
			return array;
		}
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(expectedType, true);
		if (metaData != null) {
			// It is a simple entity which can be returned directly
			if (cacheResultSize == 0) {
				return null;
			}
			else if (cacheResultSize == 1) {
				return cacheResult != null ? cacheResult.get(0) : objRefs.get(0);
			}
		}
		Object additionalInformation =
				serviceResult != null ? serviceResult.getAdditionalInformation() : null;
		if (additionalInformation == null) {
			throw new IllegalStateException("Can not convert list of " + cacheResultSize
					+ " results from cache to type " + expectedType.getName());
		}
		IServiceResultProcessor serviceResultProcessor =
				serviceResultProcessorRegistry.getServiceResultProcessor(expectedType);
		return serviceResultProcessor.processServiceResult(additionalInformation, objRefs, cacheResult,
				expectedType, originalArgs, annotation);
	}

	@Override
	protected void buildObjRefs(Class<?> entityType, byte idIndex, Class<?> idType, Object ids,
			List<IObjRef> objRefs) {
		if (ids == null) {
			return;
		}
		List<Object> idsList = ListUtil.anyToList(ids);

		for (int a = idsList.size(); a-- > 0;) {
			Object id = idsList.get(a);
			Object convertedId = conversionHelper.convertValueToType(idType, id);

			objRefs.add(new ObjRef(entityType, idIndex, convertedId, null));
		}

		IList<?> objects = cache.getObjects(objRefs, CacheDirective.returnMisses());
		for (int a = objects.size(); a-- > 0;) {
			Object obj = objects.get(a);
			IObjRef objRef = objRefs.get(a);
			if (obj == null) {
				throw new IllegalStateException("Could not retrieve object " + objRef);
			}
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(obj.getClass());
			Object version = metaData.getVersionMember().getValue(obj, false);
			objRef.setVersion(version);
		}
	}
}
