package com.koch.ambeth.cache;

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

import com.koch.ambeth.cache.service.ICacheRetriever;
import com.koch.ambeth.cache.service.ICacheRetrieverExtendable;
import com.koch.ambeth.cache.service.ICacheService;
import com.koch.ambeth.cache.service.ICacheServiceByNameExtendable;
import com.koch.ambeth.cache.service.IPrimitiveRetriever;
import com.koch.ambeth.cache.service.IPrimitiveRetrieverExtendable;
import com.koch.ambeth.cache.service.IRelationRetriever;
import com.koch.ambeth.cache.service.IRelationRetrieverExtendable;
import com.koch.ambeth.cache.transfer.ObjRelation;
import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.event.events.EventSessionChanged;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.exception.ExtendableException;
import com.koch.ambeth.ioc.extendable.ClassExtendableContainer;
import com.koch.ambeth.ioc.extendable.MapExtendableContainer;
import com.koch.ambeth.ioc.util.IMultithreadingHelper;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IObjRefHelper;
import com.koch.ambeth.merge.event.RefreshEntitiesOfType;
import com.koch.ambeth.service.cache.model.ILoadContainer;
import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.cache.model.IObjRelationResult;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.remote.IRemoteInterceptor;
import com.koch.ambeth.util.IDisposable;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IdentityLinkedMap;
import com.koch.ambeth.util.function.CheckedConsumer;
import com.koch.ambeth.util.proxy.Factory;
import com.koch.ambeth.util.proxy.ICascadedInterceptor;

import java.util.List;
import java.util.Objects;

public class CacheRetrieverRegistry implements ICacheRetriever, ICacheRetrieverExtendable, IPrimitiveRetrieverExtendable, IRelationRetrieverExtendable, ICacheServiceByNameExtendable {

    public static final String HANDLE_EVENT_SESSION_CHANGED = "handleEventSessionChanged";
    protected final ClassExtendableContainer<ICacheRetriever> typeToCacheRetrieverMap = new ClassExtendableContainer<>("cacheRetriever", "entityType");
    protected final ClassExtendableContainer<HashMap<String, IRelationRetriever>> typeToRelationRetrieverEC = new ClassExtendableContainer<>("relationRetriever", "handledType");
    protected final ClassExtendableContainer<HashMap<String, IPrimitiveRetriever>> typeToPrimitiveRetrieverEC = new ClassExtendableContainer<>("primitiveRetriever", "handledType");
    protected final MapExtendableContainer<String, ICacheService> nameToCacheServiceEC = new MapExtendableContainer<>("cacheService", "serviceName");
    @Autowired(optional = true)
    protected ICacheRetriever defaultCacheRetriever;
    @Autowired
    protected IEntityMetaDataProvider entityMetaDataProvider;
    @Autowired
    protected IEventDispatcher eventDispatcher;
    @Autowired
    protected IMultithreadingHelper multithreadingHelper;
    @Autowired
    protected IObjRefHelper objRefHelper;
    @LogInstance
    private ILogger log;

    @Override
    public void registerCacheRetriever(ICacheRetriever cacheRetriever, Class<?> handledType) {
        typeToCacheRetrieverMap.register(cacheRetriever, handledType);
    }

    @Override
    public void unregisterCacheRetriever(ICacheRetriever cacheRetriever, Class<?> handledType) {
        typeToCacheRetrieverMap.unregister(cacheRetriever, handledType);
    }

    @Override
    public void registerRelationRetriever(IRelationRetriever relationRetriever, Class<?> handledType, String propertyName) {
        registerPropertyRetriever(typeToRelationRetrieverEC, relationRetriever, handledType, propertyName);
    }

    @Override
    public void unregisterRelationRetriever(IRelationRetriever relationRetriever, Class<?> handledType, String propertyName) {
        unregisterPropertyRetriever(typeToRelationRetrieverEC, relationRetriever, handledType, propertyName);
    }

    @Override
    public void registerPrimitiveRetriever(IPrimitiveRetriever primitiveRetriever, Class<?> handledType, String propertyName) {
        registerPropertyRetriever(typeToPrimitiveRetrieverEC, primitiveRetriever, handledType, propertyName);
    }

    @Override
    public void unregisterPrimitiveRetriever(IPrimitiveRetriever primitiveRetriever, Class<?> handledType, String propertyName) {
        unregisterPropertyRetriever(typeToPrimitiveRetrieverEC, primitiveRetriever, handledType, propertyName);
    }

    protected <E> void registerPropertyRetriever(ClassExtendableContainer<HashMap<String, E>> extendableContainer, E extension, Class<?> handledType, String propertyName) {
        var writeLock = extendableContainer.getWriteLock();
        writeLock.lock();
        try {
            var map = extendableContainer.getExtension(handledType);
            if (map == null) {
                map = new HashMap<>();
                extendableContainer.register(map, handledType);
            }
            if (!map.putIfNotExists(propertyName, extension)) {
                throw new ExtendableException("Key '" + handledType.getName() + "." + propertyName + "' already added");
            }
        } finally {
            writeLock.unlock();
        }
    }

    protected <E> void unregisterPropertyRetriever(ClassExtendableContainer<HashMap<String, E>> extendableContainer, E extension, Class<?> handledType, String propertyName) {
        var writeLock = extendableContainer.getWriteLock();
        writeLock.lock();
        try {
            var map = extendableContainer.getExtension(handledType);
            if (map == null || !map.removeIfValue(propertyName, extension)) {
                throw new ExtendableException("Provided extension is not registered at key '" + handledType + "." + propertyName + "'. Extension: " + extension);
            }
            if (map.isEmpty()) {
                extendableContainer.unregister(map, handledType);
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void registerCacheService(ICacheService cacheService, String serviceName) {
        nameToCacheServiceEC.register(cacheService, serviceName);
    }

    @Override
    public void unregisterCacheService(ICacheService cacheService, String serviceName) {
        nameToCacheServiceEC.unregister(cacheService, serviceName);
    }

    @Override
    public List<ILoadContainer> getEntities(List<IObjRef> orisToLoad) {
        ParamChecker.assertParamNotNull(orisToLoad, "orisToLoad");

        var result = getEntitiesIntern(orisToLoad);
        var objRelToDelegateMap = new IdentityLinkedMap<IObjRelation, CheckedConsumer<Object>>();
        var fetchablePrimitives = bucketSortObjRelsForFetchablePrimitives(result, objRelToDelegateMap);

        if (fetchablePrimitives.isEmpty()) {
            return result;
        }
        multithreadingHelper.invokeAndWait(fetchablePrimitives, item -> {
            var value = item.getValue();
            return item.getKey().getPrimitives(value.objRelations, value.loadContainers);
        }, (resultOfFork, itemOfFork) -> {
            var objRels = itemOfFork.getValue().objRelations;

            for (int a = objRels.size(); a-- > 0; ) {
                var objRel = objRels.get(a);
                var delegate = objRelToDelegateMap.get(objRel);
                CheckedConsumer.invoke(delegate, resultOfFork[a]);
            }
        });

        return result;
    }

    protected IList<ILoadContainer> getEntitiesIntern(List<IObjRef> orisToLoad) {
        var result = new ArrayList<ILoadContainer>(orisToLoad.size());

        var assignedObjRefs = bucketSortObjRefs(orisToLoad);
        multithreadingHelper.invokeAndWait(assignedObjRefs, item -> item.getKey().getEntities(item.getValue()), (resultOfFork, itemOfFork) -> {
            if (resultOfFork == null) {
                return;
            }
            for (int a = 0, size = resultOfFork.size(); a < size; a++) {
                ILoadContainer partItem = resultOfFork.get(a);
                result.add(partItem);
            }
            if (resultOfFork instanceof IDisposable) {
                ((IDisposable) resultOfFork).dispose();
            }
        });
        return result;
    }

    @Override
    public List<IObjRelationResult> getRelations(List<IObjRelation> objRelations) {
        ParamChecker.assertParamNotNull(objRelations, "objRelations");

        var assignedObjRelations = bucketSortObjRels(objRelations);

        var result = new ArrayList<IObjRelationResult>(objRelations.size());
        multithreadingHelper.invokeAndWait(assignedObjRelations, item -> {
            var retriever = item.getKey();
            return retriever.getRelations(item.getValue());
        }, (resultOfFork, itemOfFork) -> {
            for (int a = 0, size = resultOfFork.size(); a < size; a++) {
                var partItem = resultOfFork.get(a);
                result.add(partItem);
            }
            if (resultOfFork instanceof IDisposable) {
                ((IDisposable) resultOfFork).dispose();
            }
        });
        return result;
    }

    protected ICacheRetriever getRetrieverForType(Class<?> type) {
        if (type == null) {
            return null;
        }

        var cacheRetriever = typeToCacheRetrieverMap.getExtension(type);
        if (cacheRetriever == null) {
            if (defaultCacheRetriever != null && defaultCacheRetriever != this) {
                cacheRetriever = defaultCacheRetriever;
            } else {
                throw new IllegalStateException("No cache retriever found to handle entity type '" + type.getName() + "'");
            }
        }

        return cacheRetriever;
    }

    protected <E> E getPropertyRetrieverForType(ClassExtendableContainer<HashMap<String, E>> extendableContainer, Class<?> type, String propertyName) {
        if (type == null) {
            return null;
        }
        var map = extendableContainer.getExtension(type);
        if (map == null) {
            return null;
        }
        return map.get(propertyName);
    }

    protected ILinkedMap<ICacheRetriever, IList<IObjRef>> bucketSortObjRefs(List<? extends IObjRef> orisToLoad) {
        var serviceToAssignedObjRefsDict = new IdentityLinkedMap<ICacheRetriever, IList<IObjRef>>();

        for (int i = orisToLoad.size(); i-- > 0; ) {
            var objRef = orisToLoad.get(i);
            var metaData = entityMetaDataProvider.getMetaData(objRef.getRealType());

            var cacheRetriever = getRetrieverForType(metaData.getEntityType());
            var assignedObjRefs = serviceToAssignedObjRefsDict.get(cacheRetriever);
            if (assignedObjRefs == null) {
                assignedObjRefs = new ArrayList<>();
                serviceToAssignedObjRefsDict.put(cacheRetriever, assignedObjRefs);
            }
            assignedObjRefs.add(objRef);
        }
        return serviceToAssignedObjRefsDict;
    }

    protected ILinkedMap<IRelationRetriever, IList<IObjRelation>> bucketSortObjRels(List<? extends IObjRelation> orisToLoad) {
        var retrieverToAssignedObjRelsDict = new IdentityLinkedMap<IRelationRetriever, IList<IObjRelation>>();

        for (int i = orisToLoad.size(); i-- > 0; ) {
            var orelToLoad = orisToLoad.get(i);
            var metaData = entityMetaDataProvider.getMetaData(orelToLoad.getRealType());
            var relationMember = metaData.getMemberByName(orelToLoad.getMemberName());

            // look first for a specific retriever for the requested property of the owning entity type
            var relationRetriever = getPropertyRetrieverForType(typeToRelationRetrieverEC, metaData.getEntityType(), relationMember.getName());
            if (relationRetriever == null) {
                // fallback to retriever registered for the target entity type
                relationRetriever = getRetrieverForType(orelToLoad.getRealType());
            }
            var assignedObjRefs = retrieverToAssignedObjRelsDict.get(relationRetriever);
            if (assignedObjRefs == null) {
                assignedObjRefs = new ArrayList<>();
                retrieverToAssignedObjRelsDict.put(relationRetriever, assignedObjRefs);
            }
            assignedObjRefs.add(orelToLoad);
        }
        return retrieverToAssignedObjRelsDict;
    }

    protected ILinkedMap<IPrimitiveRetriever, PrimitiveRetrieverArguments> bucketSortObjRelsForFetchablePrimitives(List<ILoadContainer> loadContainers,
            ILinkedMap<IObjRelation, CheckedConsumer<Object>> objRelToDelegateMap) {
        var retrieverToAssignedObjRelsDict = new IdentityLinkedMap<IPrimitiveRetriever, PrimitiveRetrieverArguments>();

        for (int a = loadContainers.size(); a-- > 0; ) {
            var loadContainer = loadContainers.get(a);
            var objRef = loadContainer.getReference();
            var entityType = objRef.getRealType();
            var metaData = entityMetaDataProvider.getMetaData(entityType);
            var primitiveMembers = metaData.getPrimitiveMembers();
            var primitives = loadContainer.getPrimitives();
            for (int b = primitives.length; b-- > 0; ) {
                var primitive = primitives[b];
                if (primitive != null) {
                    continue;
                }
                var memberName = primitiveMembers[b].getName();
                var primitiveRetriever = getPropertyRetrieverForType(typeToPrimitiveRetrieverEC, entityType, memberName);

                if (primitiveRetriever == null) {
                    continue;
                }
                var primitiveRetrieverArg = retrieverToAssignedObjRelsDict.get(primitiveRetriever);
                if (primitiveRetrieverArg == null) {
                    primitiveRetrieverArg = new PrimitiveRetrieverArguments();
                    retrieverToAssignedObjRelsDict.put(primitiveRetriever, primitiveRetrieverArg);
                }
                var objRefs = objRefHelper.entityToAllObjRefs(loadContainer, metaData);
                var objRel = new ObjRelation(objRefs.toArray(IObjRef[]::new), memberName);
                objRel.setRealType(entityType);
                objRel.setVersion(objRef.getVersion());
                primitiveRetrieverArg.addArg(loadContainer, objRel);

                final int primitiveIndex = b;
                objRelToDelegateMap.put(objRel, fetchedPrimitive -> {
                    primitives[primitiveIndex] = fetchedPrimitive;
                });
            }
        }
        return retrieverToAssignedObjRelsDict;
    }

    public void handleEventSessionChanged(EventSessionChanged evnt) {
        var remoteSourceIdentifier = resolveRemoteSourceIdentifier(evnt.getEventService());
        if (remoteSourceIdentifier == null) {
            if (log.isInfoEnabled()) {
                log.info("Event Session changed for '" + evnt.getEventService() + "' but no remote source identifier resolved to evaluate a need for cache resynchronization");
            }
            return;
        }
        var entityTypesToUpdateSet = new HashSet<Class<?>>();
        for (var entry : typeToCacheRetrieverMap.getExtensions()) {
            var cacheRetriever = entry.getValue();
            var cacheRemoteSourceIdentifier = resolveRemoteSourceIdentifier(cacheRetriever);
            if (Objects.equals(remoteSourceIdentifier, cacheRemoteSourceIdentifier)) {
                entityTypesToUpdateSet.add(entry.getKey());
            }
        }
        for (var entry : typeToRelationRetrieverEC.getExtensions()) {
            for (var propertyEntry : entry.getValue()) {
                var cacheRetriever = propertyEntry.getValue();
                var cacheRemoteSourceIdentifier = resolveRemoteSourceIdentifier(cacheRetriever);
                if (Objects.equals(remoteSourceIdentifier, cacheRemoteSourceIdentifier)) {
                    entityTypesToUpdateSet.add(entry.getKey());
                }
            }
        }
        for (var entry : typeToPrimitiveRetrieverEC.getExtensions()) {
            for (var propertyEntry : entry.getValue()) {
                var cacheRetriever = propertyEntry.getValue();
                var cacheRemoteSourceIdentifier = resolveRemoteSourceIdentifier(cacheRetriever);
                if (Objects.equals(remoteSourceIdentifier, cacheRemoteSourceIdentifier)) {
                    entityTypesToUpdateSet.add(entry.getKey());
                }
            }
        }
        eventDispatcher.dispatchEvent(new RefreshEntitiesOfType(entityTypesToUpdateSet.toArray(Class.class)));
    }

    private Object resolveRemoteSourceIdentifier(Object service) {
        if (service instanceof IRemoteInterceptor) {
            return ((IRemoteInterceptor) service).getRemoteSourceIdentifier();
        }
        var callback = (ICascadedInterceptor) ((Factory) service).getCallback(0);

        while (callback != null) {
            if (callback instanceof IRemoteInterceptor) {
                return ((IRemoteInterceptor) callback).getRemoteSourceIdentifier();
            }
        }
        return null;
    }

    public static class PrimitiveRetrieverArguments {
        public final ArrayList<IObjRelation> objRelations = new ArrayList<>();

        public final ArrayList<ILoadContainer> loadContainers = new ArrayList<>();

        public void addArg(ILoadContainer loadContainer, IObjRelation objRel) {
            objRelations.add(objRel);
            loadContainers.add(loadContainer);
        }
    }
}
