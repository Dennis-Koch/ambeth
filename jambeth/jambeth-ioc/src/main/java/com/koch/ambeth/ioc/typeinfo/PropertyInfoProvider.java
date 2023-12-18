package com.koch.ambeth.ioc.typeinfo;

/*-
 * #%L
 * jambeth-ioc
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
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.repackaged.com.esotericsoftware.reflectasm.FieldAccess;
import com.koch.ambeth.repackaged.com.esotericsoftware.reflectasm.MethodAccess;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.StringConversionHelper;
import com.koch.ambeth.util.annotation.PropertyAccessor;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.collections.SmartCopyMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.regex.Pattern;

public class PropertyInfoProvider implements IPropertyInfoProvider, IInitializingBean {
    private static final Pattern getSetIsPattern = Pattern.compile("(get|set|is)([A-ZÖÄÜ].*)");
    protected final SmartCopyMap<Class<?>, PropertyInfoEntry> typeToPropertyMap = new SmartCopyMap<>();
    protected final SmartCopyMap<Class<?>, PropertyInfoEntry> typeToJavaBeansPropertyMap = new SmartCopyMap<>();
    protected final SmartCopyMap<Class<?>, PropertyInfoEntry> typeToIocPropertyMap = new SmartCopyMap<>();
    protected final SmartCopyMap<Class<?>, PropertyInfoEntry> typeToPrivatePropertyMap = new SmartCopyMap<>();
    protected IThreadLocalObjectCollector objectCollector;

    @Override
    public void afterPropertiesSet() throws Throwable {
        ParamChecker.assertNotNull(objectCollector, "ObjectCollector");
    }

    public void setObjectCollector(IThreadLocalObjectCollector objectCollector) {
        this.objectCollector = objectCollector;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPropertyInfo getProperty(Object obj, String propertyName) {
        return getProperty(obj.getClass(), propertyName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPropertyInfo getProperty(Class<?> type, String propertyName) {
        var map = getPropertyMap(type);
        var property = map.get(propertyName);
        if (property != null) {
            return property;
        }
        map = getPropertyMapJavaBeans(type);
        return map.get(propertyName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPropertyInfo[] getProperties(Object obj) {
        return getProperties(obj.getClass());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPropertyInfo[] getProperties(Class<?> type) {
        return getPropertyEntry(type, typeToPropertyMap, true, false, false).properties;
    }

    @Override
    public IPropertyInfo[] getIocProperties(Class<?> type) {
        return getPropertyEntry(type, typeToIocPropertyMap, true, true, false).properties;
    }

    @Override
    public IPropertyInfo[] getPrivateProperties(Class<?> type) {
        return getPropertyEntry(type, typeToPrivatePropertyMap, false, false, false).properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IMap<String, IPropertyInfo> getPropertyMap(Object obj) {
        return getPropertyMap(obj.getClass());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IMap<String, IPropertyInfo> getPropertyMap(Class<?> type) {
        return getPropertyEntry(type, typeToPropertyMap, true, false, false).map;
    }

    public IMap<String, IPropertyInfo> getPropertyMapJavaBeans(Class<?> type) {
        return getPropertyEntry(type, typeToJavaBeansPropertyMap, true, false, true).map;
    }

    @Override
    public IMap<String, IPropertyInfo> getIocPropertyMap(Class<?> type) {
        return getPropertyEntry(type, typeToIocPropertyMap, true, true, false).map;
    }

    @Override
    public IMap<String, IPropertyInfo> getPrivatePropertyMap(Class<?> type) {
        return getPropertyEntry(type, typeToPrivatePropertyMap, false, false, false).map;
    }

    protected PropertyInfoEntry getPropertyEntry(Class<?> type, SmartCopyMap<Class<?>, PropertyInfoEntry> map, boolean isOldIocMode, boolean isIocMode, boolean isJavaBeans) {
        ParamChecker.assertParamNotNull(type, "type");
        var propertyEntry = map.get(type);
        if (propertyEntry != null) {
            return propertyEntry;
        }
        var writeLock = map.getWriteLock();
        writeLock.lock();
        try {
            propertyEntry = map.get(type);
            if (propertyEntry != null) {
                // Concurrent thread might have been faster
                return propertyEntry;
            }

            var sortedMethods = new LinkedHashMap<String, IMap<Class<?>, IMap<String, Method>>>();
            var methods = ReflectUtil.getDeclaredMethodsInHierarchy(type);

            MethodAccess methodAccess = null;
            for (int i = methods.length; i-- > 0; ) {
                var method = methods[i];
                if (method.getDeclaringClass().equals(Object.class)) {
                    continue;
                }
                int modifiers = method.getModifiers();
                if (Modifier.isStatic(modifiers)) {
                    continue;
                }
                try {
                    String propName = getPropertyNameFor(method);
                    if (propName.isEmpty()) {
                        continue;
                    }
                    var sortedMethod = sortedMethods.get(propName);
                    if (sortedMethod == null) {
                        sortedMethod = LinkedHashMap.create(1);
                        sortedMethods.put(propName, sortedMethod);
                    }

                    var parameterTypes = method.getParameterTypes();
                    Class<?> propertyType;
                    String prefix;
                    if (parameterTypes.length == 1) {
                        propertyType = parameterTypes[0];
                        prefix = "set";
                    } else if (parameterTypes.length == 0) {
                        propertyType = method.getReturnType();
                        prefix = "get";
                    } else {
                        throw new IllegalStateException("Method is not an accessor: " + method);
                    }

                    var methodPerType = sortedMethod.get(propertyType);
                    if (methodPerType == null) {
                        methodPerType = LinkedHashMap.create(2);
                        sortedMethod.put(propertyType, methodPerType);
                    }

                    methodPerType.put(prefix, method);
                } catch (Throwable e) {
                    throw RuntimeExceptionUtil.mask(e, "Error occured while processing " + method);
                }
            }

            var filteredMethods = filterOverriddenMethods(sortedMethods, type);

            var propertyMap = new LinkedHashMap<String, IPropertyInfo>(0.5f);
            for (var propertyData : filteredMethods) {
                var propertyName = propertyData.getKey();
                var propertyMethods = propertyData.getValue();
                var getter = propertyMethods.get("get");
                var setter = propertyMethods.get("set");

                if (isIocMode) {
                    if (setter == null || (!Modifier.isPublic(setter.getModifiers()) && !setter.isAnnotationPresent(Autowired.class) && !setter.isAnnotationPresent(Property.class))) {
                        continue;
                    }
                }
                IPropertyInfo propertyInfo;
                if (methodAccess == null && !type.isInterface() && !type.isPrimitive()) {
                    methodAccess = MethodAccess.get(type);
                }
                if (methodAccess != null && isNullOrNonAbstractNonPrivateMethod(getter) && isNullOrNonAbstractNonPrivateMethod(setter)) {
                    propertyInfo = new MethodPropertyInfoASM(type, propertyName, getter, setter, objectCollector, methodAccess);
                } else {
                    propertyInfo = new MethodPropertyInfo(type, propertyName, getter, setter, objectCollector);
                }
                propertyMap.put(isJavaBeans ? propertyInfo.getNameForJavaBeans() : propertyInfo.getName(), propertyInfo);
            }

            FieldAccess fieldAccess = null;
            var fields = ReflectUtil.getDeclaredFieldsInHierarchy(type);
            for (var field : fields) {
                var modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers)) {
                    continue;
                }
                if (isOldIocMode) {
                    if (!field.isAnnotationPresent(Autowired.class) && !field.isAnnotationPresent(Property.class)) {
                        continue;
                    }
                }
                var propertyName = getPropertyNameFor(field);
                var existingProperty = propertyMap.get(propertyName);
                if (existingProperty != null && existingProperty.isWritable()) {
                    // Ignore field injection if the already resolved (method-)property is writable
                    continue;
                }
                IPropertyInfo propertyInfo;
                if (fieldAccess == null && !type.isInterface() && !type.isPrimitive()) {
                    fieldAccess = FieldAccess.get(type);
                }
                if (fieldAccess != null && (field.getModifiers() & Modifier.PRIVATE) == 0) {
                    var fieldAccessOfDeclaringType = field.getDeclaringClass() != type ? FieldAccess.get(field.getDeclaringClass()) : fieldAccess;
                    propertyInfo = new FieldPropertyInfoASM(type, propertyName, field, objectCollector, fieldAccessOfDeclaringType);
                } else {
                    propertyInfo = new FieldPropertyInfo(type, propertyName, field, objectCollector);
                }
                propertyMap.put(isJavaBeans ? propertyInfo.getNameForJavaBeans() : propertyInfo.getName(), propertyInfo);
            }
            propertyEntry = new PropertyInfoEntry(propertyMap);
            map.put(type, propertyEntry);
            return propertyEntry;
        } finally {
            writeLock.unlock();
        }
    }

    protected boolean isNullOrNonAbstractNonPrivateMethod(Method method) {
        if (method == null) {
            return true;
        }
        return !Modifier.isAbstract(method.getModifiers()) && !Modifier.isPrivate(method.getModifiers());
    }

    protected IMap<String, IMap<String, Method>> filterOverriddenMethods(IMap<String, IMap<Class<?>, IMap<String, Method>>> sortedMethods, Class<?> entityType) {
        var filteredMethods = LinkedHashMap.<String, IMap<String, Method>>create(sortedMethods.size());

        for (var entry : sortedMethods) {
            var propName = entry.getKey();
            var typedHashMap = entry.getValue();

            if (typedHashMap.size() == 1) {
                var iter = typedHashMap.values().iterator();
                var accessorMap = iter.next();
                filteredMethods.put(propName, accessorMap);
                continue;
            }

            Class<?> mostConcreteType = null;
            Class<?> mostConcreteGetterType = null;
            Class<?> mostConcreteSetterType = null;
            for (var typedEntries : typedHashMap) {
                var currentType = typedEntries.getKey();
                var accessorMap = typedEntries.getValue();
                if (accessorMap.size() != 2) {
                    if (accessorMap.get("get") != null) {
                        mostConcreteGetterType = resolveMostConcreteType(mostConcreteGetterType, currentType);
                    } else {
                        mostConcreteSetterType = resolveMostConcreteType(mostConcreteSetterType, currentType);
                    }
                    continue;
                }
                mostConcreteType = resolveMostConcreteType(mostConcreteType, currentType);
            }
            if (mostConcreteType != null) {
                var accessorMap = typedHashMap.get(mostConcreteType);
                filteredMethods.put(propName, accessorMap);
            } else if (mostConcreteGetterType != null) {
                var accessorMap = typedHashMap.get(mostConcreteGetterType);
                filteredMethods.put(propName, accessorMap);
            } else if (mostConcreteSetterType != null) {
                var accessorMap = typedHashMap.get(mostConcreteSetterType);
                filteredMethods.put(propName, accessorMap);
            }
        }

        return filteredMethods;
    }

    protected Class<?> resolveMostConcreteType(Class<?> mostConcreteType, Class<?> currentType) {
        if (mostConcreteType == null || mostConcreteType.isAssignableFrom(currentType)) {
            return currentType;
        }
        return mostConcreteType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPropertyNameFor(Method method) {
        var propertyAccessor = method.getAnnotation(PropertyAccessor.class);
        if (propertyAccessor != null) {
            return propertyAccessor.value();
        }
        var matcher = getSetIsPattern.matcher(method.getName());
        if (!matcher.matches()) {
            return "";
        }
        var paramLength = method.getParameterTypes().length;
        var getSetIs = matcher.group(1);
        if ("get".equals(getSetIs) && (0 != paramLength || void.class.equals(method.getReturnType()))) {
            return "";
        } else if ("set".equals(getSetIs) && 1 != paramLength) {
            return "";
        } else if ("is".equals(getSetIs) && (0 != paramLength || void.class.equals(method.getReturnType()))) {
            return "";
        }
        var name = matcher.group(2);
        return StringConversionHelper.upperCaseFirst(objectCollector, name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPropertyNameFor(Field field) {
        return StringConversionHelper.upperCaseFirst(objectCollector, field.getName());
    }
}
