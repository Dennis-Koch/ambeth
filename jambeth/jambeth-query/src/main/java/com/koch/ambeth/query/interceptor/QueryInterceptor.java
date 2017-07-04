package com.koch.ambeth.query.interceptor;

/*-
 * #%L
 * jambeth-query
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
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import com.koch.ambeth.filter.IFilterDescriptor;
import com.koch.ambeth.filter.IPagingRequest;
import com.koch.ambeth.filter.IPagingResponse;
import com.koch.ambeth.filter.ISortDescriptor;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.ILightweightTransaction;
import com.koch.ambeth.merge.IObjRefHelper;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.query.filter.IFilterToQueryBuilder;
import com.koch.ambeth.query.filter.IPagingQuery;
import com.koch.ambeth.query.squery.GenericTypeUtils;
import com.koch.ambeth.query.squery.ISquery;
import com.koch.ambeth.query.squery.QueryBuilderBean;
import com.koch.ambeth.query.squery.QueryUtils;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.annotation.AnnotationCache;
import com.koch.ambeth.util.annotation.Find;
import com.koch.ambeth.util.annotation.NoProxy;
import com.koch.ambeth.util.annotation.QueryResultType;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.proxy.CascadedInterceptor;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

import net.sf.cglib.proxy.MethodProxy;

public class QueryInterceptor extends CascadedInterceptor {
	protected static final AnnotationCache<Find> findCache = new AnnotationCache<Find>(Find.class) {
		@Override
		protected boolean annotationEquals(Find left, Find right) {
			return left.equals(right);
		}
	};

	protected static final AnnotationCache<NoProxy> noProxyCache = new AnnotationCache<NoProxy>(
			NoProxy.class) {
		@Override
		protected boolean annotationEquals(NoProxy left, NoProxy right) {
			return left.equals(right);
		}
	};

	private static final Pattern PATTERN_QUERY_METHOD = Pattern.compile("(retrieve|read|find|get).*");

	/**
	 * WriteLock for {@link QueryInterceptor#methodMapQueryBuilderBean}
	 */
	protected final Lock writeLock = new ReentrantLock();

	@LogInstance
	private ILogger log;

	@Autowired
	protected ICache cache;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IFilterToQueryBuilder filterToQueryBuilder;

	@Autowired
	protected IObjRefHelper oriHelper;

	@Autowired
	protected IQueryBuilderFactory queryBuilderFactory;

	@Autowired
	protected ILightweightTransaction transaction;

	/**
	 * this map not need ConcurrentHashMap, because {@link QueryInterceptor#retriveQueryBuilderBean}
	 * make thread safe and speed not influenced, never access from other place where not use
	 * {@link QueryInterceptor#readLock} and {@link QueryInterceptor#writeLock}
	 */
	protected final Map<Method, QueryBuilderBean<?>> methodMapQueryBuilderBean = new HashMap<>();

	@Override
	protected Object interceptIntern(Object obj, Method method, Object[] args, MethodProxy proxy)
			throws Throwable {
		if (noProxyCache.getAnnotation(method) != null) {
			return invokeTarget(obj, method, args, proxy);
		}
		String methodName = method.getName().toLowerCase();
		Boolean isAsyncBegin = null;
		if (methodName.startsWith("begin")) {
			isAsyncBegin = Boolean.TRUE;
			methodName = methodName.substring(5);
		}
		else if (methodName.startsWith("end")) {
			isAsyncBegin = Boolean.FALSE;
			methodName = methodName.substring(3);
		}
		return intercept(obj, method, args, proxy, methodName, isAsyncBegin);
	}

	protected Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy,
			String lowerCaseMethodName, Boolean isAsyncBegin) throws Throwable {
		if (target instanceof ISquery && Modifier.isAbstract(method.getModifiers())
				&& QueryUtils.canBuildQuery(method.getName())) {
			QueryBuilderBean<?> queryBuilderBean = getOrCreateQueryBuilderBean(method);
			try {
				return queryBuilderBean.createQueryBuilder(queryBuilderFactory, conversionHelper, args,
						method.getReturnType());
			}
			catch (Throwable e) {
				throw RuntimeExceptionUtil.mask(e,
						"Error occurred while parsing query from '" + method + "'");
			}

		}
		else if (findCache.getAnnotation(method) != null
				|| PATTERN_QUERY_METHOD.matcher(method.getName()).matches()) {
			if (args.length == 3 && IPagingResponse.class.isAssignableFrom(method.getReturnType())) {
				return interceptQuery(obj, method, args, proxy, isAsyncBegin);
			}
			// if (args.length == 1)
			// {
			// return interceptLoad(obj, method, args, proxy, isAsyncBegin);
			// }
		}
		return invokeTarget(obj, method, args, proxy);
	}

	protected Object interceptQuery(Object obj, Method method, Object[] args, MethodProxy proxy,
			Boolean isAsyncBegin) throws Throwable {
		Find findAnnotation = method.getAnnotation(Find.class);
		final QueryResultType resultType;
		final String referenceName;
		if (findAnnotation == null) {
			referenceName = null;
			resultType = QueryResultType.REFERENCES;
		}
		else {
			referenceName = findAnnotation.referenceIdName();
			resultType = findAnnotation.resultType();
		}

		final IPagingRequest pagingRequest = (IPagingRequest) args[0];
		final IFilterDescriptor<?> filterDescriptor = (IFilterDescriptor<?>) args[1];
		final ISortDescriptor[] sortDescriptors = (ISortDescriptor[]) args[2];

		IPagingResponse<?> pagingResponse = transaction
				.runInLazyTransaction(new IResultingBackgroundWorkerDelegate<IPagingResponse<?>>() {
					@Override
					public IPagingResponse<?> invoke() throws Exception {
						IPagingQuery<?> pagingQuery = filterToQueryBuilder.buildQuery(filterDescriptor,
								sortDescriptors);

						switch (resultType) {
							case ENTITIES:
							case BOTH:
								return pagingQuery.retrieve(pagingRequest);
							case REFERENCES:
								if (referenceName == null || referenceName.length() == 0) {
									return pagingQuery.retrieveRefs(pagingRequest);
								}
								return pagingQuery.retrieveRefs(pagingRequest, referenceName);
							default:
								throw RuntimeExceptionUtil.createEnumNotSupportedException(resultType);
						}
					}
				});
		if (QueryResultType.BOTH == resultType) {
			List<?> result = pagingResponse.getResult();
			int size = result.size();
			List<IObjRef> oris = new ArrayList<>(size);
			IEntityMetaData metaData = entityMetaDataProvider
					.getMetaData(filterDescriptor.getEntityType());
			for (int i = 0; i < size; i++) {
				Object entity = result.get(i);
				IObjRef ori = oriHelper.entityToObjRef(entity, ObjRef.PRIMARY_KEY_INDEX, metaData, true);
				oris.add(ori);
			}
			pagingResponse.setRefResult(oris);
		}
		return pagingResponse;
	}

	protected Object interceptLoad(Object obj, Method method, Object[] args, MethodProxy proxy,
			Boolean isAsyncBegin) {
		Class<?> entityType = method.getReturnType();
		if (entityType.isArray()) {
			entityType = entityType.getComponentType();
		}
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType, true);
		if (metaData == null) {
			Type genericReturnType = method.getGenericReturnType();
			if (!(genericReturnType instanceof ParameterizedType)) {
				throw new IllegalArgumentException("Cannot identify return type");
			}
			ParameterizedType castedType = (ParameterizedType) genericReturnType;
			Type[] actualTypeArguments = castedType.getActualTypeArguments();
			if (actualTypeArguments.length != 1) {
				throw new IllegalArgumentException("Generic return type with more than one generic type");
			}
			entityType = (Class<?>) actualTypeArguments[0];
			metaData = entityMetaDataProvider.getMetaData(entityType, true);
			if (metaData == null) {
				throw new IllegalArgumentException("Cannot identify return type");
			}
		}

		Object idsRaw = args[0];
		Class<?> idsClass = idsRaw.getClass();
		if (List.class.isAssignableFrom(idsClass)) {
			List<?> ids = (List<?>) idsRaw;
			return cache.getObjects(entityType, ids);
		}
		else if (Set.class.isAssignableFrom(idsClass)) {
			List<?> ids = new ArrayList<>((Set<?>) idsRaw);
			return cache.getObjects(entityType, ids);
		}
		else if (idsClass.isArray()) {
			throw new IllegalArgumentException("Array of IDs not yet supported");
		}
		else {
			return cache.getObject(entityType, idsRaw);
		}
	}

	/**
	 * get QueryBuilderBean, this object may be from cache or will just be created ad-hoc
	 *
	 * @param obj
	 *          intercepted object
	 * @param method
	 *          intercepted method
	 * @return QueryBuilderBean instance for Squery
	 */
	private QueryBuilderBean<?> getOrCreateQueryBuilderBean(Method method) {
		ParamChecker.assertNotNull(method, "method");

		QueryBuilderBean<?> queryBuilderBean;
		writeLock.lock();
		try {
			queryBuilderBean = methodMapQueryBuilderBean.get(method);
			if (queryBuilderBean != null) {
				return queryBuilderBean;
			}
		}
		finally {
			writeLock.unlock();
		}
		Class<?> entityType = (Class<?>) GenericTypeUtils.getGenericParam(target, ISquery.class)[0];
		queryBuilderBean = QueryUtils.buildQuery(method.getName(), entityType);
		// double check to make thread safe and not influence the speed
		writeLock.lock();
		try {
			QueryBuilderBean<?> existingQueryBuilderBean = methodMapQueryBuilderBean.get(method);
			if (existingQueryBuilderBean != null) {
				return existingQueryBuilderBean;
			}
			methodMapQueryBuilderBean.put(method, queryBuilderBean);
		}
		finally {
			writeLock.unlock();
		}
		return queryBuilderBean;
	}
}
