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
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.extendable.ExtendableContainer;
import com.koch.ambeth.ioc.garbageproxy.IGCProxy;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.CacheFactoryDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.cache.ICacheFactory;
import com.koch.ambeth.merge.cache.ICacheModification;
import com.koch.ambeth.merge.cache.ICacheProvider;
import com.koch.ambeth.merge.cache.IDisposableCache;
import com.koch.ambeth.merge.cache.IWritableCache;
import com.koch.ambeth.merge.cache.ValueHolderState;
import com.koch.ambeth.merge.compositeid.CompositeIdMember;
import com.koch.ambeth.merge.config.MergeConfigurationConstants;
import com.koch.ambeth.merge.metadata.IObjRefFactory;
import com.koch.ambeth.merge.model.ICUDResult;
import com.koch.ambeth.merge.model.IOriCollection;
import com.koch.ambeth.merge.model.IRelationUpdateItem;
import com.koch.ambeth.merge.model.IUpdateItem;
import com.koch.ambeth.merge.model.RelationUpdateItemBuild;
import com.koch.ambeth.merge.proxy.IEntityMetaDataHolder;
import com.koch.ambeth.merge.proxy.IObjRefContainer;
import com.koch.ambeth.merge.security.ISecurityActivation;
import com.koch.ambeth.merge.transfer.CreateContainer;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.merge.transfer.PrimitiveUpdateItem;
import com.koch.ambeth.merge.transfer.RelationUpdateItem;
import com.koch.ambeth.merge.transfer.UpdateContainer;
import com.koch.ambeth.merge.util.DirectValueHolderRef;
import com.koch.ambeth.merge.util.IPrefetchHelper;
import com.koch.ambeth.merge.util.OptimisticLockUtil;
import com.koch.ambeth.merge.util.ValueHolderRef;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.util.Arrays;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptySet;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.collections.IdentityHashMap;
import com.koch.ambeth.util.collections.IdentityHashSet;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.model.IDataObject;
import com.koch.ambeth.util.threading.IGuiThreadHelper;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class MergeController implements IMergeController, IMergeExtendable {
    protected static final Set<CacheDirective> failEarlyAndReturnMissesSet = EnumSet.of(CacheDirective.FailEarly, CacheDirective.ReturnMisses);
    protected static final Object NO_PARENT = new Object();
    protected final ExtendableContainer<IMergeExtension> mergeExtensions = new ExtendableContainer<>(IMergeExtension.class, "mergeExtension");
    @Autowired
    protected ICacheFactory cacheFactory;
    @Autowired
    protected ICacheModification cacheModification;
    @Autowired
    protected ICacheProvider cacheProvider;
    @Autowired
    protected IConversionHelper conversionHelper;
    @Autowired
    protected IDeepScanRecursion deepScanRecursion;
    @Autowired
    protected IEntityMetaDataProvider entityMetaDataProvider;
    @Autowired
    protected IGuiThreadHelper guiThreadHelper;
    @Autowired
    protected IPrefetchHelper prefetchHelper;
    @Autowired
    protected ICUDResultHelper cudResultHelper;
    @Autowired
    protected IObjRefFactory objRefFactory;
    @Autowired
    protected IObjRefHelper objRefHelper;
    @Autowired
    protected IRevertChangesHelper revertChangesHelper;
    @Autowired
    protected ISecurityActivation securityActivation;
    @Property(name = MergeConfigurationConstants.ExactVersionForOptimisticLockingRequired, defaultValue = "false")
    protected boolean exactVersionForOptimisticLockingRequired;
    @Property(name = MergeConfigurationConstants.AlwaysUpdateVersionInChangedEntities, defaultValue = "false")
    protected boolean alwaysUpdateVersionInChangedEntities;

    protected List<IUpdateItem> addModification(Object obj, MergeHandle handle) {
        var modItemList = handle.objToModDict.get(obj);
        if (modItemList == null) {
            modItemList = new ArrayList<>();
            handle.objToModDict.put(obj, modItemList);
        }
        return modItemList;
    }

    protected void addModification(Object obj, String memberName, Class<?> targetValueType, Object value, Object cloneValue, MergeHandle handle) {
        if (value != null && Collection.class.isAssignableFrom(value.getClass()) && ((Collection<?>) value).isEmpty()) {
            return;
        }
        if (value instanceof Optional) {
            var optValue = (Optional<?>) value;
            if (optValue.isPresent()) {
                value = optValue.get();
            } else {
                value = null;
            }
        }
        for (var mergeExtension : mergeExtensions.getExtensionsShared()) {
            if (mergeExtension.handlesType(targetValueType)) {
                value = mergeExtension.extractPrimitiveValueToMerge(value);
            }
        }
        var primModItem = new PrimitiveUpdateItem();
        primModItem.setMemberName(memberName);
        primModItem.setNewValue(value);

        var modItemList = addModification(obj, handle);
        modItemList.add(primModItem);
    }

    @SuppressWarnings("rawtypes")
    protected void addOriModification(Object obj, String memberName, Object value, Object cloneValue, MergeHandle handle) {
        if (value instanceof List) {
            var list = (List) value;
            for (int a = 0, size = list.size(); a < size; a++) {
                var objItem = list.get(a);
                mergeOrPersist(objItem, handle);
            }
        } else if (value instanceof Collection) {
            var iter = ((Collection<?>) value).iterator();
            while (iter.hasNext()) {
                var objItem = iter.next();
                mergeOrPersist(objItem, handle);
            }
        } else {
            mergeOrPersist(value, handle);
        }
        try {
            var oldOriList = objRefHelper.extractObjRefList(cloneValue, handle, handle.getOldOrList());
            var newOriList = objRefHelper.extractObjRefList(value, handle, handle.getNewOrList());

            var oriModItem = createRUI(memberName, oldOriList, newOriList);
            if (oriModItem == null) {
                return;
            }

            var modItemList = addModification(obj, handle);

            modItemList.add(oriModItem);
        } finally {
            handle.getOldOrList().clear();
            handle.getNewOrList().clear();
        }
    }

    @Override
    public void applyChangesToOriginals(ICUDResult cudResult, IOriCollection oriCollection, Function<Object, ICache> entityToAssociatedCaches) {
        if (guiThreadHelper.isInGuiThread()) {
            applyChangesToOriginalsIntern(cudResult, oriCollection, entityToAssociatedCaches);
            return;
        }
        guiThreadHelper.invokeInGuiAndWait(() -> applyChangesToOriginalsIntern(cudResult, oriCollection, entityToAssociatedCaches));
    }

    protected void applyChangesToOriginalsIntern(ICUDResult cudResult, IOriCollection oriCollection, Function<Object, ICache> entityToAssociatedCaches) {
        var conversionHelper = this.conversionHelper;
        var entityMetaDataProvider = this.entityMetaDataProvider;
        var allChanges = cudResult.getAllChanges();
        var originalRefs = cudResult.getOriginalRefs();
        var allChangeORIs = oriCollection.getAllChangeORIs();
        var allChangedBy = oriCollection.getAllChangedBy();
        var allChangedOn = oriCollection.getAllChangedOn();
        var singleChangedBy = oriCollection.getChangedBy();
        var singleChangedOn = oriCollection.getChangedOn();

        var validObjects = new ArrayList<>(originalRefs.size());
        var rollback = cacheModification.pushActive();
        try {
            for (int a = originalRefs.size(); a-- > 0; ) {
                var originalRef = originalRefs.get(a);
                var ori = allChangeORIs.get(a);

                if (originalRef == null) {
                    // Object has been deleted by cascade delete constraints on server merge or simply a "not specified" original ref
                    continue;
                }
                if (originalRef instanceof IObjRef) {
                    continue;
                }
                var changedOn = allChangedOn != null ? allChangedOn[a] : singleChangedOn;
                var changedBy = allChangedBy != null ? allChangedBy[a] : singleChangedBy;
                var metaData = entityMetaDataProvider.getMetaData(originalRef.getClass());

                var keyMember = metaData.getIdMember();
                var versionMember = metaData.getVersionMember();

                Member onMember, byMember;
                if (keyMember.getValue(originalRef, false) == null) {
                    onMember = metaData.getCreatedOnMember();
                    byMember = metaData.getCreatedByMember();
                } else {
                    onMember = metaData.getUpdatedOnMember();
                    byMember = metaData.getUpdatedByMember();
                }
                if (onMember != null && changedOn != null) {
                    var createdOn = conversionHelper.convertValueToType(onMember.getElementType(), changedOn);
                    onMember.setValue(originalRef, createdOn);
                }
                if (byMember != null && changedBy != null) {
                    var createdBy = conversionHelper.convertValueToType(byMember.getElementType(), changedBy);
                    byMember.setValue(originalRef, createdBy);
                }
                if (ori == null) {
                    keyMember.setValue(originalRef, null);
                    if (versionMember != null) {
                        versionMember.setValue(originalRef, null);
                    }
                    if (originalRef instanceof IDataObject) {
                        ((IDataObject) originalRef).setToBeUpdated(false);
                        ((IDataObject) originalRef).setToBeDeleted(false);
                    }
                    continue; // Object has been deleted directly
                }
                keyMember.setValue(originalRef, conversionHelper.convertValueToType(keyMember.getRealType(), ori.getId()));
                if (versionMember != null) {
                    if (alwaysUpdateVersionInChangedEntities) {
                        versionMember.setValue(originalRef, conversionHelper.convertValueToType(versionMember.getRealType(), ori.getVersion()));
                    } else {
                        // We INTENTIONALLY do NOT set the version and let it on its old value, to force the following DCE to refresh the cached object with 'real' data
                        // If we set the version here to the ori.getVersion(), the DCE will 'see' an already valid object - but it is NOT valid
                        // because it may not contain bi-directional information which can only be  resolved by reloading the object from persistence layer
                    }
                }
                if (originalRef instanceof IDataObject) {
                    ((IDataObject) originalRef).setToBeUpdated(false);
                    ((IDataObject) originalRef).setToBeDeleted(false);
                }
                var changeContainer = allChanges.get(a);
                if (changeContainer instanceof CreateContainer || changeContainer instanceof UpdateContainer) {
                    validObjects.add(originalRef);
                }
            }
            putInstancesToCurrentCache(validObjects, entityToAssociatedCaches);
        } finally {
            rollback.rollback();
        }
    }

    protected boolean arePrimitivesEqual(IEntityMetaData metaData, Member primitiveMember, Object objValue, Object cloneValue, MergeHandle handle) {
        if (objValue == cloneValue) {
            // object identity is the simple case. of course this branch here may happen only for
            // immutable types
            return true;
        }
        if (objValue != null && cloneValue != null) {
            if (objValue.getClass().isArray() && cloneValue.getClass().isArray()) {
                var objLength = Array.getLength(objValue);
                var cloneLength = Array.getLength(cloneValue);
                if (objLength != cloneLength) {
                    return false;
                }
                var preparedArrayGetObj = Arrays.prepareGet(objValue);
                var preparedArrayGetClone = Arrays.prepareGet(cloneValue);
                for (int b = objLength; b-- > 0; ) {
                    var objItem = preparedArrayGetObj.get(b);
                    var cloneItem = preparedArrayGetClone.get(b);
                    if (!equalsObjects(objItem, cloneItem)) {
                        return false;
                    }
                }
                return true;
            } else if (objValue instanceof Optional && cloneValue instanceof Optional) {
                var objOpt = (Optional<?>) objValue;
                var cloneOpt = (Optional<?>) cloneValue;
                if (!objOpt.isPresent() && !cloneOpt.isPresent()) {
                    return true; // both have nothing
                }
                if (objOpt.isPresent() && cloneOpt.isPresent()) {
                    return arePrimitivesEqual(metaData, primitiveMember, objOpt.get(), cloneOpt.get(), handle);
                }
                return false;
            } else if (objValue instanceof List && cloneValue instanceof List) {
                var objList = (List<?>) objValue;
                var cloneList = (List<?>) cloneValue;
                if (objList.size() != cloneList.size()) {
                    return false;
                }
                for (int b = objList.size(); b-- > 0; ) {
                    Object objItem = objList.get(b);
                    Object cloneItem = cloneList.get(b);
                    if (!equalsObjects(objItem, cloneItem)) {
                        return false;
                    }
                }
                return true;
            } else if (objValue instanceof Set && cloneValue instanceof Set) {
                var objColl = (Set<?>) objValue;
                var cloneColl = (Set<?>) cloneValue;
                if (objColl.size() != cloneColl.size()) {
                    return false;
                }
                return cloneColl.containsAll(objColl);
            } else if (objValue instanceof Iterable && cloneValue instanceof Iterable) {
                var objIter = ((Iterable<?>) objValue).iterator();
                var cloneIter = ((Iterable<?>) cloneValue).iterator();
                while (objIter.hasNext()) {
                    if (!cloneIter.hasNext()) {
                        return false;
                    }
                    var objItem = objIter.next();
                    var cloneItem = cloneIter.next();
                    if (!equalsObjects(objItem, cloneItem)) {
                        return false;
                    }
                }
                if (cloneIter.hasNext()) {
                    return false;
                }
                return true;
            }
        }
        return equalsObjects(objValue, cloneValue);
    }

    protected List<Object> batchLoadOriginalState(MergeHandle handle, boolean privilegedMode, ArrayList<IObjRef> objRefs, ArrayList<ValueHolderRef> valueHolderKeys) {
        ICache cache;
        if (privilegedMode) {
            cache = handle.getPrivilegedCache();
            if (cache == null) {
                cache = cacheFactory.createPrivileged(CacheFactoryDirective.NoDCE, false, Boolean.FALSE, "MergeController.ORIGINAL.PRIVILEGED");
                handle.setPrivilegedCache(cache);
                handle.setPrivilegedCacheToDispose(true);
            }
        } else {
            cache = handle.getCache();
            if (cache == null) {
                cache = cacheFactory.create(CacheFactoryDirective.NoDCE, false, Boolean.FALSE, "MergeController.ORIGINAL");
                handle.setCache(cache);
                handle.setCacheToDispose(true);
            }
        }
        var eagerlyLoadedOriginals = cache.getObjects(objRefs, CacheDirective.returnMisses());
        for (int a = eagerlyLoadedOriginals.size(); a-- > 0; ) {
            IObjRef existingOri = objRefs.get(a);
            if (eagerlyLoadedOriginals.get(a) == null && existingOri != null && existingOri.getId() != null) {
                // Cache miss for an entity we want to merge. This is an OptimisticLock-State
                throw OptimisticLockUtil.throwDeleted(existingOri);
            }
        }
        var objRefsOfVhks = new ArrayList<IObjRef>(valueHolderKeys.size());
        for (int a = 0, size = valueHolderKeys.size(); a < size; a++) {
            objRefsOfVhks.add(valueHolderKeys.get(a).getObjRef());
        }
        var objectsOfVhks = cache.getObjects(objRefsOfVhks, failEarlyAndReturnMissesSet);
        for (int a = valueHolderKeys.size(); a-- > 0; ) {
            var objectOfVhk = (IObjRefContainer) objectsOfVhks.get(a);
            if (objectOfVhk == null) {
                continue;
            }
            var valueHolderRef = valueHolderKeys.get(a);
            if (ValueHolderState.INIT == objectOfVhk.get__State(valueHolderRef.getRelationIndex())) {
                continue;
            }
            var vhcKey = new DirectValueHolderRef(objectOfVhk, valueHolderRef.getMember());
            handle.getPendingValueHolders().add(vhcKey);
        }
        return eagerlyLoadedOriginals;
    }

    @Override
    public IRelationUpdateItem createRUI(String memberName, List<IObjRef> oldOriList, List<IObjRef> newOriList) {
        if (oldOriList.isEmpty() && newOriList.isEmpty()) {
            return null;
        }
        ISet<IObjRef> oldSet = !oldOriList.isEmpty() ? new HashSet<>(oldOriList) : EmptySet.<IObjRef>emptySet();
        ISet<IObjRef> newSet = !newOriList.isEmpty() ? new HashSet<>(newOriList) : EmptySet.<IObjRef>emptySet();

        ISet<IObjRef> smallerSet = oldSet.size() > newSet.size() ? newSet : oldSet;
        ISet<IObjRef> greaterSet = oldSet.size() > newSet.size() ? oldSet : newSet;

        // Check unchanged ORIs
        Iterator<IObjRef> smallerIter = smallerSet.iterator();
        while (smallerIter.hasNext()) {
            // Old ORIs, which exist as new ORIs, too, are unchanged
            IObjRef objRef = smallerIter.next();
            if (greaterSet.remove(objRef)) {
                smallerIter.remove();
            }
        }
        if (oldSet.isEmpty() && newSet.isEmpty()) {
            return null;
        }
        // Old ORIs are now handled as REMOVE, New ORIs as ADD
        RelationUpdateItem rui = new RelationUpdateItem();
        rui.setMemberName(memberName);
        if (!oldSet.isEmpty()) {
            rui.setRemovedORIs(oldSet.toArray(IObjRef[]::new));
        }
        if (!newSet.isEmpty()) {
            rui.setAddedORIs(newSet.toArray(IObjRef[]::new));
        }
        return rui;
    }

    @Override
    public RelationUpdateItemBuild createRUIBuild(String memberName, List<IObjRef> oldOriList, List<IObjRef> newOriList) {
        if (oldOriList.isEmpty() && newOriList.isEmpty()) {
            return null;
        }
        ISet<IObjRef> oldSet = !oldOriList.isEmpty() ? new HashSet<>(oldOriList) : EmptySet.<IObjRef>emptySet();
        ISet<IObjRef> newSet = !newOriList.isEmpty() ? new HashSet<>(newOriList) : EmptySet.<IObjRef>emptySet();

        ISet<IObjRef> smallerSet = oldSet.size() > newSet.size() ? newSet : oldSet;
        ISet<IObjRef> greaterSet = oldSet.size() > newSet.size() ? oldSet : newSet;

        // Check unchanged ORIs
        Iterator<IObjRef> smallerIter = smallerSet.iterator();
        while (smallerIter.hasNext()) {
            // Old ORIs, which exist as new ORIs, too, are unchanged
            IObjRef objRef = smallerIter.next();
            if (greaterSet.remove(objRef)) {
                smallerIter.remove();
            }
        }
        if (oldSet.isEmpty() && newSet.isEmpty()) {
            return null;
        }
        // Old ORIs are now handled as REMOVE, New ORIs as ADD
        RelationUpdateItemBuild rui = new RelationUpdateItemBuild(memberName);
        if (!oldSet.isEmpty()) {
            rui.removeObjRefs(oldSet);
        }
        if (!newSet.isEmpty()) {
            rui.addObjRefs(newSet);
        }
        return rui;
    }

    protected boolean equalsObjects(Object left, Object right) {
        if (left == null) {
            return right == null;
        }
        if (right == null) {
            return false;
        }
        if (left.equals(right)) {
            return true;
        }
        for (IMergeExtension mergeExtension : mergeExtensions.getExtensionsShared()) {
            if (mergeExtension.handlesType(left.getClass())) {
                return mergeExtension.equalsObjects(left, right);
            }
        }
        return false;
    }

    protected boolean equalsReferenceOrId(Object original, Object clone, MergeHandle handle, IEntityMetaData metaData) {
        if (original instanceof Optional opt) {
            original = opt.isPresent() ? opt.get() : null;
        }
        if (clone instanceof Optional opt) {
            clone = opt.isPresent() ? opt.get() : null;
        }
        if (original == null) {
            return clone == null;
        }
        if (clone == null) {
            return false;
        }
        var keyMember = metaData.getIdMember();
        if (keyMember instanceof CompositeIdMember compositeIdMember) {
            for (int compositeIdIndex = compositeIdMember.getMembersCount(); compositeIdIndex-- > 0; ) {
                var cloneValue = compositeIdMember.getDecompositedValue(clone, compositeIdIndex);
                var originalValue = compositeIdMember.getDecompositedValue(original, compositeIdIndex);
                if (!Objects.equals(cloneValue, originalValue)) {
                    return false;
                }
            }
            return true;
        }
        return Objects.equals(keyMember.getValue(clone, false), keyMember.getValue(original, false));
    }

    @SuppressWarnings("rawtypes")
    protected boolean isMemberModified(Object objValue, Object cloneValue, MergeHandle handle, IEntityMetaData metaData) {
        if (objValue instanceof Optional opt) {
            objValue = opt.isPresent() ? opt.get() : null;
        }
        if (cloneValue instanceof Optional opt) {
            cloneValue = opt.isPresent() ? opt.get() : null;
        }
        if (objValue == null) {
            return cloneValue != null;
        }
        if (cloneValue == null) {
            mergeDeepIntern(objValue, handle);
            return true;
        }
        if (objValue instanceof List) {
            var objList = (List) objValue;
            var cloneList = (List) cloneValue;

            var memberModified = false;

            if (objList.size() != cloneList.size()) {
                memberModified = true;
            }
            for (int a = 0, size = objList.size(); a < size; a++) {
                var objItem = objList.get(a);

                if (cloneList.size() > a) {
                    var cloneItem = cloneList.get(a);
                    if (!equalsReferenceOrId(objItem, cloneItem, handle, metaData)) {
                        memberModified = true;
                    }
                }
                mergeOrPersist(objItem, handle);
            }
            return memberModified;
        }
        if (objValue instanceof Iterable) {
            var objEnumerator = ((Iterable<?>) objValue).iterator();
            var cloneEnumerator = ((Iterable<?>) cloneValue).iterator();

            var memberModified = false;
            while (objEnumerator.hasNext()) {
                var objItem = objEnumerator.next();
                if (!cloneEnumerator.hasNext()) {
                    memberModified = true;
                } else {
                    var cloneItem = cloneEnumerator.next();
                    if (!equalsReferenceOrId(objItem, cloneItem, handle, metaData)) {
                        memberModified = true;
                    }
                }
                mergeOrPersist(objItem, handle);
            }
            if (cloneEnumerator.hasNext()) {
                memberModified = true;
            }
            return memberModified;
        }
        if (handle.isDeepMerge()) {
            mergeOrPersist(objValue, handle);
        }
        return !equalsReferenceOrId(objValue, cloneValue, handle, metaData);
    }

    protected void merge(Object obj, Object clone, MergeHandle handle) {
        var entityMetaDataProvider = this.entityMetaDataProvider;
        var metaData = entityMetaDataProvider.getMetaData(obj.getClass());

        var fieldBasedMergeActive = handle.isFieldBasedMergeActive();
        var oneChangeOccured = false;
        var relationMembers = metaData.getRelationMembers();
        if (relationMembers.length > 0) {
            var vhc = (IObjRefContainer) obj;

            for (int relationIndex = relationMembers.length; relationIndex-- > 0; ) {
                var relationMember = relationMembers[relationIndex];
                if (!metaData.isMergeRelevant(relationMember)) {
                    continue;
                }
                if (!vhc.is__Initialized(relationIndex)) {
                    // v2 valueholder is not initialized. so a change is impossible
                    continue;
                }
                var objMember = relationMember.getValue(obj, false);
                var cloneMember = relationMember.getValue(clone, false);
                if (objMember instanceof IDataObject && !((IDataObject) objMember).hasPendingChanges()) {
                    var relationMetaData = entityMetaDataProvider.getMetaData(relationMember.getRealType());
                    if (equalsReferenceOrId(objMember, cloneMember, handle, relationMetaData)) {
                        continue;
                    }
                }

                var childMetaData = entityMetaDataProvider.getMetaData(relationMember.getElementType());

                if (isMemberModified(objMember, cloneMember, handle, childMetaData)) {
                    oneChangeOccured = true;
                    addOriModification(obj, relationMember.getName(), objMember, cloneMember, handle);
                }
            }
        }
        if (fieldBasedMergeActive) {
            mergePrimitivesFieldBased(metaData, obj, clone, handle);
            if (oneChangeOccured) {
                checkOptimisticLock(obj, clone, metaData);
            }
            return;
        }
        boolean additionalRound;
        do {
            additionalRound = !oneChangeOccured;
            for (var primitiveMember : metaData.getPrimitiveMembers()) {
                if (!metaData.isMergeRelevant(primitiveMember)) {
                    continue;
                }
                var objValue = primitiveMember.getValue(obj, true);
                if (oneChangeOccured) {
                    addModification(obj, primitiveMember.getName(), primitiveMember.getElementType(), objValue, null, handle);
                    continue;
                }
                var cloneValue = primitiveMember.getValue(clone, true);
                if (!arePrimitivesEqual(metaData, primitiveMember, objValue, cloneValue, handle)) {
                    oneChangeOccured = true;
                    break;
                }
            }
        } while (additionalRound && oneChangeOccured);

        if (oneChangeOccured) {
            checkOptimisticLock(obj, clone, metaData);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void checkOptimisticLock(Object obj, Object clone, IEntityMetaData metaData) {
        var versionMember = metaData.getVersionMember();
        if (versionMember == null) {
            return;
        }
        // Check for early optimistic locking (Another, later level is directly on
        // persistence layer)
        var versionToMerge = versionMember.getValue(obj, true);
        var currentVersion = versionMember.getValue(clone, true);

        var compareResult = ((Comparable) versionToMerge).compareTo(currentVersion);
        if (exactVersionForOptimisticLockingRequired ? compareResult != 0 : compareResult < 0) {
            throw OptimisticLockUtil.throwModified(objRefHelper.entityToObjRef(clone), versionToMerge, obj);
        }
    }

    @Override
    public ICUDResult mergeDeep(Object obj, MergeHandle handle) {
        LinkedHashMap<Class<?>, List<Object>> typeToObjectsToMerge = null;
        var entityPersistOrder = entityMetaDataProvider.getEntityPersistOrder();
        if (entityPersistOrder != null && entityPersistOrder.length > 0) {
            typeToObjectsToMerge = new LinkedHashMap<>();
        }
        var objRefs = new ArrayList<IObjRef>();
        var privilegedObjRefs = new ArrayList<IObjRef>();
        var valueHolderKeys = new ArrayList<ValueHolderRef>();
        var objectsToMerge = scanForInitializedObjects(obj, handle.isDeepMerge(), true, typeToObjectsToMerge, objRefs, privilegedObjRefs, valueHolderKeys, handle.getEntityToAssociatedCaches());
        var hardRef = new ArrayList<>();
        // Load all requested object originals in one roundtrip
        try {
            if (!privilegedObjRefs.isEmpty()) {
                hardRef.add(batchLoadOriginalState(handle, true, privilegedObjRefs, valueHolderKeys));
            }
            if (!objRefs.isEmpty()) {
                hardRef.add(batchLoadOriginalState(handle, false, objRefs, valueHolderKeys));
            }
            if (typeToObjectsToMerge != null) {
                for (var orderedEntityType : entityPersistOrder) {
                    var objectsToMergeOfOrderedType = typeToObjectsToMerge.remove(orderedEntityType);
                    if (objectsToMergeOfOrderedType == null) {
                        continue;
                    }
                    mergeDeepStart(objectsToMergeOfOrderedType, handle);
                }
                for (var entry : typeToObjectsToMerge) {
                    var objectsToMergeOfUnorderedType = entry.getValue();
                    mergeDeepStart(objectsToMergeOfUnorderedType, handle);
                }
            } else if (!objectsToMerge.isEmpty()) {
                mergeDeepStart(objectsToMerge, handle);
            }
            return cudResultHelper.createCUDResult(handle);
        } finally {
            if (handle.isPrivilegedCacheToDispose()) {
                ((IDisposableCache) handle.getPrivilegedCache()).dispose();
                handle.setPrivilegedCache(null);
            }
            if (handle.isCacheToDispose()) {
                ((IDisposableCache) handle.getCache()).dispose();
                handle.setCache(null);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    protected void mergeDeepIntern(Object obj, MergeHandle handle) {
        if (obj == null) {
            return;
        }
        if (obj instanceof List) {
            if (!handle.getAlreadyProcessedSet().add(obj)) {
                return;
            }
            var objList = (List) obj;
            for (int a = 0, size = objList.size(); a < size; a++) {
                mergeOrPersist(objList.get(a), handle);
            }
        } else if (obj instanceof Iterable) {
            if (!handle.getAlreadyProcessedSet().add(obj)) {
                return;
            }
            var iter = ((Iterable<?>) obj).iterator();
            while (iter.hasNext()) {
                mergeOrPersist(iter.next(), handle);
            }
        } else if (obj.getClass().isArray()) {
            var array = (Object[]) obj;
            for (int a = array.length; a-- > 0; ) {
                mergeOrPersist(array[a], handle);
            }
        } else {
            mergeOrPersist(obj, handle);
        }
    }

    protected void mergeDeepStart(Object obj, MergeHandle handle) {
        if (handle.getPendingValueHolders().size() > 0) {
            List<Object> pendingValueHolders = handle.getPendingValueHolders();
            prefetchHelper.prefetch(pendingValueHolders);
            pendingValueHolders.clear();
        }
        mergeDeepIntern(obj, handle);

        while (true) {
            var pendingRunnables = handle.getPendingRunnables();
            var pendingValueHolders = handle.getPendingValueHolders();
            if (pendingValueHolders.isEmpty() && pendingRunnables.isEmpty()) {
                return;
            }
            if (!pendingValueHolders.isEmpty()) {
                prefetchHelper.prefetch(pendingValueHolders);
                pendingValueHolders.clear();
            }
            if (!pendingRunnables.isEmpty()) {
                var pendingRunnablesClone = new ArrayList<>(pendingRunnables);
                pendingRunnables.clear();
                for (int a = 0, size = pendingRunnablesClone.size(); a < size; a++) {
                    pendingRunnablesClone.get(a).run();
                }
            }
        }
    }

    protected void mergeOrPersist(Object obj, MergeHandle handle) {
        if (obj instanceof Optional) {
            var opt = (Optional<?>) obj;
            if (!opt.isPresent()) {
                return;
            }
            mergeOrPersist(opt.get(), handle);
            return;
        }
        if (obj == null || !handle.alreadyProcessedSet.add(obj)) {
            return;
        }
        if (obj instanceof IDataObject) {
            var dataObject = (IDataObject) obj;
            if (!dataObject.hasPendingChanges()) {
                return;
            }
            if (dataObject.isToBeDeleted()) {
                handle.objToDeleteSet.add(obj);
                return;
            }
        }
        var metaData = entityMetaDataProvider.getMetaData(obj.getClass());
        metaData.prePersist(obj);
        var key = metaData.getIdMember().getValue(obj, false);
        if (key == null) {
            persist(obj, handle);
            return;
        }
        var isEntityFromPrivilegedCache = ((IObjRefContainer) obj).get__Cache().isPrivileged();
        var cache = isEntityFromPrivilegedCache ? handle.getPrivilegedCache() : handle.getCache();
        if (cache == null) {
            throw new IllegalStateException("Object has been cloned somewhere");
        }
        var clone = cache.getObject(metaData.getEntityType(), key, CacheDirective.none());
        if (clone == null) {
            throw OptimisticLockUtil.throwDeleted(objRefHelper.entityToObjRef(obj), obj);
        }
        merge(obj, clone, handle);
    }

    protected void mergePrimitivesFieldBased(IEntityMetaData metaData, Object obj, Object clone, MergeHandle handle) {
        for (var primitiveMember : metaData.getPrimitiveMembers()) {
            if (!metaData.isMergeRelevant(primitiveMember)) {
                continue;
            }
            var objValue = primitiveMember.getValue(obj, true);
            var cloneValue = primitiveMember.getValue(clone, true);
            if (!arePrimitivesEqual(metaData, primitiveMember, objValue, cloneValue, handle)) {
                addModification(obj, primitiveMember.getName(), primitiveMember.getElementType(), objValue, cloneValue, handle);
            }
        }
    }

    protected void persist(Object obj, MergeHandle handle) {
        var metaData = entityMetaDataProvider.getMetaData(obj.getClass());

        // Ensure entity will be persisted even if no single property is specified
        addModification(obj, handle);

        var relationMembers = metaData.getRelationMembers();
        if (relationMembers.length > 0) {
            var vhc = (IObjRefContainer) obj;

            for (int relationIndex = relationMembers.length; relationIndex-- > 0; ) {
                var relationMember = relationMembers[relationIndex];
                if (ValueHolderState.INIT != vhc.get__State(relationIndex)) {
                    continue;
                }
                var objMember = relationMember.getValue(obj, false);

                if (objMember == null) {
                    continue;
                }
                addOriModification(obj, relationMember.getName(), objMember, null, handle);
            }
        }
        for (var primitiveMember : metaData.getPrimitiveMembers()) {
            if (!metaData.isMergeRelevant(primitiveMember)) {
                continue;
            }
            var objMember = primitiveMember.getValue(obj, true);
            if (objMember != null) {
                addModification(obj, primitiveMember.getName(), primitiveMember.getElementType(), objMember, null, handle);
            }
        }
    }

    protected void putInstancesToCurrentCache(List<Object> validObjects, Function<Object, ICache> entityToAssociatedCaches) {
        if (validObjects.isEmpty() || !MergeProcess.isAddNewlyPersistedEntities()) {
            return;
        }
        if (entityToAssociatedCaches != null) {
            var cacheToEntitiesMap = buildCacheToEntitiesMap(validObjects, entityToAssociatedCaches);
            var rollback = cacheModification.pushActive();
            try {
                var objToObjRefMap = new IdentityHashMap<Object, IObjRef>();
                var objRefCache = new IObjRefHelper.IObjRefHelperState() {
                    @Override
                    public Map<Object, IObjRef> getObjToObjRefMap() {
                        return objToObjRefMap;
                    }
                };
                for (var entry : cacheToEntitiesMap) {
                    var cache = entry.getKey();
                    var entities = entry.getValue();

                    var objRefs = new ArrayList<IObjRef>();
                    var privilegedObjRefs = new ArrayList<IObjRef>();
                    var valueHolderKeys = new ArrayList<ValueHolderRef>();

                    var unownedObjectsToCache = new ArrayList<>();
                    var objRefsToFetchFromCache = new ArrayList<IObjRef>();
                    var entitiesToRefresh = new IdentityHashSet<>();
                    var alreadyHandled = new IdentityHashSet<>();
                    deepScanRecursion.handleDeep(entities, new IDeepScanRecursion.EntityDelegate() {
                        Object parent;

                        RelationMember parentMember;

                        @Override
                        public boolean visitEntity(Object entity, IDeepScanRecursion.Proceed proceed) {
                            var cacheOfEntity = ((IObjRefContainer) entity).get__Cache();
                            if (cacheOfEntity == null) {
                                unownedObjectsToCache.add(entity);
                                return true;
                            } else if (cacheOfEntity instanceof IGCProxy gcProxy) {
                                cacheOfEntity = (IWritableCache) gcProxy.getGCProxyTarget();
                            }
                            if (cacheOfEntity == cache) {
                                // cache of this nested entity is in sync with the outer cache. nothing to do
                                return true;
                            }
                            if (parent != null) {
                                var objRef = objRefHelper.getCreateObjRef(entity, objRefCache);
                                objRefsToFetchFromCache.add(objRef);
                                entitiesToRefresh.add(parent);
                                // clear the relation from parent to this entity as we must refetch it from the correct cache and relink it from the parent entity
                                if (parentMember.isToMany()) {
                                    var coll = ((Collection<?>) parentMember.getValue(parent, false));
                                    coll.remove(entity);
                                } else {
                                    parentMember.setValue(parent, null);
                                }
                            }
                            if (!alreadyHandled.add(entity)) {
                                return true;
                            }
                            var relationMembers = ((IEntityMetaDataHolder) entity).get__EntityMetaData().getRelationMembers();
                            if (relationMembers.length == 0) {
                                return true;
                            }
                            var oldParent = parent;
                            var oldParentMember = parentMember;
                            parent = entity;
                            try {
                                var vhc = (IObjRefContainer) entity;
                                for (int relationIndex = relationMembers.length; relationIndex-- > 0; ) {
                                    if (!vhc.is__Initialized(relationIndex)) {
                                        continue;
                                    }
                                    var relationMember = relationMembers[relationIndex];
                                    var item = relationMember.getValue(entity);
                                    if (item == null) {
                                        continue;
                                    }
                                    parentMember = relationMember;
                                    if (!proceed.proceed(item)) {
                                        return false;
                                    }
                                }
                                return true;
                            } finally {
                                parent = oldParent;
                                parentMember = oldParentMember;
                            }
                        }
                    }, false);

                    var fetchedHardRefs = cache.getObjects(objRefsToFetchFromCache, CacheDirective.cacheValueResult());

                    var fixWrongTransitiveCacheReferences = false;

                    if (!unownedObjectsToCache.isEmpty()) {
                        cache.put(unownedObjectsToCache);
                    }
                    if (!entitiesToRefresh.isEmpty()) {
                        revertChangesHelper.revertChanges(entitiesToRefresh, null, false, false);
                    }
                }
                return;
            } finally {
                rollback.rollback();
            }
        }
        var currentCache = (IWritableCache) cacheProvider.getCurrentCache();
        currentCache.put(validObjects);
    }

    private IdentityHashMap<IWritableCache, List<Object>> buildCacheToEntitiesMap(List<Object> validObjects, Function<Object, ICache> entityToAssociatedCaches) {
        IWritableCache currentCache = null;
        var cacheToEntitiesMap = new IdentityHashMap<IWritableCache, List<Object>>();
        for (var validObject : validObjects) {
            var cache = (IWritableCache) entityToAssociatedCaches.apply(validObject);
            if (cache == null) {
                if (currentCache == null) {
                    currentCache = (IWritableCache) cacheProvider.getCurrentCache();
                }
                cache = currentCache;
            }
            if (cache instanceof IGCProxy gcProxy) {
                cache = (IWritableCache) gcProxy.getGCProxyTarget();
            }
            var entities = cacheToEntitiesMap.computeIfAbsent(cache, key -> new ArrayList<>());
            entities.add(validObject);
        }
        return cacheToEntitiesMap;
    }

    @Override
    public void registerMergeExtension(IMergeExtension mergeExtension) {
        mergeExtensions.register(mergeExtension);
    }

    @Override
    public List<Object> scanForInitializedObjects(Object obj, boolean isDeepMerge, boolean dirtyOnly, Map<Class<?>, List<Object>> typeToObjectsToMerge, List<IObjRef> objRefs,
            List<IObjRef> privilegedObjRefs, List<ValueHolderRef> valueHolderKeys, Map<Object, ICache> entityToAssociatedCaches) {
        var objects = new ArrayList<>();
        var entityTraversalStack = entityToAssociatedCaches != null ? new ArrayList<>() : null;
        var unassociatedEntityToParent = entityToAssociatedCaches != null ? new IdentityHashMap<>() : null;

        var isFilterActivated = securityActivation.isFilterActivated();
        Predicate<IDataObject> dataObjectPredicate = dirtyOnly ? dataObject -> dataObject.hasPendingChanges() : dataObject -> true;
        deepScanRecursion.handleDeep(obj, new IDeepScanRecursion.EntityDelegate() {
            @Override
            public boolean visitEntity(Object entity, IDeepScanRecursion.Proceed proceed) {
                ICache cache = null;
                if (entityToAssociatedCaches != null) {
                    cache = ((IObjRefContainer) entity).get__Cache();
                    var parentEntity = entityTraversalStack.peek();
                    if (cache != null) {
                        entityToAssociatedCaches.put(entity, cache);
                        while (parentEntity != null) {
                            var parentOfParentEntity = unassociatedEntityToParent.remove(parentEntity);
                            if (parentOfParentEntity == null) {
                                break;
                            }
                            ((IObjRefContainer) parentEntity).set__Cache(cache);
                            entityToAssociatedCaches.put(parentEntity, cache);
                            if (parentOfParentEntity != NO_PARENT) {
                                parentEntity = parentOfParentEntity;
                            } else {
                                parentEntity = null;
                            }
                        }
                    } else {
                        // this is presumably a new entity which may potentially be referenced by an existing entity owned by an existing cache instance
                        if (parentEntity != null) {
                            var parentCache = ((IObjRefContainer) parentEntity).get__Cache();
                            if (parentCache != null) {
                                ((IObjRefContainer) entity).set__Cache(parentCache);
                                entityToAssociatedCaches.put(entity, parentCache);
                                cache = parentCache;
                            } else {
                                // even the parent is currently not associated to a cache, so we mark it for post-processing
                                unassociatedEntityToParent.put(entity, parentEntity);
                            }
                        } else {
                            // this is a root entity in the tree - so we mark it for post-processing
                            unassociatedEntityToParent.put(entity, NO_PARENT);
                        }
                    }
                }
                IEntityMetaData metaData = null;
                IObjRef objRef = null;
                if (objects != null || objRefs != null || privilegedObjRefs != null || valueHolderKeys != null) {
                    metaData = ((IEntityMetaDataHolder) entity).get__EntityMetaData();
                    objRef = markObjRef(typeToObjectsToMerge, objRefs, privilegedObjRefs, entity, metaData, objects, isFilterActivated, dataObjectPredicate);
                }
                if (!isDeepMerge) {
                    return true;
                }
                if (metaData == null) {
                    metaData = ((IEntityMetaDataHolder) entity).get__EntityMetaData();
                }
                var relationMembers = metaData.getRelationMembers();
                if (relationMembers.length == 0) {
                    return true;
                }
                if (entityTraversalStack != null) {
                    entityTraversalStack.add(entity);
                }
                try {
                    var vhc = (IObjRefContainer) entity;
                    for (int relationIndex = relationMembers.length; relationIndex-- > 0; ) {
                        if (!vhc.is__Initialized(relationIndex)) {
                            continue;
                        }
                        var relationMember = relationMembers[relationIndex];
                        var item = relationMember.getValue(entity);
                        if (valueHolderKeys != null && objRef != null && item != null) {
                            var vhk = new ValueHolderRef(objRef, relationMember, relationIndex);
                            valueHolderKeys.add(vhk);
                        }
                        if (!proceed.proceed(item)) {
                            return false;
                        }
                    }
                    return true;
                } finally {
                    if (entityTraversalStack != null) {
                        entityTraversalStack.popLastElement();
                    }
                }
            }
        }, true);
        if (unassociatedEntityToParent != null) {
            var entities = new ArrayList<>(unassociatedEntityToParent.size());
            unassociatedEntityToParent.keySet(entities);
            for (var entity : entities) {
                while (true) {
                    var parentEntity = unassociatedEntityToParent.remove(entity);
                    if (parentEntity == null || parentEntity == NO_PARENT) {
                        // no existing entity related - neither before nor after the depth-first traversal. as a consequence it stays unassociated for now
                        break;
                    }
                    var parentCache = ((IObjRefContainer) parentEntity).get__Cache();
                    if (parentCache == null) {
                        break;
                    }
                    ((IObjRefContainer) entity).set__Cache(parentCache);
                    entityToAssociatedCaches.put(entity, parentCache);
                    entity = parentEntity;
                }
            }
        }

        return objects;
    }

    private IObjRef markObjRef(Map<Class<?>, List<Object>> typeToObjectsToMerge, List<IObjRef> objRefs, List<IObjRef> privilegedObjRefs, Object entity, IEntityMetaData metaData, List<Object> objects,
            boolean isFilterActivated, Predicate<IDataObject> dataObjectPredicate) {
        var id = metaData.getIdMember().getValue(entity, false);
        var isEntityFromPrivilegedCache = false;
        IObjRef objRef = null;
        if (id != null) {
            objRef = objRefFactory.createObjRef(metaData.getEntityType(), ObjRef.PRIMARY_KEY_INDEX, id, null);
            var cache = ((IObjRefContainer) entity).get__Cache();
            if (cache == null && isFilterActivated) {
                throw new SecurityException("Entity '" + objRef + "' seems to be not readable by current user");
            }
            isEntityFromPrivilegedCache = cache.isPrivileged();
        }
        if (!(entity instanceof IDataObject) || dataObjectPredicate.test((IDataObject) entity)) {
            if (typeToObjectsToMerge != null) {
                var objectsToMerge = typeToObjectsToMerge.get(metaData.getEntityType());
                if (objectsToMerge == null) {
                    objectsToMerge = new ArrayList<>();
                    typeToObjectsToMerge.put(metaData.getEntityType(), objectsToMerge);
                }
                objectsToMerge.add(entity);
            }
            objects.add(entity);
            if (isEntityFromPrivilegedCache) {
                if (privilegedObjRefs != null) {
                    privilegedObjRefs.add(objRef);
                }
            } else {
                if (objRefs != null) {
                    objRefs.add(objRef);
                }
            }
        }
        return objRef;
    }

    @Override
    public void unregisterMergeExtension(IMergeExtension mergeExtension) {
        mergeExtensions.unregister(mergeExtension);
    }
}
