package com.koch.ambeth.merge;

/*-
 * #%L
 * jambeth-merge
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

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.cache.AbstractCacheValue;
import com.koch.ambeth.merge.compositeid.ICompositeIdFactory;
import com.koch.ambeth.merge.metadata.IObjRefFactory;
import com.koch.ambeth.merge.proxy.IEntityMetaDataHolder;
import com.koch.ambeth.merge.transfer.DirectObjRef;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.service.cache.model.ILoadContainer;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.ArrayList;

import java.util.List;
import java.util.Optional;

public class ObjRefHelper implements IObjRefHelper {
    @Autowired
    protected ICompositeIdFactory compositeIdFactory;

    @Autowired
    protected IEntityMetaDataProvider entityMetaDataProvider;

    @Autowired
    protected IObjRefFactory objRefFactory;

    @Override
    public List<IObjRef> extractObjRefList(Object objValue, MergeHandle mergeHandle) {
        return extractObjRefList(objValue, mergeHandle, null, null);
    }

    @Override
    public List<IObjRef> extractObjRefList(Object objValue, MergeHandle mergeHandle, List<IObjRef> targetObjRefList) {
        return extractObjRefList(objValue, mergeHandle, targetObjRefList, null);
    }

    @Override
    public List<IObjRef> extractObjRefList(Object objValue, MergeHandle mergeHandle, List<IObjRef> targetObjRefList, EntityCallback entityCallback) {
        if (objValue instanceof Optional opt) {
            if (opt.isPresent()) {
                objValue = opt.get();
            } else {
                objValue = null;
            }
        }
        if (objValue == null) {
            if (targetObjRefList == null) {
                return List.of();
            }
            return targetObjRefList;
        }
        if (objValue.getClass().isArray()) {
            var array = (Object[]) objValue;
            if (targetObjRefList == null) {
                targetObjRefList = new ArrayList<>(array.length);
            }
            for (int a = 0, size = array.length; a < size; a++) {
                extractObjRefList(array[a], mergeHandle, targetObjRefList, entityCallback);
            }
        } else if (objValue instanceof List<?>) {
            var list = (List<?>) objValue;
            if (targetObjRefList == null) {
                targetObjRefList = new ArrayList<>(list.size());
            }
            for (int a = 0, size = list.size(); a < size; a++) {
                extractObjRefList(list.get(a), mergeHandle, targetObjRefList, entityCallback);
            }
        } else if (objValue instanceof Iterable<?>) {
            var objEnumerator = ((Iterable<?>) objValue).iterator();
            if (targetObjRefList == null) {
                targetObjRefList = new ArrayList<>();
            }
            while (objEnumerator.hasNext()) {
                extractObjRefList(objEnumerator.next(), mergeHandle, targetObjRefList, entityCallback);
            }
        } else {
            if (targetObjRefList == null) {
                targetObjRefList = new ArrayList<>(1);
            }
            getCreateORIs(objValue, mergeHandle, targetObjRefList, entityCallback);
        }
        return targetObjRefList;
    }

    @Override
    public List<IObjRef> extractObjRefList(Object objValue, IObjRefProvider oriProvider, List<IObjRef> targetObjRefList, EntityCallback entityCallback) {
        if (objValue == null) {
            if (targetObjRefList == null) {
                return List.of();
            }
            return targetObjRefList;
        }
        if (targetObjRefList == null) {
            targetObjRefList = new ArrayList<>();
        }
        if (objValue.getClass().isArray()) {
            var array = (Object[]) objValue;
            for (int a = 0, size = array.length; a < size; a++) {
                var objItem = array[a];
                targetObjRefList.add(getCreateObjRef(objItem, oriProvider));
                if (entityCallback != null) {
                    entityCallback.callback(objItem);
                }
            }
        } else if (objValue instanceof List<?> list) {
            for (int a = 0, size = list.size(); a < size; a++) {
                var objItem = list.get(a);
                targetObjRefList.add(getCreateObjRef(objItem, oriProvider));
                if (entityCallback != null) {
                    entityCallback.callback(objItem);
                }
            }
        } else if (objValue instanceof Iterable<?> iterable) {
            var objEnumerator = iterable.iterator();
            while (objEnumerator.hasNext()) {
                var objItem = objEnumerator.next();
                targetObjRefList.add(getCreateObjRef(objItem, oriProvider));
                if (entityCallback != null) {
                    entityCallback.callback(objItem);
                }
            }
        } else {
            targetObjRefList.add(getCreateObjRef(objValue, oriProvider));
            if (entityCallback != null) {
                entityCallback.callback(objValue);
            }
        }
        return targetObjRefList;
    }

    protected void getCreateORIs(Object obj, IObjRefProvider oriProvider, List<IObjRef> targetList) {
        var ori = getCreateObjRef(obj, oriProvider);
        targetList.add(ori);
    }

    @Override
    public IObjRef getCreateObjRef(Object obj, IObjRefProvider oriProvider) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof IObjRef) {
            return (IObjRef) obj;
        }
        var metaData = ((IEntityMetaDataHolder) obj).get__EntityMetaData();
        return oriProvider.getObjRef(obj, metaData);
    }

    @Override
    public IObjRef getCreateObjRef(Object obj, IObjRefHelperState state) {
        if (obj == null) {
            return null;
        } else if (obj instanceof Optional opt) {
            if (!opt.isPresent()) {
                return null;
            }
            obj = opt.get();
        }
        IObjRef objRef = null;
        var objToObjRefDict = state != null ? state.getObjToObjRefMap() : null;
        if (objToObjRefDict != null) {
            objRef = objToObjRefDict.get(obj);
        }
        if (objRef != null) {
            return objRef;
        }
        if (obj instanceof IObjRef) {
            return (IObjRef) obj;
        }
        if (!(obj instanceof IEntityMetaDataHolder)) {
            return null;
        }
        var metaData = ((IEntityMetaDataHolder) obj).get__EntityMetaData();

        Object keyValue;
        if (obj instanceof AbstractCacheValue) {
            keyValue = ((AbstractCacheValue) obj).getId();
        } else {
            keyValue = metaData.getIdMember().getValue(obj, false);
        }
        if (keyValue == null || state != null && state.isHandleExistingIdAsNewId()) {
            var dirOri = new DirectObjRef(metaData.getEntityType(), obj);
            if (keyValue != null) {
                dirOri.setId(keyValue);
            }
            objRef = dirOri;
        } else {
            Object version;
            if (obj instanceof AbstractCacheValue) {
                version = ((AbstractCacheValue) obj).getVersion();
            } else {
                var versionMember = metaData.getVersionMember();
                version = versionMember != null ? versionMember.getValue(obj, true) : null;
            }
            objRef = objRefFactory.createObjRef(metaData.getEntityType(), ObjRef.PRIMARY_KEY_INDEX, keyValue, version);
        }
        if (objToObjRefDict != null) {
            objToObjRefDict.put(obj, objRef);

            var objRefToObjDict = state != null ? state.getObjRefToObjMap() : null;
            if (objRefToObjDict != null) {
                objRefToObjDict.putIfAbsent(objRef, obj);
            }
        }
        return objRef;
    }

    protected void getCreateORIs(Object obj, IObjRefHelperState state, List<IObjRef> targetList, EntityCallback entityCallback) {
        var ori = getCreateObjRef(obj, state);
        targetList.add(ori);
        if (entityCallback != null) {
            entityCallback.callback(obj);
        }

    }

    @Override
    public IObjRef entityToObjRef(Object entity) {
        return entityToObjRef(entity, ObjRef.PRIMARY_KEY_INDEX, ((IEntityMetaDataHolder) entity).get__EntityMetaData());
    }

    @Override
    public IObjRef entityToObjRef(Object entity, boolean forceOri) {
        return entityToObjRef(entity, ObjRef.PRIMARY_KEY_INDEX, ((IEntityMetaDataHolder) entity).get__EntityMetaData(), forceOri);
    }

    @Override
    public IObjRef entityToObjRef(Object entity, int idIndex) {
        return entityToObjRef(entity, idIndex, ((IEntityMetaDataHolder) entity).get__EntityMetaData());
    }

    @Override
    public IObjRef entityToObjRef(Object entity, IEntityMetaData metaData) {
        return entityToObjRef(entity, ObjRef.PRIMARY_KEY_INDEX, metaData);
    }

    @Override
    public IObjRef entityToObjRef(Object entity, int idIndex, IEntityMetaData metaData) {
        return entityToObjRef(entity, idIndex, metaData, false);
    }

    @Override
    public IObjRef entityToObjRef(Object entity, int idIndex, IEntityMetaData metaData, boolean forceOri) {
        Object id;
        Object version;
        var versionMember = metaData.getVersionMember();
        if (entity instanceof AbstractCacheValue) {
            var cacheValue = (AbstractCacheValue) entity;
            if (idIndex == ObjRef.PRIMARY_KEY_INDEX) {
                id = cacheValue.getId();
            } else {
                id = compositeIdFactory.createIdFromPrimitives(metaData, idIndex, cacheValue);
            }
            version = cacheValue.getVersion();
        } else if (entity instanceof ILoadContainer) {
            var lc = (ILoadContainer) entity;
            if (idIndex == ObjRef.PRIMARY_KEY_INDEX) {
                id = lc.getReference().getId();
            } else {
                id = compositeIdFactory.createIdFromPrimitives(metaData, idIndex, lc.getPrimitives());
            }
            version = lc.getReference().getVersion();
        } else {
            id = metaData.getIdMemberByIdIndex(idIndex).getValue(entity, false);
            version = versionMember != null ? versionMember.getValue(entity, false) : null;
        }
        IObjRef objRef;

        if (id != null || forceOri) {
            objRef = objRefFactory.createObjRef(metaData.getEntityType(), idIndex, id, version);
        } else {
            objRef = new DirectObjRef(metaData.getEntityType(), entity);
        }

        return objRef;
    }

    @Override
    public List<IObjRef> entityToAllObjRefs(Object id, Object version, Object[] primitives, IEntityMetaData metaData) {
        var alternateIdCount = metaData.getAlternateIdCount();

        var allOris = new ArrayList<IObjRef>();

        var entityType = metaData.getEntityType();
        // Convert id and version to the correct metadata type
        if (id != null) {
            allOris.add(objRefFactory.createObjRef(entityType, ObjRef.PRIMARY_KEY_INDEX, id, version));
        }
        if (alternateIdCount > 0) {
            var alternateIdMembers = metaData.getAlternateIdMembers();

            var primitiveMembers = metaData.getPrimitiveMembers();
            for (int a = primitiveMembers.length; a-- > 0; ) {
                var primitiveMember = primitiveMembers[a];
                for (int b = alternateIdMembers.length; b-- > 0; ) {
                    var alternateIdMember = alternateIdMembers[b];
                    if (alternateIdMember == primitiveMember) {
                        var alternateId = primitives[a];
                        if (alternateId == null) {
                            // The current member is an alternate id. But alternate ids are not mandatorily
                            // not-null
                            // If they are not specified, they are simply ignored
                            continue;
                        }
                        allOris.add(objRefFactory.createObjRef(entityType, b, alternateId, version));
                        break;
                    }
                }
            }
        }
        return allOris;
    }

    @Override
    public List<IObjRef> entityToAllObjRefs(Object entity) {
        if (entity instanceof IEntityMetaDataHolder) {
            return entityToAllObjRefs(entity, ((IEntityMetaDataHolder) entity).get__EntityMetaData());
        }
        var lc = (ILoadContainer) entity;
        return entityToAllObjRefs(entity, entityMetaDataProvider.getMetaData(lc.getReference().getRealType()));
    }

    @Override
    public List<IObjRef> entityToAllObjRefs(Object entity, IEntityMetaData metaData) {
        var alternateIdCount = metaData.getAlternateIdCount();
        var allOris = new ArrayList<IObjRef>();

        var ref = entityToObjRef(entity, ObjRef.PRIMARY_KEY_INDEX, metaData);
        if (ref.getId() != null) {
            allOris.add(ref);
        }
        // Sorted list may later reduce search cost
        for (int i = 0; i < alternateIdCount; i++) {
            ref = entityToObjRef(entity, (byte) i, metaData);
            if (ref.getId() != null) {
                allOris.add(ref);
            }
        }

        return allOris;
    }
}
