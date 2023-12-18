package com.koch.ambeth.merge.model;

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

import java.util.Collection;
import java.util.List;

import com.koch.ambeth.merge.transfer.RelationUpdateItem;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.Arrays;
import com.koch.ambeth.util.IPrintable;
import com.koch.ambeth.util.collections.EmptySet;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.ISet;

public class RelationUpdateItemBuild implements IRelationUpdateItem, IPrintable {
    protected String memberName;

    protected ISet<IObjRef> addedORIs = EmptySet.<IObjRef>emptySet();

    protected ISet<IObjRef> removedORIs = EmptySet.<IObjRef>emptySet();

    public RelationUpdateItemBuild(String memberName) {
        this.memberName = memberName;
    }

    @Override
    public String getMemberName() {
        return memberName;
    }

    public int getAddedCount() {
        return addedORIs.size();
    }

    public int getRemovedCount() {
        return removedORIs.size();
    }

    @Override
    public IObjRef[] getAddedORIs() {
        if (addedORIs.isEmpty()) {
            return null;
        }
        return addedORIs.toArray(IObjRef[]::new);
    }

    @Override
    public IObjRef[] getRemovedORIs() {
        if (removedORIs.isEmpty()) {
            return null;
        }
        return removedORIs.toArray(IObjRef[]::new);
    }

    public void addObjRef(IObjRef objRef) {
        if (addedORIs.isEmpty()) {
            addedORIs = new HashSet<>();
        }
        addedORIs.add(objRef);
    }

    public void addObjRefs(IObjRef[] objRefs) {
        for (IObjRef objRef : objRefs) {
            addObjRef(objRef);
        }
    }

    public void addObjRefs(List<IObjRef> objRefs) {
        for (int a = 0, size = objRefs.size(); a < size; a++) {
            addObjRef(objRefs.get(a));
        }
    }

    public void addObjRefs(Collection<IObjRef> objRefs) {
        for (IObjRef objRef : objRefs) {
            addObjRef(objRef);
        }
    }

    public void removeObjRef(IObjRef objRef) {
        if (removedORIs.isEmpty()) {
            removedORIs = new HashSet<>();
        }
        removedORIs.add(objRef);
    }

    public void removeObjRefs(IObjRef[] objRefs) {
        for (IObjRef objRef : objRefs) {
            removeObjRef(objRef);
        }
    }

    public void removeObjRefs(List<IObjRef> objRefs) {
        for (int a = objRefs.size(); a-- > 0; ) {
            removeObjRef(objRefs.get(a));
        }
    }

    public void removeObjRefs(Collection<IObjRef> objRefs) {
        for (IObjRef objRef : objRefs) {
            removeObjRef(objRef);
        }
    }

    public IRelationUpdateItem buildRUI() {
        RelationUpdateItem rui = new RelationUpdateItem();
        rui.setMemberName(memberName);
        rui.setAddedORIs(getAddedORIs());
        rui.setRemovedORIs(getRemovedORIs());
        return rui;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb);
        return sb.toString();
    }

    @Override
    public void toString(StringBuilder sb) {
        sb.append("RUI: MemberName=").append(getMemberName());
        IObjRef[] addedORIs = getAddedORIs();
        IObjRef[] removedORIs = getRemovedORIs();
        if (addedORIs != null && addedORIs.length > 0) {
            sb.append(" AddedORIs=");
            Arrays.toString(sb, addedORIs);
        }
        if (removedORIs != null && removedORIs.length > 0) {
            sb.append(" RemovedORIs=");
            Arrays.toString(sb, removedORIs);
        }
    }
}
