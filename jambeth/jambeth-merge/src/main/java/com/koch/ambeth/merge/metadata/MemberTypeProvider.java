package com.koch.ambeth.merge.metadata;

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
import com.koch.ambeth.ioc.bytecode.IBytecodeEnhancer;
import com.koch.ambeth.ioc.typeinfo.FieldPropertyInfo;
import com.koch.ambeth.merge.compositeid.ICompositeIdFactory;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.metadata.EmbeddedMember;
import com.koch.ambeth.service.metadata.IRelationMemberWrite;
import com.koch.ambeth.service.metadata.IntermediateEmbeddedPrimitiveMember;
import com.koch.ambeth.service.metadata.IntermediateEmbeddedRelationMember;
import com.koch.ambeth.service.metadata.IntermediatePrimitiveMember;
import com.koch.ambeth.service.metadata.IntermediateRelationMember;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.service.metadata.PrimitiveMember;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.annotation.Cascade;
import com.koch.ambeth.util.annotation.CascadeLoadMode;
import com.koch.ambeth.util.collections.Tuple3KeyEntry;
import com.koch.ambeth.util.collections.Tuple3KeyHashMap;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;
import lombok.SneakyThrows;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MemberTypeProvider implements IMemberTypeProvider, IIntermediateMemberTypeProvider {
    public static final Class<?>[] EMPTY_TYPES = new Class<?>[0];
    protected static final Object[] EMPTY_OBJECTS = new Object[0];

    public static IPropertyInfo[] buildPropertyPath(Class<?> entityType, String memberName, IPropertyInfoProvider propertyInfoProvider) {
        String[] memberPath = EmbeddedMember.split(memberName);
        Class<?> currType = entityType;
        IPropertyInfo[] propertyPath = new IPropertyInfo[memberPath.length];
        for (int a = 0, size = propertyPath.length; a < size; a++) {
            IPropertyInfo property = propertyInfoProvider.getProperty(currType, memberPath[a]);
            if (property == null) {
                Field[] fields = ReflectUtil.getDeclaredFieldInHierarchy(currType, memberPath[a]);
                if (fields.length == 0) {
                    throw new IllegalStateException("Path illegal: " + memberName);
                }
                property = new FieldPropertyInfo(currType, memberPath[a], fields[a]);
            }
            propertyPath[a] = property;
            currType = property.getPropertyType();
        }
        return propertyPath;
    }

    protected final TypeAndStringWeakMap<PrimitiveMember> typeToPrimitiveMemberMap = new TypeAndStringWeakMap<>();
    protected final TypeAndStringWeakMap<Member> typeToMemberMap = new TypeAndStringWeakMap<>();
    protected final TypeAndStringWeakMap<RelationMember> typeToRelationMemberMap = new TypeAndStringWeakMap<>();
    protected final Lock writeLock = new ReentrantLock();
    @Autowired
    protected IBytecodeEnhancer bytecodeEnhancer;
    @Autowired
    protected ICompositeIdFactory compositeIdFactory;
    @Autowired
    protected IProperties properties;
    @Autowired
    protected IPropertyInfoProvider propertyInfoProvider;

    @Override
    public RelationMember getRelationMember(Class<?> type, String propertyName) {
        return getMemberIntern(type, propertyName, null, typeToRelationMemberMap, RelationMember.class);
    }

    @Override
    public PrimitiveMember getPrimitiveMember(Class<?> type, String propertyName) {
        return getMemberIntern(type, propertyName, null, typeToPrimitiveMemberMap, PrimitiveMember.class);
    }

    @Override
    public PrimitiveMember getPrimitiveMember(Class<?> type, String propertyName, Class<?> forcedElementType) {
        return getMemberIntern(type, propertyName, forcedElementType, typeToPrimitiveMemberMap, PrimitiveMember.class);
    }

    @Override
    public Member getMember(Class<?> type, String propertyName) {
        return getMemberIntern(type, propertyName, null, typeToMemberMap, Member.class);
    }

    @SuppressWarnings("unchecked")
    protected <T extends Member> T getMemberIntern(Class<?> type, String propertyName, Class<?> forcedElementType, TypeAndStringWeakMap<T> map, Class<?> baseType) {
        var accessorR = map.get(type, propertyName, forcedElementType);
        var member = accessorR != null ? accessorR.get() : null;
        if (member != null) {
            return member;
        }
        var writeLock = this.writeLock;
        writeLock.lock();
        try {
            // concurrent thread might have been faster
            accessorR = map.get(type, propertyName, forcedElementType);
            member = accessorR != null ? accessorR.get() : null;
            if (member != null) {
                return member;
            }
        } finally {
            writeLock.unlock();
        }
        // necessary to release the lock because getMemberIntern() needs the IBytecodeEnhancer to work - but in some code paths the IBytecodeEnhancer also needs the MemberTypeProvider. if multiple
        // threads each build up their own bytecode classes at the same time sometimes a deadlock could occur without releasing the lock
        member = (T) getMemberIntern(type, propertyName, forcedElementType, baseType);
        writeLock.lock();
        try {
            accessorR = map.get(type, propertyName, forcedElementType);
            var existingMember = accessorR != null ? accessorR.get() : null;
            if (existingMember != null) {
                // concurrent thread might have been faster
                return existingMember;
            }
            if (member instanceof RelationMember) {
                CascadeLoadMode cascadeLoadMode = null;
                var cascadeAnnotation = member.getAnnotation(Cascade.class);
                if (cascadeAnnotation != null) {
                    cascadeLoadMode = cascadeAnnotation.load();
                }
                if (cascadeLoadMode == null || CascadeLoadMode.DEFAULT.equals(cascadeLoadMode)) {
                    cascadeLoadMode = CascadeLoadMode.valueOf(
                            properties.getString(member.isToMany() ? ServiceConfigurationConstants.ToManyDefaultCascadeLoadMode : ServiceConfigurationConstants.ToOneDefaultCascadeLoadMode,
                                    CascadeLoadMode.DEFAULT.toString()));
                }
                if (cascadeLoadMode == null || CascadeLoadMode.DEFAULT.equals(cascadeLoadMode)) {
                    cascadeLoadMode = CascadeLoadMode.LAZY;
                }
                ((IRelationMemberWrite) member).setCascadeLoadMode(cascadeLoadMode);
            }
            map.put(type, propertyName, forcedElementType, new WeakReference<>(member));
            return member;
        } finally {
            writeLock.unlock();
        }
    }

    @SneakyThrows
    protected Member getMemberIntern(Class<?> type, String propertyName, Class<?> forcedElementType, Class<?> baseType) {
        if (propertyName.contains("&")) {
            var compositePropertyNames = propertyName.split("&");
            var members = new PrimitiveMember[compositePropertyNames.length];
            for (int a = compositePropertyNames.length; a-- > 0; ) {
                members[a] = (PrimitiveMember) getMemberIntern(type, compositePropertyNames[a], null, baseType);
            }
            return compositeIdFactory.createCompositeIdMember(type, members);
        }

        var enhancedType = getMemberTypeIntern(type, propertyName, forcedElementType, baseType);
        if (enhancedType == baseType) {
            throw new IllegalStateException("Must never happen. No enhancement for " + baseType + " has been done");
        }
        Constructor<?> constructor = enhancedType.getConstructor(EMPTY_TYPES);
        return (Member) constructor.newInstance(EMPTY_OBJECTS);
    }

    protected Class<?> getMemberTypeIntern(Class<?> targetType, String propertyName, Class<?> forcedElementType, Class<?> baseType) {
        var memberTypeName = targetType.getName() + "$" + baseType.getSimpleName() + "$" + (forcedElementType != null ? forcedElementType.getSimpleName() : "") + "$" +
                propertyName.replaceAll(Pattern.quote("."), Matcher.quoteReplacement("$"));
        if (memberTypeName.startsWith("java.")) {
            memberTypeName = "ambeth." + memberTypeName;
        }
        if (baseType == RelationMember.class) {
            return bytecodeEnhancer.getEnhancedType(baseType, new RelationMemberEnhancementHint(targetType, propertyName));
        }
        return bytecodeEnhancer.getEnhancedType(baseType, new MemberEnhancementHint(targetType, propertyName, forcedElementType));
    }

    @Override
    public IntermediatePrimitiveMember getIntermediatePrimitiveMember(Class<?> entityType, String propertyName) {
        var memberNamePath = EmbeddedMember.split(propertyName);
        var currDeclaringType = entityType;
        var members = new Member[memberNamePath.length];
        for (int a = 0, size = memberNamePath.length; a < size; a++) {
            var property = propertyInfoProvider.getProperty(currDeclaringType, memberNamePath[a]);
            if (property == null) {
                return null;
            }
            members[a] = new IntermediatePrimitiveMember(currDeclaringType, entityType, property.getPropertyType(), property.getElementType(), property.getName(), property.getAnnotations());
            currDeclaringType = property.getPropertyType();
        }
        if (members.length > 1) {
            var memberPath = new Member[members.length - 1];
            System.arraycopy(members, 0, memberPath, 0, memberPath.length);
            var lastMember = (PrimitiveMember) members[memberPath.length];
            return new IntermediateEmbeddedPrimitiveMember(entityType, lastMember.getRealType(), lastMember.getElementType(), propertyName, memberPath, lastMember);
        }
        return (IntermediatePrimitiveMember) members[0];
    }

    @Override
    public IntermediateRelationMember getIntermediateRelationMember(Class<?> entityType, String propertyName) {
        var memberNamePath = EmbeddedMember.split(propertyName);
        var currDeclaringType = entityType;
        var members = new Member[memberNamePath.length];
        for (int a = 0, size = memberNamePath.length; a < size; a++) {
            var property = propertyInfoProvider.getProperty(currDeclaringType, memberNamePath[a]);
            if (property == null) {
                return null;
            }
            members[a] = new IntermediateRelationMember(currDeclaringType, entityType, property.getPropertyType(), property.getElementType(), property.getName(), property.getAnnotations());
            currDeclaringType = property.getPropertyType();
        }
        if (members.length > 1) {
            var memberPath = new Member[members.length - 1];
            System.arraycopy(members, 0, memberPath, 0, memberPath.length);
            var lastMember = members[memberPath.length];
            return new IntermediateEmbeddedRelationMember(entityType, lastMember.getRealType(), lastMember.getElementType(), propertyName, memberPath, lastMember);
        }
        return (IntermediateRelationMember) members[0];
    }

    public class TypeAndStringWeakMap<T> extends Tuple3KeyHashMap<Class<?>, String, Class<?>, Reference<T>> {
        @Override
        protected void transfer(Tuple3KeyEntry<Class<?>, String, Class<?>, Reference<T>>[] newTable) {
            var newCapacityMinus1 = newTable.length - 1;
            var table = this.table;

            for (int a = table.length; a-- > 0; ) {
                Tuple3KeyEntry<Class<?>, String, Class<?>, Reference<T>> entry = table[a], next;
                while (entry != null) {
                    next = entry.getNextEntry();

                    // only handle this entry if it has still a valid value
                    if (entry.getValue().get() != null) {
                        var i = entry.getHash() & newCapacityMinus1;
                        entry.setNextEntry(newTable[i]);
                        newTable[i] = entry;
                    }
                    entry = next;
                }
            }
        }
    }
}
