package com.koch.ambeth.util;

/*-
 * #%L
 * jambeth-util
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

import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.WeakHashMap;
import org.objectweb.asm.Type;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

public final class ReflectUtil {
    public static final Method METHOD_FINALIZE;
    private static final Field[] EMPTY_FIELDS = new Field[0];

    private static final WeakHashMap<Class<?>, Reference<ReflectEntry>> typeToMethodsMap = new WeakHashMap<>();

    private static final java.util.concurrent.locks.Lock writeLock = new ReentrantLock();

    static {
        try {
            METHOD_FINALIZE = Object.class.getDeclaredMethod("finalize");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    protected static ReflectEntry getReflectEntry(Class<?> type) {
        var entryR = typeToMethodsMap.get(type);
        if (entryR != null) {
            var entry = entryR.get();
            if (entry != null) {
                return entry;
            }
        }
        var entry = new ReflectEntry();
        typeToMethodsMap.put(type, new WeakReference<>(entry));
        return entry;
    }

    public static Constructor<?>[] getConstructors(Class<?> type) {
        writeLock.lock();
        try {
            var entry = getReflectEntry(type);
            var constructors = entry.constructors;
            if (constructors != null) {
                return constructors;
            }
            constructors = type.getConstructors();
            for (var constructor : constructors) {
                constructor.setAccessible(true);
            }
            entry.constructors = constructors;
            return constructors;
        } finally {
            writeLock.unlock();
        }
    }

    public static Method[] getMethods(Class<?> type) {
        writeLock.lock();
        try {
            var entry = getReflectEntry(type);
            var methods = entry.methods;
            if (methods != null) {
                return methods;
            }
            methods = type.getMethods();
            for (var method : methods) {
                method.setAccessible(true);
            }
            entry.methods = methods;
            return methods;
        } finally {
            writeLock.unlock();
        }
    }

    public static Field[] getDeclaredFields(Class<?> type) {
        writeLock.lock();
        try {
            var entry = getReflectEntry(type);
            var declaredFields = entry.declaredFields;
            if (declaredFields != null) {
                return declaredFields;
            }
            initDeclaredFields(type, entry);
            return entry.declaredFields;
        } finally {
            writeLock.unlock();
        }
    }

    public static Field[] getDeclaredFieldsInHierarchy(Class<?> type) {
        writeLock.lock();
        try {
            var entry = getReflectEntry(type);
            var declaredFieldsInHierarchy = entry.declaredFieldsInHierarchy;
            if (declaredFieldsInHierarchy != null) {
                return declaredFieldsInHierarchy;
            }
            initDeclaredFields(type, entry);
            return entry.declaredFieldsInHierarchy;
        } finally {
            writeLock.unlock();
        }
    }

    public static Method[] getDeclaredMethods(Class<?> type) {
        writeLock.lock();
        try {
            var entry = getReflectEntry(type);
            var declaredMethods = entry.declaredMethods;
            if (declaredMethods != null) {
                return declaredMethods;
            }
            initDeclaredMethods(type, entry);
            return entry.declaredMethods;
        } finally {
            writeLock.unlock();
        }
    }

    public static Method[] getDeclaredMethodsInHierarchy(Class<?> type) {
        writeLock.lock();
        try {
            var entry = getReflectEntry(type);
            var declaredMethodsInHierarchy = entry.declaredMethodsInHierarchy;
            if (declaredMethodsInHierarchy != null) {
                return declaredMethodsInHierarchy;
            }
            initDeclaredMethods(type, entry);
            return entry.declaredMethodsInHierarchy;
        } finally {
            writeLock.unlock();
        }
    }

    protected static void fillDeclaredMethods(Class<?> type, ArrayList<Method> declaredMethods) {
        declaredMethods.addAll(type.getDeclaredMethods());
    }

    public static Method getDeclaredMethod(boolean tryOnly, Class<?> type, Class<?> returnType, String methodName, Class<?>... parameters) {
        var currType = type;
        var params = parameters != null ? TypeUtil.getClassesToTypes(parameters) : null;
        var returnTypeAsType = returnType != null ? Type.getType(returnType) : null;
        while (currType != null) {
            var method = getDeclaredMethodIntern(currType, returnTypeAsType, methodName, params, true);
            if (method != null) {
                return method;
            }
            currType = currType.getSuperclass();
        }
        if (tryOnly) {
            return null;
        }
        throw new IllegalArgumentException(type + " does not implement '" + methodName + "(" + Arrays.toString(parameters) + ")'");
    }

    private static Method getDeclaredMethodIntern(Class<?> type, Type returnType, String methodName, Type[] parameters, boolean tryOnly) {
        var declaredMethods = getDeclaredMethods(type);
        for (int a = declaredMethods.length; a-- > 0; ) {
            var declaredMethod = declaredMethods[a];
            if (!declaredMethod.getName().equals(methodName)) {
                continue;
            }
            if (returnType != null && !Type.getType(declaredMethod.getReturnType()).equals(returnType)) {
                continue;
            }
            if (parameters == null) {
                return declaredMethod;
            }
            var parameterTypes = declaredMethod.getParameterTypes();
            if (parameterTypes.length != parameters.length) {
                continue;
            }
            var sameParams = true;
            for (int b = parameterTypes.length; b-- > 0; ) {
                if (parameters[b] != null && !Type.getType(parameterTypes[b]).equals(parameters[b])) {
                    sameParams = false;
                    break;
                }
            }
            if (sameParams) {
                return declaredMethod;
            }
        }
        if (tryOnly) {
            return null;
        }
        throw new IllegalArgumentException(type + " does not implement '" + methodName + "'");
    }

    public static Method getDeclaredMethod(boolean tryOnly, Class<?> type, Type returnType, String methodName, Type... parameters) {
        var currType = type;
        while (currType != null) {
            var method = getDeclaredMethodIntern(currType, returnType, methodName, parameters, true);
            if (method != null) {
                return method;
            }
            currType = currType.getSuperclass();
        }
        if (tryOnly) {
            return null;
        }
        throw new IllegalArgumentException(type + " does not implement '" + methodName + "'");
    }

    public static Constructor<?>[] getDeclaredConstructors(Class<?> type) {
        return type.getDeclaredConstructors();
    }

    public static Constructor<?> getDeclaredConstructor(boolean tryOnly, Class<?> type, Type[] parameters) {
        for (var declaredMethod : getDeclaredConstructors(type)) {
            var parameterTypes = declaredMethod.getParameterTypes();
            if (parameterTypes.length != parameters.length) {
                continue;
            }
            var sameParams = true;
            for (int b = parameterTypes.length; b-- > 0; ) {
                if (!Type.getType(parameterTypes[b]).equals(parameters[b])) {
                    sameParams = false;
                    break;
                }
            }
            if (sameParams) {
                return declaredMethod;
            }
        }
        if (tryOnly) {
            return null;
        }
        throw new IllegalArgumentException("No matching constructor found");
    }

    public static Field getDeclaredField(Class<?> type, String fieldName) {
        writeLock.lock();
        try {
            var entry = getReflectEntry(type);
            var nameToDeclaredFieldMap = entry.nameToDeclaredFieldMap;
            if (nameToDeclaredFieldMap != null) {
                return nameToDeclaredFieldMap.get(fieldName);
            }
            initDeclaredFields(type, entry);
            return entry.nameToDeclaredFieldMap.get(fieldName);
        } finally {
            writeLock.unlock();
        }
    }

    public static Field[] getDeclaredFieldInHierarchy(Class<?> type, String fieldName) {
        writeLock.lock();
        try {
            var entry = getReflectEntry(type);
            var nameToDeclaredFieldsInHierarchyMap = entry.nameToDeclaredFieldsInHierarchyMap;

            Field[] fields;
            if (nameToDeclaredFieldsInHierarchyMap != null) {
                fields = nameToDeclaredFieldsInHierarchyMap.get(fieldName);
            } else {
                initDeclaredFields(type, entry);
                fields = entry.nameToDeclaredFieldsInHierarchyMap.get(fieldName);
            }
            if (fields == null) {
                fields = EMPTY_FIELDS;
            }
            return fields;
        } finally {
            writeLock.unlock();
        }
    }

    protected static void initDeclaredFields(Class<?> type, ReflectEntry entry) {
        entry.nameToDeclaredFieldMap = new HashMap<>(0.5f);
        entry.nameToDeclaredFieldsInHierarchyMap = new HashMap<>(0.5f);
        var allDeclaredFields = new ArrayList<Field>();
        var declaredFields = type.getDeclaredFields();
        for (var declaredField : declaredFields) {
            if (!type.getPackageName().startsWith("java.")) {
                declaredField.setAccessible(true);
            }
            entry.nameToDeclaredFieldMap.put(declaredField.getName(), declaredField);
            allDeclaredFields.add(declaredField);
        }
        var currType = type.getSuperclass();
        if (currType != null && currType != Object.class) {
            var currDeclaredFields = getDeclaredFieldsInHierarchy(currType);
            allDeclaredFields.addAll(currDeclaredFields);
        }
        entry.declaredFields = declaredFields;
        entry.declaredFieldsInHierarchy = allDeclaredFields.toArray(Field[]::new);

        for (var declaredField : entry.declaredFieldsInHierarchy) {
            var fieldsInHierarchy = entry.nameToDeclaredFieldsInHierarchyMap.get(declaredField.getName());
            if (fieldsInHierarchy == null) {
                fieldsInHierarchy = new Field[1];
            } else {
                var newFieldsInHierarchy = new Field[fieldsInHierarchy.length + 1];
                System.arraycopy(fieldsInHierarchy, 0, newFieldsInHierarchy, 0, fieldsInHierarchy.length);
                fieldsInHierarchy = newFieldsInHierarchy;
            }
            entry.nameToDeclaredFieldsInHierarchyMap.put(declaredField.getName(), fieldsInHierarchy);
            fieldsInHierarchy[fieldsInHierarchy.length - 1] = declaredField;
        }
    }

    protected static void initDeclaredMethods(Class<?> type, ReflectEntry entry) {
        var declaredMethodsList = new ArrayList<Method>();
        var allDeclaredMethodsList = new ArrayList<Method>();
        fillDeclaredMethods(type, declaredMethodsList);
        var declaredMethods = declaredMethodsList.toArray(Method[]::new);
        for (var declaredMethod : declaredMethods) {
            if (METHOD_FINALIZE.equals(declaredMethod)) {
                continue;
            }
            if (Modifier.isNative(declaredMethod.getModifiers())) {
                continue;
            }
            if (!type.getPackageName().startsWith("java.")) {
                try {
                    declaredMethod.setAccessible(true);
                } catch (InaccessibleObjectException e) {
                    throw e;
                }
            }
            allDeclaredMethodsList.add(declaredMethod);
        }
        entry.declaredMethods = declaredMethods;

        var currType = type.getSuperclass();
        if (currType != null && currType != Object.class) {
            var currDeclaredMethods = getDeclaredMethodsInHierarchy(currType);
            allDeclaredMethodsList.addAll(currDeclaredMethods);
        }
        for (var currInterface : type.getInterfaces()) {
            var currDeclaredMethods = getDeclaredMethodsInHierarchy(currInterface);
            allDeclaredMethodsList.addAll(currDeclaredMethods);
        }
        entry.declaredMethodsInHierarchy = allDeclaredMethodsList.toArray(Method[]::new);
    }

    private ReflectUtil() {
        // Intended blank
    }

    public static class ReflectEntry {
        Constructor<?>[] constructors;

        Method[] methods;

        Field[] declaredFields;

        Field[] declaredFieldsInHierarchy;

        HashMap<String, Field> nameToDeclaredFieldMap;

        HashMap<String, Field[]> nameToDeclaredFieldsInHierarchyMap;

        Method[] declaredMethods;

        Method[] declaredMethodsInHierarchy;
    }
}
