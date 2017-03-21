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

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Interface for Ambeth map implementation. Overrides methods from java.util.Map to be comparable
 * with C# interface.
 *
 * @param <K> Key type
 * @param <V> Value type
 */
public interface IMap<K, V> extends Map<K, V>, Iterable<Entry<K, V>> {
	@Override
	void clear();

	@Override
	boolean containsKey(Object key);

	@Override
	ISet<Entry<K, V>> entrySet();

	void entrySet(ISet<Entry<K, V>> targetEntrySet);

	@Override
	V get(Object key);

	@Override
	boolean isEmpty();

	@Override
	ISet<K> keySet();

	void keySet(Collection<K> targetKeySet);

	IList<K> keyList();

	@Override
	V put(K key, V value);

	@Override
	V remove(Object key);

	@Override
	IList<V> values();

	K getKey(K key);

	boolean putIfNotExists(K key, V value);

	boolean removeIfValue(K key, V value);

	V[] toArray(Class<V> arrayType);
}
