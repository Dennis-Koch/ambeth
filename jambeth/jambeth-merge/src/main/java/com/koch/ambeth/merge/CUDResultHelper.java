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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.extendable.ClassExtendableContainer;
import com.koch.ambeth.merge.model.ICUDResult;
import com.koch.ambeth.merge.model.IChangeContainer;
import com.koch.ambeth.merge.model.IDirectObjRef;
import com.koch.ambeth.merge.model.IPrimitiveUpdateItem;
import com.koch.ambeth.merge.model.IRelationUpdateItem;
import com.koch.ambeth.merge.transfer.CUDResult;
import com.koch.ambeth.merge.transfer.CreateContainer;
import com.koch.ambeth.merge.transfer.DeleteContainer;
import com.koch.ambeth.merge.transfer.UpdateContainer;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.IMap;

public class CUDResultHelper implements IInitializingBean, ICUDResultHelper, ICUDResultExtendable {
    protected final ClassExtendableContainer<ICUDResultExtension> extensions = new ClassExtendableContainer<>(ICUDResultExtension.class.getSimpleName(), "entityType");
    protected IEntityMetaDataProvider entityMetaDataProvider;
    protected IObjRefHelper oriHelper;

    @Override
    public void afterPropertiesSet() {
        ParamChecker.assertNotNull(entityMetaDataProvider, "entityMetaDataProvider");
        ParamChecker.assertNotNull(oriHelper, "oriHelper");
    }

    public void setOriHelper(IObjRefHelper oriHelper) {
        this.oriHelper = oriHelper;
    }

    public void setEntityMetaDataProvider(IEntityMetaDataProvider entityMetaDataProvider) {
        this.entityMetaDataProvider = entityMetaDataProvider;
    }

    @Override
    public ICUDResult createCUDResult(MergeHandle mergeHandle) {
        var typeToCudResultExtension = extensions.getExtensions();
        for (var entry : typeToCudResultExtension) {
            entry.getValue().extend(mergeHandle);
        }

        var objToModDict = mergeHandle.objToModDict;
        var objToDeleteSet = mergeHandle.objToDeleteSet;

        var entityTypeToFullPuis = new HashMap<Class<?>, IPrimitiveUpdateItem[]>();
        var entityTypeToFullRuis = new HashMap<Class<?>, IRelationUpdateItem[]>();

        var allChanges = new ArrayList<IChangeContainer>(objToModDict.size());
        var originalRefs = new ArrayList<>(objToModDict.size());

        for (var objToDelete : objToDeleteSet) {
            var ori = oriHelper.getCreateObjRef(objToDelete, mergeHandle);
            if (ori == null) {
                continue;
            }
            var deleteContainer = new DeleteContainer();
            deleteContainer.setReference(ori);
            allChanges.add(deleteContainer);
            originalRefs.add(objToDelete);
        }
        var entityMetaDataProvider = this.entityMetaDataProvider;
        for (var entry : objToModDict) {
            var obj = entry.getKey();
            var modItems = entry.getValue();

            var metaData = entityMetaDataProvider.getMetaData(obj.getClass());

            var fullPuis = getEnsureFullPUIs(metaData, entityTypeToFullPuis);
            var fullRuis = getEnsureFullRUIs(metaData, entityTypeToFullRuis);

            int puiCount = 0, ruiCount = 0;
            for (int a = modItems.size(); a-- > 0; ) {
                var modItem = modItems.get(a);

                var member = metaData.getMemberByName(modItem.getMemberName());

                if (modItem instanceof IRelationUpdateItem) {
                    fullRuis[metaData.getIndexByRelation(member)] = (IRelationUpdateItem) modItem;
                    ruiCount++;
                } else {
                    fullPuis[metaData.getIndexByPrimitive(member)] = (IPrimitiveUpdateItem) modItem;
                    puiCount++;
                }
            }

            var ruis = compactRUIs(fullRuis, ruiCount);
            var puis = compactPUIs(fullPuis, puiCount);
            var ori = oriHelper.getCreateObjRef(obj, mergeHandle);
            originalRefs.add(obj);

            if (ori instanceof IDirectObjRef) {
                var createContainer = new CreateContainer();

                ((IDirectObjRef) ori).setCreateContainerIndex(allChanges.size());

                createContainer.setReference(ori);
                createContainer.setPrimitives(puis);
                createContainer.setRelations(ruis);

                allChanges.add(createContainer);
            } else {
                var updateContainer = new UpdateContainer();
                updateContainer.setReference(ori);
                updateContainer.setPrimitives(puis);
                updateContainer.setRelations(ruis);
                allChanges.add(updateContainer);
            }
        }
        return new CUDResult(allChanges, originalRefs);
    }

    @Override
    public IPrimitiveUpdateItem[] getEnsureFullPUIs(IEntityMetaData metaData, IMap<Class<?>, IPrimitiveUpdateItem[]> entityTypeToFullPuis) {
        IPrimitiveUpdateItem[] fullPuis = entityTypeToFullPuis.get(metaData.getEntityType());
        if (fullPuis == null) {
            fullPuis = new IPrimitiveUpdateItem[metaData.getPrimitiveMembers().length];
            entityTypeToFullPuis.put(metaData.getEntityType(), fullPuis);
        }
        return fullPuis;
    }

    @Override
    public IRelationUpdateItem[] getEnsureFullRUIs(IEntityMetaData metaData, IMap<Class<?>, IRelationUpdateItem[]> entityTypeToFullRuis) {
        IRelationUpdateItem[] fullRuis = entityTypeToFullRuis.get(metaData.getEntityType());
        if (fullRuis == null) {
            fullRuis = new IRelationUpdateItem[metaData.getRelationMembers().length];
            entityTypeToFullRuis.put(metaData.getEntityType(), fullRuis);
        }
        return fullRuis;
    }

    @Override
    public IPrimitiveUpdateItem[] compactPUIs(IPrimitiveUpdateItem[] fullPUIs, int puiCount) {
        if (puiCount == 0) {
            return null;
        }
        IPrimitiveUpdateItem[] puis = new IPrimitiveUpdateItem[puiCount];
        for (int a = fullPUIs.length; a-- > 0; ) {
            IPrimitiveUpdateItem pui = fullPUIs[a];
            if (pui == null) {
                continue;
            }
            fullPUIs[a] = null;
            puis[--puiCount] = pui;
        }
        return puis;
    }

    @Override
    public IRelationUpdateItem[] compactRUIs(IRelationUpdateItem[] fullRUIs, int ruiCount) {
        if (ruiCount == 0) {
            return null;
        }
        IRelationUpdateItem[] ruis = new IRelationUpdateItem[ruiCount];
        for (int a = fullRUIs.length; a-- > 0; ) {
            IRelationUpdateItem rui = fullRUIs[a];
            if (rui == null) {
                continue;
            }
            fullRUIs[a] = null;
            ruis[--ruiCount] = rui;
        }
        return ruis;
    }

    @Override
    public void registerCUDResultExtension(ICUDResultExtension cudResultExtension, Class<?> entityType) {
        extensions.register(cudResultExtension, entityType);
    }

    @Override
    public void unregisterCUDResultExtension(ICUDResultExtension cudResultExtension, Class<?> entityType) {
        extensions.unregister(cudResultExtension, entityType);
    }
}
