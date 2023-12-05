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

import com.koch.ambeth.filter.IFilterDescriptor;
import com.koch.ambeth.filter.IPagingRequest;
import com.koch.ambeth.filter.IPagingResponse;
import com.koch.ambeth.filter.ISortDescriptor;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.typeinfo.TypeInfoItemUtil;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IObjRefHelper;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.query.filter.IFilterToQueryBuilder;
import com.koch.ambeth.query.squery.QueryBuilderBean;
import com.koch.ambeth.query.squery.QueryUtils;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.annotation.AnnotationCache;
import com.koch.ambeth.util.annotation.Find;
import com.koch.ambeth.util.annotation.NoProxy;
import com.koch.ambeth.util.annotation.QueryResultType;
import com.koch.ambeth.util.annotation.SmartQuery;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.proxy.CascadedInterceptor;
import com.koch.ambeth.util.proxy.MethodProxy;
import com.koch.ambeth.util.transaction.ILightweightTransaction;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class QueryInterceptor extends CascadedInterceptor {
    public static final String P_METHOD_TO_COMMAND_MAP = "MethodToCommandMap";

    protected static final AnnotationCache<Find> findCache = new AnnotationCache<Find>(Find.class) {
        @Override
        protected boolean annotationEquals(Find left, Find right) {
            return left.equals(right);
        }
    };

    protected static final AnnotationCache<NoProxy> noProxyCache = new AnnotationCache<NoProxy>(NoProxy.class) {
        @Override
        protected boolean annotationEquals(NoProxy left, NoProxy right) {
            return left.equals(right);
        }
    };

    protected final ConcurrentMap<Method, QueryBuilderBean<?>> methodMapQueryBuilderBean = new ConcurrentHashMap<>(16, 0.5f);
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

    @Property
    protected Map<Method, QueryInterceptorCommand> methodToCommandMap;
    @LogInstance
    private ILogger log;

    @Override
    protected Object interceptIntern(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        var command = methodToCommandMap.get(method);
        if (command == null) {
            return invokeTarget(obj, method, args, proxy);
        }
        return command.intercept(this, obj, method, args, proxy);
    }

    public Object interceptFind(Object obj, Method method, Object[] args, MethodProxy proxy, Boolean isAsyncBegin, Find find) throws Throwable {
        final QueryResultType resultType;
        final String referenceName;
        if (find == null) {
            referenceName = null;
            resultType = QueryResultType.REFERENCES;
        } else {
            referenceName = find.referenceIdName();
            resultType = find.resultType();
        }

        var pagingRequest = (IPagingRequest) args[0];
        var filterDescriptor = (IFilterDescriptor<?>) args[1];
        var sortDescriptors = (ISortDescriptor[]) args[2];

        var pagingResponse = transaction.runInLazyTransaction(() -> {
            var pagingQuery = filterToQueryBuilder.buildQuery(filterDescriptor, sortDescriptors);

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
        });
        if (QueryResultType.BOTH == resultType) {
            var result = pagingResponse.getResult();
            var size = result.size();
            var oris = new ArrayList<IObjRef>(size);
            var metaData = entityMetaDataProvider.getMetaData(filterDescriptor.getEntityType());
            for (int i = 0; i < size; i++) {
                var entity = result.get(i);
                var ori = oriHelper.entityToObjRef(entity, ObjRef.PRIMARY_KEY_INDEX, metaData, true);
                oris.add(ori);
            }
            pagingResponse.setRefResult(oris);
        }
        return pagingResponse;
    }

    protected Object interceptLoad(Object obj, Method method, Object[] args, MethodProxy proxy, Boolean isAsyncBegin) {
        var entityType = method.getReturnType();
        if (entityType.isArray()) {
            entityType = entityType.getComponentType();
        }
        var metaData = entityMetaDataProvider.getMetaData(entityType, true);
        if (metaData == null) {
            var genericReturnType = method.getGenericReturnType();
            if (!(genericReturnType instanceof ParameterizedType)) {
                throw new IllegalArgumentException("Cannot identify return type");
            }
            var castedType = (ParameterizedType) genericReturnType;
            var actualTypeArguments = castedType.getActualTypeArguments();
            if (actualTypeArguments.length != 1) {
                throw new IllegalArgumentException("Generic return type with more than one generic type");
            }
            entityType = (Class<?>) actualTypeArguments[0];
            metaData = entityMetaDataProvider.getMetaData(entityType, true);
            if (metaData == null) {
                throw new IllegalArgumentException("Cannot identify return type");
            }
        }

        var idsRaw = args[0];
        var idsClass = idsRaw.getClass();
        if (List.class.isAssignableFrom(idsClass)) {
            var ids = (List<?>) idsRaw;
            return cache.getObjects(entityType, ids);
        } else if (Set.class.isAssignableFrom(idsClass)) {
            var ids = new ArrayList<>((Set<?>) idsRaw);
            return cache.getObjects(entityType, ids);
        } else if (idsClass.isArray()) {
            throw new IllegalArgumentException("Array of IDs not yet supported");
        } else {
            return cache.getObject(entityType, idsRaw);
        }
    }

    public Object interceptSmartQuery(Object obj, Method method, Object[] args, MethodProxy proxy, Boolean isAsyncBegin, SmartQuery smartQuery) {
        var queryBuilderBean = getOrCreateQueryBuilderBean(method, smartQuery);
        try {
            return queryBuilderBean.createQueryBuilder(queryBuilderFactory, conversionHelper, args, method.getReturnType());
        } catch (Throwable e) {
            throw RuntimeExceptionUtil.mask(e, "Error occurred while parsing query from '" + method + "'");
        }
    }

    /**
     * get QueryBuilderBean, this object may be from cache or will just be created ad-hoc
     *
     * @param method     intercepted method
     * @param smartQuery the mode in which generic method behavior is expected
     * @return QueryBuilderBean instance for Squery
     */
    protected QueryBuilderBean<?> getOrCreateQueryBuilderBean(Method method, SmartQuery smartQuery) {
        ParamChecker.assertNotNull(method, "method");

        var queryBuilderBean = methodMapQueryBuilderBean.get(method);
        if (queryBuilderBean != null) {
            return queryBuilderBean;
        }
        Class<?> entityType;
        if (method.getReturnType() == IPagingResponse.class) {
            var castedType = (ParameterizedType) method.getGenericReturnType();
            var actualTypeArguments = castedType.getActualTypeArguments();
            entityType = TypeInfoItemUtil.getElementTypeUsingReflection(null, actualTypeArguments[0]);
        } else {
            entityType = TypeInfoItemUtil.getElementTypeUsingReflection(method.getReturnType(), method.getGenericReturnType());
        }
        var metaData = entityMetaDataProvider.getMetaData(entityType, true);
        if (metaData == null) {
            var annotationEntityType = smartQuery.entityType();
            if (annotationEntityType == Object.class) {
                throw new IllegalArgumentException(
                        "Could not resolve an applicable entity type for method '" + method + "'. Please check the signature and/or consider to use the @" + SmartQuery.class.getSimpleName() + " " +
                                "annotation with an explicitly defined entity type for this method");
            }
            entityType = annotationEntityType;
        }
        queryBuilderBean = QueryUtils.buildQuery(method.getName(), entityType);
        var existingQueryBuilderBean = methodMapQueryBuilderBean.putIfAbsent(method, queryBuilderBean);
        if (existingQueryBuilderBean != null) {
            // concurrent thread was faster
            return existingQueryBuilderBean;
        }
        return queryBuilderBean;
    }
}
