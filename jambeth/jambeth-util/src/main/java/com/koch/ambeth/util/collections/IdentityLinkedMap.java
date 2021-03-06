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

import java.util.Map;

/**
 * Eine Identity-Variante der LinkedMap
 *
 * @author kochd
 *
 * @param <K> Der Typ der in der Map enthaltenen Keys
 * @param <V> Der Typ der in der Map enthaltenen Values
 */
public class IdentityLinkedMap<K, V> extends LinkedHashMap<K, V> {
	public static <K, V> IdentityLinkedMap<K, V> create(int size) {
		return create(size, DEFAULT_LOAD_FACTOR);
	}

	public static <K, V> IdentityLinkedMap<K, V> create(int size, float loadFactor) {
		return new IdentityLinkedMap<>((int) (size / loadFactor) + 1, loadFactor);
	}

	public IdentityLinkedMap() {
		super();
	}

	public IdentityLinkedMap(float loadFactor) {
		super(loadFactor);
	}

	public IdentityLinkedMap(int initialCapacity) {
		super(initialCapacity);
	}

	public IdentityLinkedMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	public IdentityLinkedMap(Map<? extends K, ? extends V> map) {
		super((int) (map.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_LOAD_FACTOR);
		putAll(map);
	}

	@Override
	protected int extractHash(K key) {
		return System.identityHashCode(key);
	}

	@Override
	protected boolean equalKeys(K key, IMapEntry<K, V> entry) {
		return key == entry.getKey();
	}

	@Override
	public ISet<K> keySet() {
		final IdentityLinkedSet<K> keySet = IdentityLinkedSet.<K>create(size());
		keySet(keySet);
		return keySet;
	}

	@Override
	public ISet<Entry<K, V>> entrySet() {
		final IdentityLinkedSet<Entry<K, V>> entrySet = IdentityLinkedSet.<Entry<K, V>>create(size());
		entrySet(entrySet);
		return entrySet;
	}
}
