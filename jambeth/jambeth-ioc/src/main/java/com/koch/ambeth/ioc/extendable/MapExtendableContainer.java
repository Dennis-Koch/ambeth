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

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import com.koch.ambeth.ioc.exception.ExtendableException;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptyList;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.collections.SmartCopyMap;

public class MapExtendableContainer<K, V> extends SmartCopyMap<K, Object>
		implements
			IMapExtendableContainer<K, V> {
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
		Object item = get(key);
		if (item == null) {
			return null;
		}
		if (!multiValue) {
			return (V) item;
		}
		ArrayList<V> values = (ArrayList<V>) item;
		// unregister removes empty value lists -> at least one entry
		return values.get(0);
	}

	@Override
	public ILinkedMap<K, V> getExtensions() {
		LinkedHashMap<K, V> targetMap = LinkedHashMap.create(size());
		getExtensions(targetMap);
		return targetMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IList<V> getExtensions(K key) {
		ParamChecker.assertParamNotNull(key, "key");
		Object item = get(key);
		if (item == null) {
			return EmptyList.getInstance();
		}
		if (!multiValue) {
			return new ArrayList<>(new Object[] {item});
		}
		return new ArrayList<>((ArrayList<V>) item);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void getExtensions(Map<K, V> targetMap) {
		if (!multiValue) {
			for (Entry<K, Object> entry : this) {
				targetMap.put(entry.getKey(), (V) entry.getValue());
			}
		} else {
			for (Entry<K, Object> entry : this) {
				// unregister removes empty value lists -> at least one entry
				targetMap.put(entry.getKey(), ((List<V>) entry.getValue()).get(0));
			}
		}
	}

	@Override
	public void register(V extension, K key) {
		ParamChecker.assertParamNotNull(extension, message);
		ParamChecker.assertParamNotNull(key, keyMessage);
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try {
			boolean putted = false;
			if (!multiValue) {
				putted = putIfNotExists(key, extension);
			} else {
				@SuppressWarnings("unchecked")
				ArrayList<V> values = (ArrayList<V>) get(key);
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
			Lock writeLock = getWriteLock();
			writeLock.lock();
			try {
				if (!multiValue) {
					ParamChecker.assertTrue(removeIfValue(key, extension), message);
				} else {
					@SuppressWarnings("unchecked")
					ArrayList<V> values = (ArrayList<V>) get(key);
					values = new ArrayList<>(values);
					ParamChecker.assertNotNull(values, message);
					ParamChecker.assertTrue(values.remove(extension), message);
					if (values.isEmpty()) {
						remove(key);
					} else {
						put(key, values);
					}
				}
			} finally {
				writeLock.unlock();
			}
		} catch (RuntimeException e) {
			throw new ExtendableException("Provided extension is not registered at key '" + key
					+ "'. Extension: " + extension);
		}
	}
}
