package com.koch.ambeth.service.typeinfo;

/*-
 * #%L
 * jambeth-service
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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.repackaged.com.esotericsoftware.reflectasm.FieldAccess;
import com.koch.ambeth.util.NamedItemComparator;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.collections.SmartCopyMap;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.typeinfo.EmbeddedTypeInfoItem;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;
import com.koch.ambeth.util.typeinfo.ITypeInfo;
import com.koch.ambeth.util.typeinfo.ITypeInfoItem;
import com.koch.ambeth.util.typeinfo.ITypeInfoProvider;
import com.koch.ambeth.util.typeinfo.Transient;

public class TypeInfoProvider extends SmartCopyMap<Class<?>, TypeInfo> implements ITypeInfoProvider, IInitializingBean {
    private static final NamedItemComparator typeInfoItemComparator = new NamedItemComparator();

    private static final Pattern memberPathSplitPattern = Pattern.compile("\\.");
    protected final ThreadLocal<LinkedHashMap<Class<?>, TypeInfo>> tempTypeInfoMapTL = new ThreadLocal<>();
    protected IProperties properties;

    protected IPropertyInfoProvider propertyInfoProvider;
    @LogInstance
    private ILogger log;

    @Override
    public void afterPropertiesSet() throws Throwable {
        ParamChecker.assertNotNull(properties, "Properties");
        ParamChecker.assertNotNull(propertyInfoProvider, "propertyInfoProvider");
    }

    public void setProperties(IProperties properties) {
        this.properties = properties;
    }

    public void setPropertyInfoProvider(IPropertyInfoProvider propertyInfoProvider) {
        this.propertyInfoProvider = propertyInfoProvider;
    }

    @Override
    public ITypeInfoProvider getInstance() {
        return this;
    }

    @Override
    public ITypeInfoItem getHierarchicMember(Class<?> entityType, String hierarchicMemberName) {
        String[] memberPath = memberPathSplitPattern.split(hierarchicMemberName);
        ITypeInfoItem[] members = new ITypeInfoItem[memberPath.length];
        Class<?> currentType = entityType;
        for (int a = 0, size = memberPath.length; a < size; a++) {
            String memberName = memberPath[a];
            ITypeInfo typeInfo = getTypeInfo(currentType);
            ITypeInfoItem member = typeInfo.getMemberByName(memberName);
            if (member == null) {
                String memberNameLower = memberName.toLowerCase();
                ITypeInfoItem[] allMembers = typeInfo.getMembers();
                for (int b = allMembers.length; b-- > 0; ) {
                    if (allMembers[b].getName().toLowerCase().equals(memberNameLower)) {
                        member = allMembers[b];
                        break;
                    }
                }
                if (member == null) {
                    return null;
                }
            }
            currentType = member.getRealType();
            members[a] = member;
        }
        if (members.length == 1) {
            return members[0];
        } else if (members.length > 1) {
            ITypeInfoItem[] memberForParentPath = new ITypeInfoItem[members.length - 1];
            System.arraycopy(members, 0, memberForParentPath, 0, memberForParentPath.length);
            ITypeInfoItem lastMember = members[members.length - 1];
            return new EmbeddedTypeInfoItem(hierarchicMemberName, lastMember, memberForParentPath);
        } else {
            throw new IllegalArgumentException("Must never happen");
        }
    }

    @Override
    public ITypeInfoItem getMember(Class<?> entityType, IPropertyInfo propertyInfo) {
        return new PropertyInfoItem(propertyInfo);
    }

    @Override
    public ITypeInfoItem getMember(Field field) {
        ITypeInfoItem rii = null;
        if (!Modifier.isPrivate(field.getModifiers())) {
            ITypeInfo typeInfo = getTypeInfo(field.getDeclaringClass());
            FieldAccess fieldAccess = ((TypeInfo) typeInfo).getFieldAccess();
            if (fieldAccess != null) {
                rii = new FieldInfoItemASM(field, fieldAccess);
            }
        }
        if (rii == null) {
            rii = new FieldInfoItem(field);
        }
        return rii;
    }

    @Override
    public ITypeInfoItem getMember(String propertyName, Field field) {
        ITypeInfoItem rii = null;
        if (!Modifier.isPrivate(field.getModifiers())) {
            ITypeInfo typeInfo = getTypeInfo(field.getDeclaringClass());
            FieldAccess fieldAccess = ((TypeInfo) typeInfo).getFieldAccess();
            if (fieldAccess != null) {
                rii = new FieldInfoItemASM(field, propertyName, fieldAccess);
            }
        }
        if (rii == null) {
            rii = new FieldInfoItem(field, propertyName);
        }
        return rii;
    }

    @Override
    public ITypeInfo getTypeInfo(Class<?> type) {
        var typeInfo = get(type);
        if (typeInfo != null) {
            return typeInfo;
        }
        var tempTypeInfoMapCreated = false;
        var writeLock = getWriteLock();
        writeLock.lock();
        try {
            typeInfo = get(type);
            if (typeInfo != null) {
                // Concurrent thread might have been faster
                return typeInfo;
            }
            var tempTypeInfoMap = tempTypeInfoMapTL.get();
            if (tempTypeInfoMap != null) {
                typeInfo = tempTypeInfoMap.get(type);
                if (typeInfo != null) {
                    // Current thread is currently created cascaded TypeInfo instances
                    return typeInfo;
                }
            } else {
                tempTypeInfoMap = new LinkedHashMap<>();
                tempTypeInfoMapTL.set(tempTypeInfoMap);
                tempTypeInfoMapCreated = true;
            }
            var allFields = new ArrayList<Field>(0);
            var allProperties = new ArrayList<IPropertyInfo>();

            allFields.addAll(Arrays.asList(type.getFields()));
            for (int a = allFields.size(); a-- > 0; ) {
                var field = allFields.get(a);
                if ((Modifier.STATIC & field.getModifiers()) != 0) {
                    allFields.remove(a);
                }
            }
            allProperties.addAll(Arrays.asList(propertyInfoProvider.getProperties(type)));

            var members = new ArrayList<ITypeInfoItem>(allProperties.size() + allFields.size());
            var allMembers = new ArrayList<ITypeInfoItem>(allProperties.size() + allFields.size());

            for (int a = allFields.size(); a-- > 0; ) {
                var field = allFields.get(a);

                var fieldNameLower = field.getName().toLowerCase();
                for (var property : allProperties) {
                    if (property.getPropertyType().equals(field.getType()) && fieldNameLower.equals(property.getName().toLowerCase())) {
                        allFields.remove(a);
                        break;
                    }
                }
            }
            typeInfo = new TypeInfo(type);
            tempTypeInfoMap.put(type, typeInfo);

            for (var property : allProperties) {
                int modifiers = property.getModifiers();
                if (Modifier.isTransient(modifiers) || property.getAnnotation(Transient.class) != null) {
                    continue; // Can not handle non datamember properties
                }
                if (!Modifier.isPublic(modifiers)) {
                    continue;
                }
                allMembers.add(getMember(type, property));
                if (Modifier.isFinal(modifiers) || Modifier.isNative(modifiers)) {
                    continue;
                }
                members.add(getMember(type, property));
            }
            for (var field : allFields) {
                if (field.getAnnotation(Transient.class) != null) {
                    continue; // Can not handle non datamember fields
                }
                allMembers.add(getMember(field));
                members.add(getMember(field));
            }

            Collections.sort(members, typeInfoItemComparator);
            Collections.sort(allMembers, typeInfoItemComparator);

            typeInfo.postInit(members.toArray(ITypeInfoItem[]::new), allMembers.toArray(ITypeInfoItem[]::new));

            putAll(tempTypeInfoMap);
            return typeInfo;
        } finally {
            writeLock.unlock();
            if (tempTypeInfoMapCreated) {
                tempTypeInfoMapTL.remove();
            }
        }
    }
}
