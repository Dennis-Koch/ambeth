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

import com.koch.ambeth.ioc.exception.ExtendableException;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptyList;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.collections.SmartCopyMap;

import java.util.Map;

public class MapExtendableContainer<K, V> extends SmartCopyMap<K, Object> implements IMapExtendableContainer<K, V> {
    protected final boolean multiValue;

    protected final String message, keyMessage;

    public MapExtendableContainer(String message, String keyMessage) {
        this(message, keyMessage, false);
    }

    public MapExtendableContainer(String message, String keyMessage, boolean multiValue) {
        ParamChecker.assertParamNotNull(message, "message");
        ParamChecker.assertParamNotNull(keyMessage, "keyMessage");
        this.multiValue = multiValue;
        this.message = message;
        this.keyMessage = keyMessage;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V getExtension(K key) {
        ParamChecker.assertParamNotNull(key, "key");
        var item = get(key);
        if (item == null) {
            return null;
        }
        if (!multiValue) {
            return (V) item;
        }
        var values = (ArrayList<V>) item;
        // unregister removes empty value lists -> at least one entry
        return values.get(0);
    }

    @Override
    public ILinkedMap<K, V> getExtensions() {
        var targetMap = LinkedHashMap.<K, V>create(size());
        getExtensions(targetMap);
        return targetMap;
    }

    @Override
    public ILinkedMap<K, IList<V>> getAllExtensions() {
        var targetMap = LinkedHashMap.<K, IList<V>>create(size());
        getAllExtensions(targetMap);
        return targetMap;
    }

    @SuppressWarnings("unchecked")
    @Override
    public IList<V> getExtensions(K key) {
        ParamChecker.assertParamNotNull(key, "key");
        var item = get(key);
        if (item == null) {
            return EmptyList.getInstance();
        }
        if (!multiValue) {
            return new ArrayList<>(new Object[] { item });
        }
        return new ArrayList<>((ArrayList<V>) item);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void getExtensions(Map<K, V> targetMap) {
        if (!multiValue) {
            for (var entry : this) {
                targetMap.put(entry.getKey(), (V) entry.getValue());
            }
        } else {
            for (var entry : this) {
                // unregister removes empty value lists -> at least one entry
                targetMap.put(entry.getKey(), ((IList<V>) entry.getValue()).get(0));
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void getAllExtensions(Map<K, IList<V>> targetMap) {
        if (!multiValue) {
            for (var entry : this) {
                targetMap.put(entry.getKey(), new ArrayList<V>(new Object[] { entry.getValue() }));
            }
        } else {
            for (var entry : this) {
                // unregister removes empty value lists -> at least one entry
                targetMap.put(entry.getKey(), (IList<V>) entry.getValue());
            }
        }
    }

    @Override
    public void register(V extension, K key) {
        ParamChecker.assertParamNotNull(extension, message);
        ParamChecker.assertParamNotNull(key, keyMessage);
        var writeLock = getWriteLock();
        writeLock.lock();
        try {
            boolean putted = false;
            if (!multiValue) {
                putted = putIfNotExists(key, extension);
            } else {
                @SuppressWarnings("unchecked") ArrayList<V> values = (ArrayList<V>) get(key);
                if (values == null) {
                    values = new ArrayList<>(1);
                } else {
                    values = new ArrayList<>(values);
                }
                if (!values.contains(extension)) {
                    values.add(extension);
                    putted = true;
                    put(key, values);
                }
            }
            if (!putted) {
                throw new ExtendableException("Key '" + keyMessage + "' already added: " + key);
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void unregister(V extension, K key) {
        ParamChecker.assertParamNotNull(extension, message);
        ParamChecker.assertParamNotNull(key, keyMessage);

        try {
            var writeLock = getWriteLock();
            writeLock.lock();
            try {
                if (!multiValue) {
                    ParamChecker.assertTrue(removeIfValue(key, extension), message);
                } else {
                    @SuppressWarnings("unchecked") ArrayList<V> values = (ArrayList<V>) get(key);
                    var clone = new ArrayList<V>(values.size() - 1);
                    for (int a = 0, size = values.size(); a < size; a++) {
                        var item = values.get(a);
                        if (!extension.equals(item)) {
                            clone.add(item);
                        }
                    }
                    ParamChecker.assertTrue(clone.size() == values.size() - 1, message);
                    if (clone.isEmpty()) {
                        remove(key);
                    } else {
                        put(key, clone);
                    }
                }
            } finally {
                writeLock.unlock();
            }
        } catch (RuntimeException e) {
            throw new ExtendableException("Provided extension is not registered at key '" + key + "'. Extension: " + extension);
        }
    }
}
