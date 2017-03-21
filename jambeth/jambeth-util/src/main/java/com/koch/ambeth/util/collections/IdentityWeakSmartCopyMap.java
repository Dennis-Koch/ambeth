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

/**
 * This special kind of HashMap is intended to be used in high-performance concurrent scenarios with
 * many reads and only some single occurences of write accesses. To allow extremely high concurrency
 * there is NO lock in read access scenarios. The design pattern to synchronize the reads with the
 * indeed synchronized write accesses the internal table-structure well be REPLACED on each write.
 *
 * Because of this the existing internal object graph will NEVER be modified allowing unsynchronized
 * read access of any amount without performance loss.
 *
 * @param <K>
 * @param <V>
 */
public class IdentityWeakSmartCopyMap<K, V> extends WeakSmartCopyMap<K, V> {
	public IdentityWeakSmartCopyMap() {
		super();
	}

	public IdentityWeakSmartCopyMap(float loadFactor) {
		super(loadFactor);
	}

	public IdentityWeakSmartCopyMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	public IdentityWeakSmartCopyMap(int initialCapacity) {
		super(initialCapacity, 0.5f);
	}

	@Override
	protected boolean equalKeys(K key, IMapEntry<K, V> entry) {
		return key == entry.getKey();
	}

	@Override
	protected int extractHash(K key) {
		return System.identityHashCode(key);
	}
}
