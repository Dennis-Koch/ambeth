package com.koch.ambeth.util.collections;

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

import java.lang.ref.Reference;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * This special kind of HashMap is intended to be used in high-performance concurrent scenarios with
 * many reads and only some single occurences of write accesses. To allow extremely high concurrency
 * there is NO lock in read access scenarios. The design pattern to synchronize the reads with the
 * indeed synchronized write accesses the internal table-structure well be REPLACED on each write.
 * <p>
 * Because of this the existing internal object graph will NEVER be modified allowing unsynchronized
 * read access of any amount without performance loss.
 *
 * @param <K>
 * @param <V>
 */
public class SmartCopyMap<K, V> extends HashMap<K, V> {
    private final Lock writeLock = new ReentrantLock();

    private boolean autoCleanupNullValue;

    public SmartCopyMap() {
        super();
    }

    public SmartCopyMap(float loadFactor) {
        super(loadFactor);
    }

    public SmartCopyMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public SmartCopyMap(int initialCapacity) {
        super(initialCapacity);
    }

    public boolean isAutoCleanupNullValue() {
        return autoCleanupNullValue;
    }

    public void setAutoCleanupNullValue(boolean autoCleanupNullValue) {
        this.autoCleanupNullValue = autoCleanupNullValue;
    }

    public Lock getWriteLock() {
        return writeLock;
    }

    protected HashMap<K, V> createCopy() {
        var This = this;
        // Copy existing data in FULLY NEW STRUCTURE
        var table = this.table;
        var backupMap = new HashMap<K, V>(table.length, loadFactor) {
            @Override
            protected boolean equalKeys(K key, IMapEntry<K, V> entry) {
                return This.equalKeys(key, entry);
            }

            @Override
            protected int extractHash(K key) {
                return This.extractHash(key);
            }
        };
        if (autoCleanupNullValue) {
            for (int a = table.length; a-- > 0; ) {
                var entry = table[a];
                while (entry != null) {
                    var key = entry.getKey();
                    if (key != null) {
                        var value = entry.getValue();
                        var valueAsRef = (Reference<?>) value;
                        if (valueAsRef.get() != null) {
                            // Only copy the entry if the value content is still valid
                            backupMap.put(cloneKey(key), cloneValue(value));
                        }
                    }
                    entry = entry.getNextEntry();
                }
            }
        } else {
            for (int a = table.length; a-- > 0; ) {
                var entry = table[a];
                while (entry != null) {
                    var key = entry.getKey();
                    if (key != null) {
                        var value = entry.getValue();
                        backupMap.put(cloneKey(key), cloneValue(value));
                    }
                    entry = entry.getNextEntry();
                }
            }
        }
        return backupMap;
    }

    protected void saveCopy(HashMap<K, V> copy) {
        // Now the structure contains all necessary data, so we "retarget" the existing table
        table = copy.table;
        threshold = copy.threshold;
        size = copy.size;
    }

    protected K cloneKey(K key) {
        return key;
    }

    protected V cloneValue(V value) {
        return value;
    }

    @Override
    public void clear() {
        var writeLock = getWriteLock();
        writeLock.lock();
        try {
            if (size() == 0) {
                return;
            }
            var backupMap = createCopy();
            backupMap.clear();
            saveCopy(backupMap);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V put(K key, V value) {
        var writeLock = getWriteLock();
        writeLock.lock();
        try {
            var backupMap = createCopy();
            // Write new data in the copied structure
            var existingValue = backupMap.put(key, value);
            saveCopy(backupMap);
            return existingValue;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        var writeLock = getWriteLock();
        writeLock.lock();
        try {
            var backupMap = createCopy();
            // Write new data in the copied structure
            backupMap.putAll(map);
            saveCopy(backupMap);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean putIfNotExists(K key, V value) {
        var entry = getEntry(key);
        if (entry != null) {
            return false;
        }
        var writeLock = getWriteLock();
        writeLock.lock();
        try {
            entry = getEntry(key);
            if (entry != null) {
                return false;
            }
            var backupMap = createCopy();
            // Write new data in the copied structure
            if (!backupMap.putIfNotExists(key, value)) {
                throw new IllegalStateException("Must never happen has the entry was not existing before while we kept the semaphore");
            }
            saveCopy(backupMap);
            return true;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        var value = get(key);
        if (value != null) {
            return value;
        }
        var writeLock = getWriteLock();
        writeLock.lock();
        try {
            value = get(key);
            if (value != null) {
                return value;
            }
            var backupMap = createCopy();
            // Write new data in the copied structure
            value = backupMap.computeIfAbsent(key, mappingFunction);
            saveCopy(backupMap);
            return value;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        var value = get(key);
        if (value == null) {
            return value;
        }
        var writeLock = getWriteLock();
        writeLock.lock();
        try {
            value = get(key);
            if (value == null) {
                return value;
            }
            var backupMap = createCopy();
            // Write new data in the copied structure
            value = backupMap.computeIfPresent(key, remappingFunction);
            saveCopy(backupMap);
            return value;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        var writeLock = getWriteLock();
        writeLock.lock();
        try {
            return super.compute(key, remappingFunction);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V remove(Object key) {
        var writeLock = getWriteLock();
        writeLock.lock();
        try {
            var backupMap = createCopy();
            // Write new data in the copied structure
            var existingValue = backupMap.remove(key);
            saveCopy(backupMap);
            return existingValue;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean removeIfValue(K key, V value) {
        var writeLock = getWriteLock();
        writeLock.lock();
        try {
            var backupMap = createCopy();
            // Write new data in the copied structure
            if (!backupMap.removeIfValue(key, value)) {
                return false;
            }
            saveCopy(backupMap);
            return true;
        } finally {
            writeLock.unlock();
        }
    }
}
