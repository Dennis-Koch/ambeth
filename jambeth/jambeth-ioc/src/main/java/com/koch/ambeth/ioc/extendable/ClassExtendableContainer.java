package com.koch.ambeth.ioc.extendable;

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

import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IdentityHashMap;
import com.koch.ambeth.util.collections.InterfaceFastList;

import java.util.concurrent.locks.Lock;

public class ClassExtendableContainer<V> extends MapExtendableContainer<Class<?>, V> {
    public static final int NO_VALID_DISTANCE = -1;
    protected static final Object alreadyHandled = new Object();

    public static int getDistanceForType(Class<?> existingRequestedType, Class<?> type) {
        // If a converter handles A (strong registration)
        // It implicitily handles X extends A (weak registration)
        if (existingRequestedType == null || !type.isAssignableFrom(existingRequestedType)) {
            return NO_VALID_DISTANCE;
        }
        if (existingRequestedType.equals(type)) {
            // Type matched exactly - 'strong' registration
            return 0;
        }
        if (type.equals(Object.class)) {
            return Integer.MAX_VALUE;
        }
        if (existingRequestedType.isArray() && type.isArray()) {
            // if both types are an array their distance is measured by the distance of their component
            // type
            return getDistanceForType(existingRequestedType.getComponentType(), type.getComponentType());
        }
        int bestDistance = Integer.MAX_VALUE;
        Class<?>[] currInterfaces = existingRequestedType.getInterfaces();

        for (Class<?> currInterface : currInterfaces) {
            int distance = getDistanceForType(currInterface, type);
            if (distance < 0) {
                continue;
            }
            distance += 10000;
            if (distance < bestDistance) {
                bestDistance = distance;
            }
        }
        Class<?> baseType = existingRequestedType.getSuperclass();
        if (baseType == null) {
            baseType = Object.class;
        }
        int distance = getDistanceForType(baseType, type);
        if (distance >= 0) {
            distance++;
            if (distance < bestDistance) {
                bestDistance = distance;
            }
        }
        if (bestDistance == Integer.MAX_VALUE) {
            throw new IllegalStateException("Must never happen");
        }
        return bestDistance;
    }

    protected volatile ClassEntry<V> classEntry = new ClassEntry<>();

    public ClassExtendableContainer(String message, String keyMessage) {
        this(message, keyMessage, false);
    }

    public ClassExtendableContainer(String message, String keyMessage, boolean multiValue) {
        super(message, keyMessage, multiValue);
    }

    @Override
    public void clear() {
        var writeLock = getWriteLock();
        writeLock.lock();
        try {
            super.clear();
            clearWeakCache();
        } finally {
            writeLock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public void clearWeakCache() {
        var writeLock = getWriteLock();
        writeLock.lock();
        try {
            ClassExtendableContainer<V> tempCC = new ClassExtendableContainer<>("", "");
            for (Entry<Class<?>, Object> entry : this) {
                tempCC.register((V) entry.getValue(), entry.getKey());
            }
            this.classEntry = tempCC.classEntry;
        } finally {
            writeLock.unlock();
        }
    }

    public V getExtensionHardKey(Class<?> key) {
        return super.getExtension(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V getExtension(Class<?> key) {
        if (key == null) {
            return null;
        }
        var extension = classEntry.get(key);
        if (extension == null) {
            Lock writeLock = getWriteLock();
            writeLock.lock();
            try {
                extension = classEntry.get(key);
                if (extension == null) {
                    ClassEntry<V> classEntry = copyStructure();

                    classEntry.put(key, alreadyHandled);
                    classEntry.typeToDefEntryMap.put(key, alreadyHandled);
                    checkToWeakRegisterExistingExtensions(key, classEntry);
                    this.classEntry = classEntry;

                    extension = classEntry.get(key);
                    if (extension == null) {
                        return null;
                    }
                }
            } finally {
                writeLock.unlock();
            }
        }
        if (extension == alreadyHandled) {
            // Already tried
            return null;
        }
        return (V) extension;
    }

    @SuppressWarnings("unchecked")
    protected ClassEntry<V> copyStructure() {
        var newClassEntry = new ClassEntry<V>();
        var newTypeToDefEntryMap = newClassEntry.typeToDefEntryMap;
        var newDefinitionReverseMap = newClassEntry.definitionReverseMap;
        var originalToCopyMap = new IdentityHashMap<DefEntry<V>, DefEntry<V>>();
        {
            for (var entry : classEntry.typeToDefEntryMap) {
                var key = entry.getKey();
                var value = entry.getValue();

                if (value == alreadyHandled) {
                    newTypeToDefEntryMap.put(key, alreadyHandled);
                } else {
                    var list = (InterfaceFastList<DefEntry<V>>) value;

                    var newList = new InterfaceFastList<DefEntry<V>>();

                    var pointer = list.first();
                    while (pointer != null) {
                        var defEntry = pointer.getElemValue();
                        var newDefEntry = new DefEntry<>(defEntry.extension, defEntry.type, defEntry.distance);
                        originalToCopyMap.put(defEntry, newDefEntry);

                        newList.pushLast(newDefEntry);
                        pointer = pointer.getNext();
                    }
                    newTypeToDefEntryMap.put(key, newList);
                }
                typeToDefEntryMapChanged(newClassEntry, key);
            }
        }
        for (var entry : classEntry.definitionReverseMap) {
            var defEntries = entry.getValue();
            var newDefEntries = new ArrayList<DefEntry<V>>(defEntries.size());

            for (int a = 0, size = defEntries.size(); a < size; a++) {
                var newDefEntry = originalToCopyMap.get(defEntries.get(a));
                if (newDefEntry == null) {
                    throw new IllegalStateException("Must never happen");
                }
                newDefEntries.add(newDefEntry);
            }
            newDefinitionReverseMap.put(entry.getKey(), newDefEntries);
        }
        return newClassEntry;
    }

    protected boolean checkToWeakRegisterExistingExtensions(Class<?> type, ClassEntry<V> classEntry) {
        var changesHappened = false;
        for (var entry : classEntry.definitionReverseMap) {
            var strongKey = entry.getKey();
            var registeredStrongType = strongKey.strongType;
            var distance = getDistanceForType(type, registeredStrongType);
            if (distance == NO_VALID_DISTANCE) {
                continue;
            }
            var defEntries = entry.getValue();
            for (int a = 0, size = defEntries.size(); a < size; a++) {
                var defEntry = defEntries.get(a);
                changesHappened |= appendRegistration(registeredStrongType, type, defEntry.extension, distance, classEntry);
            }
        }
        return changesHappened;
    }

    protected boolean checkToWeakRegisterExistingTypes(Class<?> type, V extension, ClassEntry<V> classEntry) {
        var changesHappened = false;
        for (var entry : classEntry.typeToDefEntryMap) {
            var existingRequestedType = entry.getKey();
            var priorityForExistingRequestedType = getDistanceForType(existingRequestedType, type);
            if (priorityForExistingRequestedType == NO_VALID_DISTANCE) {
                continue;
            }
            changesHappened |= appendRegistration(type, existingRequestedType, extension, priorityForExistingRequestedType, classEntry);
        }
        return changesHappened;
    }

    @Override
    public void register(V extension, Class<?> key) {
        var writeLock = getWriteLock();
        writeLock.lock();
        try {
            super.register(extension, key);

            var classEntry = copyStructure();
            appendRegistration(key, key, extension, 0, classEntry);
            checkToWeakRegisterExistingTypes(key, extension, classEntry);
            checkToWeakRegisterExistingExtensions(key, classEntry);
            this.classEntry = classEntry;
        } finally {
            writeLock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void unregister(V extension, Class<?> key) {
        ParamChecker.assertParamNotNull(extension, "extension");
        ParamChecker.assertParamNotNull(key, "key");

        var writeLock = getWriteLock();
        writeLock.lock();
        try {
            super.unregister(extension, key);

            var classEntry = copyStructure();
            var definitionReverseMap = classEntry.definitionReverseMap;
            var weakEntriesOfStrongType = definitionReverseMap.remove(new StrongKey<>(extension, key));
            if (weakEntriesOfStrongType == null) {
                return;
            }
            var typeToDefEntryMap = classEntry.typeToDefEntryMap;
            for (int a = weakEntriesOfStrongType.size(); a-- > 0; ) {
                var defEntry = weakEntriesOfStrongType.get(a);
                var registeredType = defEntry.type;

                var value = typeToDefEntryMap.get(registeredType);
                var list = (InterfaceFastList<DefEntry<V>>) value;
                list.remove(defEntry);
                if (list.isEmpty()) {
                    typeToDefEntryMap.remove(registeredType);
                }
                typeToDefEntryMapChanged(classEntry, registeredType);
            }
            this.classEntry = classEntry;
        } finally {
            writeLock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    protected void typeToDefEntryMapChanged(ClassEntry<V> classEntry, Class<?> key) {
        var obj = classEntry.typeToDefEntryMap.get(key);
        if (obj == null) {
            classEntry.remove(key);
            return;
        }
        if (obj == alreadyHandled) {
            classEntry.put(key, alreadyHandled);
            return;
        }
        if (obj instanceof DefEntry) {
            classEntry.put(key, ((DefEntry<V>) obj).extension);
            return;
        }
        var firstDefEntry = ((InterfaceFastList<DefEntry<V>>) obj).first().getElemValue();
        classEntry.put(key, firstDefEntry.extension);
    }

    @SuppressWarnings("unchecked")
    protected boolean appendRegistration(Class<?> strongTypeKey, Class<?> key, V extension, int distance, ClassEntry<V> classEntry) {
        var typeToDefEntryMap = classEntry.typeToDefEntryMap;
        var fastList = typeToDefEntryMap.get(key);
        if (fastList != null && fastList != alreadyHandled) {
            var pointer = ((InterfaceFastList<DefEntry<V>>) fastList).first();
            while (pointer != null) {
                var existingDefEntry = pointer.getElemValue();
                if (existingDefEntry.extension == extension && existingDefEntry.distance == distance) {
                    // DefEntry already exists with same distance
                    return false;
                }
                pointer = pointer.getNext();
            }
        }
        if (fastList == null || fastList == alreadyHandled) {
            fastList = new InterfaceFastList<DefEntry<V>>();
            typeToDefEntryMap.put(key, fastList);
        }
        var defEntry = new DefEntry<>(extension, key, distance);

        var definitionReverseMap = classEntry.definitionReverseMap;
        var strongKey = new StrongKey<>(extension, strongTypeKey);
        var typeEntries = definitionReverseMap.get(strongKey);
        if (typeEntries == null) {
            typeEntries = new ArrayList<>();
            definitionReverseMap.put(strongKey, typeEntries);
        }
        typeEntries.add(defEntry);

        InterfaceFastList.insertOrdered((InterfaceFastList<DefEntry<V>>) fastList, defEntry);
        typeToDefEntryMapChanged(classEntry, key);
        return true;
    }
}
