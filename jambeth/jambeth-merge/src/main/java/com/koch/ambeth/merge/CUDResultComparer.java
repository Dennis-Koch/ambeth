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
import com.koch.ambeth.merge.model.CreateOrUpdateContainerBuild;
import com.koch.ambeth.merge.model.ICUDResult;
import com.koch.ambeth.merge.model.IChangeContainer;
import com.koch.ambeth.merge.model.IPrimitiveUpdateItem;
import com.koch.ambeth.merge.model.IRelationUpdateItem;
import com.koch.ambeth.merge.model.RelationUpdateItemBuild;
import com.koch.ambeth.merge.transfer.CUDResult;
import com.koch.ambeth.merge.transfer.CreateContainer;
import com.koch.ambeth.merge.transfer.UpdateContainer;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptyList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.IdentityHashMap;
import com.koch.ambeth.util.collections.LinkedHashSet;

import java.util.List;
import java.util.Objects;

public class CUDResultComparer implements ICUDResultComparer {
    @Autowired
    protected ICUDResultHelper cudResultHelper;
    @Autowired
    protected IEntityMetaDataProvider entityMetaDataProvider;

    protected boolean equalsChangeContainer(CUDResultDiff cudResultDiff, IChangeContainer left, IChangeContainer right) {
        if (left.getClass() != right.getClass()) {
            throw new IllegalStateException("Must never happen");
        }
        cudResultDiff.setLeftContainer(left);
        try {
            if (left instanceof CreateContainer) {
                var leftCreate = (CreateContainer) left;
                var rightCreate = (CreateContainer) right;
                var isEqual = equalsPUIs(cudResultDiff, leftCreate.getPrimitives(), rightCreate.getPrimitives());
                if (!isEqual) {
                    if (!cudResultDiff.doFullDiff) {
                        return false;
                    }
                }
                isEqual &= equalsRUIs(cudResultDiff, leftCreate.getRelations(), rightCreate.getRelations());
                if (!isEqual) {
                    if (!cudResultDiff.doFullDiff) {
                        return false;
                    }
                }
                return isEqual;
            }
            if (left instanceof UpdateContainer) {
                var leftUpdate = (UpdateContainer) left;
                var rightUpdate = (UpdateContainer) right;
                var isEqual = equalsPUIs(cudResultDiff, leftUpdate.getPrimitives(), rightUpdate.getPrimitives());
                if (!isEqual) {
                    if (!cudResultDiff.doFullDiff) {
                        return false;
                    }
                }
                isEqual &= equalsRUIs(cudResultDiff, leftUpdate.getRelations(), rightUpdate.getRelations());
                if (!isEqual) {
                    if (!cudResultDiff.doFullDiff) {
                        return false;
                    }
                }
                return isEqual;
            }
            // a DeleteContainer is only compared by the reference. But we know that this is already equal
            // since we entered this method
            return true;
        } finally {
            cudResultDiff.setLeftContainer(null);
        }
    }

    @Override
    public boolean equalsCUDResult(ICUDResult left, ICUDResult right) {
        var diff = new CUDResultDiff(left, right, false, cudResultHelper, entityMetaDataProvider);
        return equalsCUDResult(diff);
    }

    @Override
    public ICUDResult diffCUDResult(ICUDResult left, ICUDResult right) {
        var diff = new CUDResultDiff(left, right, true, cudResultHelper, entityMetaDataProvider);
        equalsCUDResult(diff);

        if (!diff.hasChanges()) {
            return null; // null means empty diff
        }
        var diffChanges = diff.diffChanges;
        for (int a = diffChanges.size(); a-- > 0; ) {
            var changeContainer = diffChanges.get(a);
            if (!(changeContainer instanceof CreateOrUpdateContainerBuild)) {
                continue;
            }
            diffChanges.set(a, ((CreateOrUpdateContainerBuild) changeContainer).build());
        }
        return new CUDResult(diffChanges, diff.originalRefs);
    }

    protected boolean equalsCUDResult(CUDResultDiff cudResultDiff) {
        var left = cudResultDiff.left;
        var right = cudResultDiff.right;
        var leftRefs = left.getOriginalRefs();
        var rightRefs = right.getOriginalRefs();
        if (leftRefs.size() != rightRefs.size()) {
            if (!cudResultDiff.doFullDiff) {
                return false;
            }
        }
        var leftChanges = left.getAllChanges();
        var rightChanges = right.getAllChanges();
        var rightMap = IdentityHashMap.<Object, Integer>create(rightRefs.size());
        for (int a = rightRefs.size(); a-- > 0; ) {
            rightMap.put(rightRefs.get(a), Integer.valueOf(a));
        }
        for (int a = leftRefs.size(); a-- > 0; ) {
            var leftEntity = leftRefs.get(a);
            var rightIndex = rightMap.remove(leftEntity);
            if (rightIndex == null) {
                if (!cudResultDiff.doFullDiff) {
                    return false;
                }
                cudResultDiff.diffChanges.add(leftChanges.get(a));
                cudResultDiff.originalRefs.add(leftEntity);
                continue;
            }
            if (!equalsChangeContainer(cudResultDiff, leftChanges.get(a), rightChanges.get(rightIndex.intValue()))) {
                if (!cudResultDiff.doFullDiff) {
                    if (cudResultDiff.containerBuild != null) {
                        throw new IllegalStateException();
                    }
                    return false;
                }
                cudResultDiff.diffChanges.add(cudResultDiff.containerBuild);
                cudResultDiff.originalRefs.add(rightRefs.get(rightIndex.intValue()));
                cudResultDiff.containerBuild = null;
            } else if (cudResultDiff.containerBuild != null) {
                throw new IllegalStateException();
            }
        }
        if (rightMap.isEmpty()) {
            return true;
        }
        for (var entry : rightMap) {
            var rightRef = entry.getKey();
            int rightIndex = entry.getValue().intValue();
            var rightChange = rightChanges.get(rightIndex);
            cudResultDiff.diffChanges.add(rightChange);
            cudResultDiff.originalRefs.add(rightRef);
        }
        return false;
    }

    protected boolean equalsPUIs(CUDResultDiff cudResultDiff, IPrimitiveUpdateItem[] left, IPrimitiveUpdateItem[] right) {
        if (left == null || left.length == 0) {
            if (right == null || right.length == 0) {
                return true;
            }
            if (!cudResultDiff.doFullDiff) {
                return false;
            }
            var containerBuild = cudResultDiff.updateContainerBuild();
            for (var rightPui : right) {
                containerBuild.addPrimitive(rightPui);
            }
            return false;
        }
        if (right == null || right.length == 0) {
            throw new IllegalStateException("Must never happen");
        }
        if (left.length != right.length) {
            if (!cudResultDiff.doFullDiff) {
                return false;
            }
            int leftIndex = left.length - 1;
            for (int rightIndex = right.length; rightIndex-- > 0; ) {
                var leftPui = leftIndex >= 0 ? left[leftIndex] : null;
                var rightPui = right[rightIndex];
                if (leftPui == null || !leftPui.getMemberName().equals(rightPui.getMemberName())) {
                    var containerBuild = cudResultDiff.updateContainerBuild();
                    containerBuild.addPrimitive(rightPui);
                    continue;
                }
                if (!equalsPUI(cudResultDiff, leftPui, rightPui)) {
                    if (!cudResultDiff.doFullDiff) {
                        return false;
                    }
                    var containerBuild = cudResultDiff.updateContainerBuild();
                    containerBuild.addPrimitive(rightPui);
                }
                leftIndex--;
            }
            return false;
        }
        var isEqual = true;
        for (int a = left.length; a-- > 0; ) {
            var rightPui = right[a];
            if (!equalsPUI(cudResultDiff, left[a], rightPui)) {
                if (!cudResultDiff.doFullDiff) {
                    return false;
                }
                var containerBuild = cudResultDiff.updateContainerBuild();
                containerBuild.addPrimitive(rightPui);
                isEqual = false;
            }
        }
        return isEqual;
    }

    protected boolean equalsPUI(CUDResultDiff cudResultDiff, IPrimitiveUpdateItem left, IPrimitiveUpdateItem right) {
        if (Objects.equals(left.getNewValue(), right.getNewValue())) {
            return true;
        }
        if (!cudResultDiff.doFullDiff) {
            return false;
        }
        var containerBuild = cudResultDiff.updateContainerBuild();
        containerBuild.addPrimitive(right);
        return false;
    }

    protected boolean equalsRUIs(CUDResultDiff cudResultDiff, IRelationUpdateItem[] left, IRelationUpdateItem[] right) {
        if (left == null || left.length == 0) {
            if (right == null || right.length == 0) {
                return true;
            }
            if (cudResultDiff.doFullDiff) {
                var containerBuild = cudResultDiff.updateContainerBuild();
                for (var rightRui : right) {
                    containerBuild.addRelation(rightRui);
                }
            }
            return false;
        }
        if (right == null || right.length == 0) {
            throw new IllegalStateException("Must never happen");
        }
        if (left.length != right.length) {
            if (!cudResultDiff.doFullDiff) {
                return false;
            }
            int leftIndex = left.length - 1;
            for (int rightIndex = right.length; rightIndex-- > 0; ) {
                var leftRui = leftIndex >= 0 ? left[leftIndex] : null;
                var rightRui = right[rightIndex];
                if (leftRui == null || !leftRui.getMemberName().equals(rightRui.getMemberName())) {
                    var containerBuild = cudResultDiff.updateContainerBuild();
                    containerBuild.addRelation(rightRui);
                    continue;
                }
                if (!equalsRUI(cudResultDiff, leftRui, rightRui)) {
                    if (!cudResultDiff.doFullDiff) {
                        return false;
                    }
                }
                leftIndex--;
            }
            return false;
        }
        var isEqual = true;
        for (int a = left.length; a-- > 0; ) {
            var rightPui = right[a];
            if (!equalsRUI(cudResultDiff, left[a], rightPui)) {
                if (!cudResultDiff.doFullDiff) {
                    return false;
                }
                isEqual = false;
            }
        }
        return isEqual;
    }

    protected boolean equalsRUI(CUDResultDiff cudResultDiff, IRelationUpdateItem left, IRelationUpdateItem right) {
        // we do NOT have to check each relational ObjRef because IF an objRef is in the scope it must
        // not be removed afterwards
        // so we know by design that the arrays can only grow

        try {
            var leftORIs = left.getAddedORIs();
            var rightORIs = right.getAddedORIs();

            if (leftORIs == null) {
                if (rightORIs != null) {
                    if (!cudResultDiff.doFullDiff) {
                        return false;
                    }
                    var relationBuild = cudResultDiff.updateRelationBuild();
                    relationBuild.addObjRefs(rightORIs);
                }
            } else if (rightORIs == null) {
                throw new IllegalStateException("Must never happen");
            } else if (leftORIs.length != rightORIs.length) {
                if (!cudResultDiff.doFullDiff) {
                    return false;
                }
                var added = new LinkedHashSet<>(leftORIs);
                added.removeAll(rightORIs);

                var relationBuild = cudResultDiff.updateRelationBuild();
                relationBuild.addObjRefs(added);
            }
            leftORIs = left.getRemovedORIs();
            rightORIs = right.getRemovedORIs();
            if (leftORIs == null) {
                if (rightORIs != null) {
                    if (!cudResultDiff.doFullDiff) {
                        return false;
                    }
                    var relationBuild = cudResultDiff.updateRelationBuild();
                    relationBuild.removeObjRefs(rightORIs);
                }
            } else if (rightORIs == null) {
                throw new IllegalStateException("Must never happen");
            } else if (leftORIs.length != rightORIs.length) {
                if (!cudResultDiff.doFullDiff) {
                    return false;
                }
                var removed = new LinkedHashSet<>(leftORIs);
                removed.removeAll(rightORIs);

                var relationBuild = cudResultDiff.updateRelationBuild();
                relationBuild.removeObjRefs(removed);
            }
            return true;
        } finally {
            cudResultDiff.relationBuild = null;
        }
    }

    public static class CUDResultDiff {
        public final boolean doFullDiff;

        public final ICUDResult left;

        public final ICUDResult right;

        public final List<IChangeContainer> diffChanges;

        public final List<Object> originalRefs;
        private final ICUDResultHelper cudResultHelper;
        private final IEntityMetaDataProvider entityMetaDataProvider;
        public String memberName;
        public CreateOrUpdateContainerBuild containerBuild;
        public RelationUpdateItemBuild relationBuild;
        protected boolean hasChanges;
        private IChangeContainer leftContainer;
        private HashMap<Class<?>, HashMap<String, Integer>> typeToMemberNameToIndexMap;
        private HashMap<Class<?>, HashMap<String, Integer>> typeToPrimitiveMemberNameToIndexMap;

        public CUDResultDiff(ICUDResult left, ICUDResult right, boolean doFullDiff, ICUDResultHelper cudResultHelper, IEntityMetaDataProvider entityMetaDataProvider) {
            this.doFullDiff = doFullDiff;
            this.left = left;
            this.right = right;
            this.cudResultHelper = cudResultHelper;
            this.entityMetaDataProvider = entityMetaDataProvider;
            if (doFullDiff) {
                diffChanges = new ArrayList<>();
                originalRefs = new ArrayList<>();
            } else {
                diffChanges = EmptyList.<IChangeContainer>getInstance();
                originalRefs = EmptyList.<Object>getInstance();
            }
        }

        public void setHasChanges(boolean hasChanges) {
            this.hasChanges = hasChanges;
        }

        public boolean hasChanges() {
            return hasChanges || !diffChanges.isEmpty();
        }

        public CreateOrUpdateContainerBuild updateContainerBuild() {
            if (containerBuild != null) {
                return containerBuild;
            }
            var metaData = entityMetaDataProvider.getMetaData(getLeftContainer().getReference().getRealType());
            containerBuild = new CreateOrUpdateContainerBuild(metaData, leftContainer instanceof CreateContainer, getOrCreateRelationMemberNameToIndexMap(metaData.getEntityType()),
                    getOrCreatePrimitiveMemberNameToIndexMap(metaData.getEntityType()), cudResultHelper);
            containerBuild.setReference(getLeftContainer().getReference());
            return containerBuild;
        }

        public RelationUpdateItemBuild updateRelationBuild() {
            if (relationBuild != null) {
                return relationBuild;
            }
            relationBuild = new RelationUpdateItemBuild(memberName);
            return relationBuild;
        }

        protected HashMap<String, Integer> getOrCreateRelationMemberNameToIndexMap(Class<?> entityType) {
            if (typeToMemberNameToIndexMap == null) {
                typeToMemberNameToIndexMap = new HashMap<>();
            }
            var memberNameToIndexMap = typeToMemberNameToIndexMap.get(entityType);
            if (memberNameToIndexMap != null) {
                return memberNameToIndexMap;
            }
            var metaData = entityMetaDataProvider.getMetaData(entityType);
            var relationMembers = metaData.getRelationMembers();
            memberNameToIndexMap = HashMap.create(relationMembers.length);
            for (int a = relationMembers.length; a-- > 0; ) {
                memberNameToIndexMap.put(relationMembers[a].getName(), Integer.valueOf(a));
            }
            typeToMemberNameToIndexMap.put(entityType, memberNameToIndexMap);
            return memberNameToIndexMap;
        }

        protected HashMap<String, Integer> getOrCreatePrimitiveMemberNameToIndexMap(Class<?> entityType) {
            if (typeToPrimitiveMemberNameToIndexMap == null) {
                typeToPrimitiveMemberNameToIndexMap = new HashMap<>();
            }
            var memberNameToIndexMap = typeToPrimitiveMemberNameToIndexMap.get(entityType);
            if (memberNameToIndexMap != null) {
                return memberNameToIndexMap;
            }
            var metaData = entityMetaDataProvider.getMetaData(entityType);
            var primitiveMembers = metaData.getPrimitiveMembers();
            memberNameToIndexMap = HashMap.create(primitiveMembers.length);
            for (int a = primitiveMembers.length; a-- > 0; ) {
                memberNameToIndexMap.put(primitiveMembers[a].getName(), Integer.valueOf(a));
            }
            typeToPrimitiveMemberNameToIndexMap.put(entityType, memberNameToIndexMap);
            return memberNameToIndexMap;
        }

        public IChangeContainer getLeftContainer() {
            return leftContainer;
        }

        public void setLeftContainer(IChangeContainer leftContainer) {
            if (leftContainer != null && containerBuild != null) {
                throw new IllegalStateException();
            }
            this.leftContainer = leftContainer;
        }
    }
}
