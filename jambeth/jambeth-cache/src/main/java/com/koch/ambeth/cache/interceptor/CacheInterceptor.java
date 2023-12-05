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

import com.koch.ambeth.cache.Cached;
import com.koch.ambeth.cache.service.ICacheService;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.typeinfo.TypeInfoItemUtil;
import com.koch.ambeth.ioc.util.IImmutableTypeSet;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.interceptor.MergeInterceptor;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.service.SyncToAsyncUtil;
import com.koch.ambeth.service.cache.IServiceResultHolder;
import com.koch.ambeth.service.cache.IServiceResultProcessorRegistry;
import com.koch.ambeth.service.cache.model.IServiceResult;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.IDTOType;
import com.koch.ambeth.service.transfer.ServiceDescription;
import com.koch.ambeth.util.ListUtil;
import com.koch.ambeth.util.annotation.Find;
import com.koch.ambeth.util.annotation.QueryResultType;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.proxy.MethodProxy;
import com.koch.ambeth.util.threading.SensitiveThreadLocal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

public class CacheInterceptor extends MergeInterceptor {
    public static final ThreadLocal<Boolean> pauseCache = new SensitiveThreadLocal<>();

    @Autowired
    protected ICacheService cacheService;

    @Autowired
    protected ICache cache;

    @Autowired
    protected IImmutableTypeSet immutableTypeSet;

    @Autowired(optional = true)
    protected IServiceResultHolder serviceResultHolder;

    @Autowired
    protected IServiceResultProcessorRegistry serviceResultProcessorRegistry;

    @Override
    protected Object interceptLoad(Object obj, Method method, Object[] args, MethodProxy proxy, Annotation annotation, Boolean isAsyncBegin) throws Throwable {
        ServiceDescription serviceDescription;
        IServiceResult serviceResult;

        var cached = annotation instanceof Cached ? (Cached) annotation : null;
        if (cached == null && (Boolean.TRUE.equals(pauseCache.get()) || serviceResultHolder != null && !serviceResultHolder.isExpectServiceResult())) {
            return super.interceptLoad(obj, method, args, proxy, annotation, isAsyncBegin);
        }

        var returnType = method.getReturnType();
        if (immutableTypeSet.isImmutableType(returnType) || IDTOType.class.isAssignableFrom(returnType)) {
            // No possible result which might been read by cache
            return super.interceptLoad(obj, method, args, proxy, annotation, isAsyncBegin);
        }
        if (cached == null) {
            var securityScopes = securityScopeProvider.getSecurityScopes();
            serviceDescription = SyncToAsyncUtil.createServiceDescription(serviceName, method, args, securityScopes);
            serviceResult = cacheService.getORIsForServiceRequest(serviceDescription);
            return createResultObject(serviceResult, returnType, args, annotation);
        }

        if (args.length != 1) {
            throw new IllegalArgumentException(
                    "This annotation is only allowed on methods with exactly 1 argument. Please check your " + Cached.class.toString() + " annotation on method " + method.toString());
        }
        var entityType = cached.type();
        if (entityType == null || void.class.equals(entityType)) {
            entityType = TypeInfoItemUtil.getElementTypeUsingReflection(returnType, method.getGenericReturnType());
        }
        if (entityType == null || void.class.equals(entityType)) {
            throw new IllegalArgumentException("Please specify a valid returnType for the " + Cached.class.getSimpleName() + " annotation on method " + method.toString());
        }
        var metaData = getSpecifiedMetaData(method, Cached.class, entityType);
        var member = getSpecifiedMember(method, Cached.class, metaData, cached.alternateIdName());

        byte idIndex;
        try {
            idIndex = metaData.getIdIndexByMemberName(member.getName());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Member " + entityType.getName() + "." + cached.alternateIdName() +
                    " is not configured as an alternate ID member. There must be a single-column unique constraint on the respective table column. Please check your " + Cached.class.toString() +
                    " annotation on method " + method.toString(), e);
        }
        var returnMisses = cached.returnMisses();
        var orisToGet = new ArrayList<IObjRef>();
        fillOrisToGet(orisToGet, args, entityType, idIndex, returnMisses);
        return createResultObject(orisToGet, returnType, returnMisses, annotation);
    }

    protected void fillOrisToGet(List<IObjRef> orisToGet, Object[] args, Class<?> entityType, byte idIndex, boolean returnMisses) {
        var argument = args[0];
        if (argument instanceof List) {
            var list = (List<?>) argument;
            for (int a = 0, size = list.size(); a < size; a++) {
                var id = list.get(a);
                if (id == null) {
                    if (returnMisses) {
                        orisToGet.add(null);
                    } else {
                        continue;
                    }
                }
                var objRef = new ObjRef(entityType, idIndex, id, null);
                orisToGet.add(objRef);
            }
        } else if (argument instanceof Collection) {
            var iter = ((Collection<?>) argument).iterator();
            while (iter.hasNext()) {
                var id = iter.next();
                if (id == null) {
                    if (returnMisses) {
                        orisToGet.add(null);
                    } else {
                        continue;
                    }
                }
                var objRef = new ObjRef(entityType, idIndex, id, null);
                orisToGet.add(objRef);
            }
        } else if (argument.getClass().isArray()) {
            for (int a = 0, size = Array.getLength(argument); a < size; a++) {
                var id = Array.get(argument, a);
                if (id == null) {
                    if (returnMisses) {
                        orisToGet.add(null);
                    } else {
                        continue;
                    }
                }
                var objRef = new ObjRef(entityType, idIndex, id, null);
                orisToGet.add(objRef);
            }
        } else {
            var objRef = new ObjRef(entityType, idIndex, argument, null);
            orisToGet.add(objRef);
        }
    }

    protected Object createResultObject(IServiceResult serviceResult, Class<?> expectedType, Object[] originalArgs, Annotation annotation) {
        var objRefs = serviceResult.getObjRefs();
        IList<Object> syncObjects = null;
        if (!(annotation instanceof Find) || ((Find) annotation).resultType() != QueryResultType.REFERENCES) {
            syncObjects = cache.getObjects(objRefs, CacheDirective.none());
        }
        return postProcessCacheResult(objRefs, syncObjects, expectedType, serviceResult, originalArgs, annotation);
    }

    protected Object createResultObject(List<IObjRef> objRefs, Class<?> expectedType, boolean returnMisses, Annotation annotation) {
        var syncObjects = cache.getObjects(objRefs, returnMisses ? CacheDirective.returnMisses() : CacheDirective.none());
        return postProcessCacheResult(objRefs, syncObjects, expectedType, null, null, annotation);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected Object postProcessCacheResult(List<IObjRef> objRefs, IList<Object> cacheResult, Class<?> expectedType, IServiceResult serviceResult, Object[] originalArgs, Annotation annotation) {
        var cacheResultSize = cacheResult != null ? cacheResult.size() : objRefs.size();
        if (Collection.class.isAssignableFrom(expectedType)) {
            var targetCollection = ListUtil.createCollectionOfType(expectedType, cacheResultSize);

            if (cacheResult != null) {
                for (int a = 0; a < cacheResultSize; a++) {
                    targetCollection.add(cacheResult.get(a));
                }
            } else {
                for (int a = 0; a < cacheResultSize; a++) {
                    targetCollection.add(objRefs.get(a));
                }
            }
            return targetCollection;
        } else if (expectedType.isArray()) {
            var array = (Object[]) Array.newInstance(expectedType.getComponentType(), cacheResultSize);

            if (cacheResult != null) {
                for (int a = 0; a < cacheResultSize; a++) {
                    array[a] = cacheResult.get(a);
                }
            } else {
                for (int a = 0; a < cacheResultSize; a++) {
                    array[a] = objRefs.get(a);
                }
            }
            return array;
        }
        var metaData = entityMetaDataProvider.getMetaData(expectedType, true);
        if (metaData != null) {
            // It is a simple entity which can be returned directly
            if (cacheResultSize == 0) {
                return null;
            } else if (cacheResultSize == 1) {
                return cacheResult != null ? cacheResult.get(0) : objRefs.get(0);
            }
        }
        var additionalInformation = serviceResult != null ? serviceResult.getAdditionalInformation() : null;
        if (additionalInformation == null) {
            throw new IllegalStateException("Can not convert list of " + cacheResultSize + " results from cache to type " + expectedType.getName());
        }
        var serviceResultProcessor = serviceResultProcessorRegistry.getServiceResultProcessor(expectedType);
        return serviceResultProcessor.processServiceResult(additionalInformation, objRefs, cacheResult, expectedType, originalArgs, annotation);
    }

    @Override
    protected void buildObjRefs(Class<?> entityType, byte idIndex, Class<?> idType, Object ids, List<IObjRef> objRefs) {
        if (ids == null) {
            return;
        }
        var idsList = ListUtil.anyToList(ids);

        for (int a = idsList.size(); a-- > 0; ) {
            var id = idsList.get(a);
            var convertedId = conversionHelper.convertValueToType(idType, id);

            objRefs.add(new ObjRef(entityType, idIndex, convertedId, null));
        }

        var objects = cache.getObjects(objRefs, CacheDirective.returnMisses());
        for (int a = objects.size(); a-- > 0; ) {
            var obj = objects.get(a);
            var objRef = objRefs.get(a);
            if (obj == null) {
                throw new IllegalStateException("Could not retrieve object " + objRef);
            }
            var metaData = entityMetaDataProvider.getMetaData(obj.getClass());
            var version = metaData.getVersionMember().getValue(obj, false);
            objRef.setVersion(version);
        }
    }
}
