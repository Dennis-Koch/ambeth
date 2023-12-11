package com.koch.ambeth.merge.util;

import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.RelationMember;

import java.util.Objects;

public class ValueHolderRef {
    protected IObjRef objRef;

    protected RelationMember member;

    protected int relationIndex;

    public ValueHolderRef(IObjRef objRef, RelationMember member, int relationIndex) {
        this.objRef = objRef;
        this.member = member;
        this.relationIndex = relationIndex;
    }

    public IObjRef getObjRef() {
        return objRef;
    }

    public RelationMember getMember() {
        return member;
    }

    public int getRelationIndex() {
        return relationIndex;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ValueHolderRef other) {
            return Objects.equals(getObjRef(), other.getObjRef()) && getRelationIndex() == other.getRelationIndex();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getObjRef().hashCode() ^ getRelationIndex();
    }
}
