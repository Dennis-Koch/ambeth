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

import com.koch.ambeth.util.objectcollector.IObjectCollector;

import java.lang.reflect.Array;

public final class Arrays {
    @SuppressWarnings("unchecked")
    public static final <T> T[] removeElementAt(T[] array, int index) {
        Object arr = Array.newInstance(array.getClass().getComponentType(), array.length - 1);
        System.arraycopy(array, 0, arr, 0, index);
        System.arraycopy(array, index + 1, arr, index, array.length - index - 1);
        return (T[]) arr;
    }

    public static final String toString(IObjectCollector objectCollector, Object[] array) {
        if (array == null) {
            return "null";
        }
        int iMax = array.length - 1;
        if (iMax == -1) {
            return "[]";
        }
        StringBuilder sb = objectCollector.create(StringBuilder.class);
        try {
            sb.append('[');
            for (int i = 0; ; i++) {
                Object item = array[i];
                StringBuilderUtil.appendPrintable(sb, item);
                if (i == iMax) {
                    return sb.append(']').toString();
                }
                sb.append(", ");
            }
        } finally {
            objectCollector.dispose(sb);
        }
    }

    public static final void toString(StringBuilder sb, Object[] array) {
        if (array == null) {
            sb.append("null");
            return;
        }
        int iMax = array.length - 1;
        if (iMax == -1) {
            sb.append("[]");
            return;
        }
        sb.append('[');
        for (int i = 0; ; i++) {
            Object item = array[i];
            StringBuilderUtil.appendPrintable(sb, item);
            if (i == iMax) {
                sb.append(']');
                return;
            }
            sb.append(", ");
        }
    }

    public static PreparedArrayGet prepareGet(Object array) {
        var componentType = array.getClass().getComponentType();
        if (byte.class.equals(componentType)) {
            var castArray = (byte[]) array;
            return index -> Byte.valueOf(castArray[index]);
        }
        if (char.class.equals(componentType)) {
            var castArray = (char[]) array;
            return index -> Character.valueOf(castArray[index]);
        }
        if (boolean.class.equals(componentType)) {
            var castArray = (boolean[]) array;
            return index -> Boolean.valueOf(castArray[index]);
        }
        if (short.class.equals(componentType)) {
            var castArray = (short[]) array;
            return index -> Short.valueOf(castArray[index]);
        }
        if (int.class.equals(componentType)) {
            var castArray = (int[]) array;
            return index -> Integer.valueOf(castArray[index]);
        }
        if (float.class.equals(componentType)) {
            var castArray = (float[]) array;
            return index -> Float.valueOf(castArray[index]);
        }
        if (long.class.equals(componentType)) {
            var castArray = (long[]) array;
            return index -> Long.valueOf(castArray[index]);
        }
        if (double.class.equals(componentType)) {
            var castArray = (double[]) array;
            return index -> Double.valueOf(castArray[index]);
        }
        var castArray = (Object[]) array;
        return index -> castArray[index];
    }

    public static PreparedArraySet prepareSet(Object array) {
        var componentType = array.getClass().getComponentType();
        if (byte.class.equals(componentType)) {
            var castArray = (byte[]) array;
            return (index, value) -> castArray[index] = ((Number) value).byteValue();
        }
        if (char.class.equals(componentType)) {
            var castArray = (char[]) array;
            return (index, value) -> castArray[index] = ((Character) value).charValue();
        }
        if (boolean.class.equals(componentType)) {
            var castArray = (boolean[]) array;
            return (index, value) -> castArray[index] = ((Boolean) value).booleanValue();
        }
        if (short.class.equals(componentType)) {
            var castArray = (short[]) array;
            return (index, value) -> castArray[index] = ((Number) value).shortValue();
        }
        if (int.class.equals(componentType)) {
            var castArray = (int[]) array;
            return (index, value) -> castArray[index] = ((Number) value).intValue();
        }
        if (float.class.equals(componentType)) {
            var castArray = (float[]) array;
            return (index, value) -> castArray[index] = ((Number) value).floatValue();
        }
        if (long.class.equals(componentType)) {
            var castArray = (long[]) array;
            return (index, value) -> castArray[index] = ((Number) value).longValue();
        }
        if (double.class.equals(componentType)) {
            var castArray = (double[]) array;
            return (index, value) -> castArray[index] = ((Number) value).doubleValue();
        }
        var castArray = (Object[]) array;
        return (index, value) -> castArray[index] = value;
    }

    private Arrays() {
        // Intended blank
    }

    public interface PreparedArraySet {
        void set(int index, Object value);
    }

    public interface PreparedArrayGet {
        Object get(int index);
    }
}
