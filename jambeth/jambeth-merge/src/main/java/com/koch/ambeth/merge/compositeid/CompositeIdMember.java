package com.koch.ambeth.merge.compositeid;

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

import com.koch.ambeth.merge.metadata.IMemberTypeProvider;
import com.koch.ambeth.service.metadata.IPrimitiveMemberWrite;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.service.metadata.PrimitiveMember;
import lombok.Getter;
import lombok.SneakyThrows;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompositeIdMember extends PrimitiveMember implements IPrimitiveMemberWrite {
    public static String filterEmbeddedFieldName(String fieldName) {
        return fieldName.replaceAll(Pattern.quote("."), Matcher.quoteReplacement("__"));
    }

    @Getter
    protected final Class<?> declaringType;

    @Getter
    protected final Class<?> realType;

    @Getter
    protected final PrimitiveMember[] members;

    @Getter
    protected final int membersCount;

    @Getter
    protected final Constructor<?> realTypeConstructorAccess;

    protected final Member[] fieldIndexOfMembers;

    @Getter
    protected final String name;

    protected boolean technicalMember;

    protected PrimitiveMember definedBy;

    private boolean isTransient;

    @SneakyThrows
    public CompositeIdMember(Class<?> declaringType, Class<?> realType, String name, PrimitiveMember[] members, IMemberTypeProvider memberTypeProvider) {
        this.declaringType = declaringType;
        this.realType = realType;
        this.name = name;
        this.members = members;
        this.membersCount = members.length;
        fieldIndexOfMembers = new Member[members.length];
        var paramTypes = new Class<?>[members.length];
        for (int a = 0, size = members.length; a < size; a++) {
            var member = members[a];
            fieldIndexOfMembers[a] = memberTypeProvider.getMember(realType, CompositeIdMember.filterEmbeddedFieldName(member.getName()));
            paramTypes[a] = member.getRealType();
        }
        realTypeConstructorAccess = realType.getConstructor(paramTypes);
    }

    @Override
    public boolean isTechnicalMember() {
        return technicalMember;
    }

    @Override
    public void setTechnicalMember(boolean technicalMember) {
        this.technicalMember = technicalMember;
    }

    @Override
    public boolean isTransient() {
        return isTransient;
    }

    @Override
    public void setTransient(boolean isTransient) {
        this.isTransient = isTransient;
    }

    @Override
    public PrimitiveMember getDefinedBy() {
        return definedBy;
    }

    @Override
    public void setDefinedBy(PrimitiveMember definedBy) {
        this.definedBy = definedBy;
    }

    @Override
    public Class<?> getEntityType() {
        return getRealType();
    }

    @Override
    public Class<?> getElementType() {
        return realType;
    }

    @Override
    public boolean isToMany() {
        return false;
    }

    @Override
    public boolean canRead() {
        return true;
    }

    @Override
    public boolean canWrite() {
        return true;
    }

    public Object getDecompositedValue(Object compositeId, int compositeMemberIndex) {
        return fieldIndexOfMembers[compositeMemberIndex].getValue(compositeId, false);
    }

    public void getDecompositedValues(Object compositeId, Object[] decompositedValues) {
        var fieldIndexOfMembers = this.fieldIndexOfMembers;
        for (int compositeMemberIndex = fieldIndexOfMembers.length; compositeMemberIndex-- > 0; ) {
            decompositedValues[compositeMemberIndex] = fieldIndexOfMembers[compositeMemberIndex].getValue(compositeId, false);
        }
    }

    public Object getDecompositedValueOfObject(Object obj, int compositeMemberIndex) {
        return members[compositeMemberIndex].getValue(obj, false);
    }

    @Override
    public Object getNullEquivalentValue() {
        return null;
    }

    @Override
    public Object getValue(Object obj) {
        return getValue(obj, true);
    }

    @SneakyThrows
    @Override
    public Object getValue(Object obj, boolean allowNullEquivalentValue) {
        var members = this.members;
        var args = new Object[members.length];
        for (int a = members.length; a-- > 0; ) {
            var memberValue = members[a].getValue(obj, allowNullEquivalentValue);
            if (memberValue == null) {
                return null;
            }
            args[a] = memberValue;
        }
        return realTypeConstructorAccess.newInstance(args);
    }

    @Override
    public void setValue(Object obj, Object compositeId) {
        var members = this.members;
        var fieldIndexOfMembers = this.fieldIndexOfMembers;
        if (compositeId != null) {
            for (int a = members.length; a-- > 0; ) {
                var memberValue = fieldIndexOfMembers[a].getValue(compositeId, false);
                members[a].setValue(obj, memberValue);
            }
        } else {
            for (int a = members.length; a-- > 0; ) {
                members[a].setValue(obj, null);
            }
        }
    }

    @Override
    public <V extends Annotation> V getAnnotation(Class<V> annotationType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return getName();
    }
}
