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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
public class SmartCopySet<K> extends HashSet<K> {
	private final Lock writeLock = new ReentrantLock();

	public SmartCopySet() {
		super(0.5f);
	}

	public SmartCopySet(float loadFactor) {
		super(loadFactor);
	}

	public SmartCopySet(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	public SmartCopySet(int initialCapacity) {
		super(initialCapacity, 0.5f);
	}

	public SmartCopySet(Collection<? extends K> sourceCollection) {
		this((int) (sourceCollection.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_LOAD_FACTOR);
		addAll(sourceCollection);
	}

	public SmartCopySet(K[] sourceArray) {
		this((int) (sourceArray.length / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_LOAD_FACTOR);
		addAll(sourceArray);
	}

	public Lock getWriteLock() {
		return writeLock;
	}

	protected HashSet<K> createCopy() {
		final SmartCopySet<K> This = this;
		ISetEntry<K>[] table = this.table;
		// Copy existing data in FULLY NEW STRUCTURE
		HashSet<K> backupMap = new HashSet<K>(table.length, loadFactor) {
			@Override
			protected boolean equalKeys(K key, ISetEntry<K> entry) {
				return This.equalKeys(key, entry);
			}

			@Override
			protected int extractHash(K key) {
				return This.extractHash(key);
			}
		};
		for (int a = table.length; a-- > 0;) {
			ISetEntry<K> entry = table[a];
			while (entry != null) {
				K key = entry.getKey();
				if (key != null) {
					backupMap.add(cloneKey(key));
				}
				entry = entry.getNextEntry();
			}
		}
		return backupMap;
	}

	protected void saveCopy(HashSet<K> copy) {
		// Now the structure contains all necessary data, so we "retarget" the existing table
		table = copy.table;
		threshold = copy.threshold;
		size = copy.size;
	}

	protected K cloneKey(K key) {
		return key;
	}

	@Override
	public void clear() {
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try {
			if (size() == 0) {
				return;
			}
			HashSet<K> backupMap = createCopy();
			backupMap.clear();
			saveCopy(backupMap);
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public boolean addAll(Collection<? extends K> c) {
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try {
			HashSet<K> backupMap = createCopy();
			// Write new data in the copied structure
			if (!backupMap.addAll(c)) {
				return false;
			}
			saveCopy(backupMap);
			return true;
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public <S extends K> boolean addAll(S[] array) {
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try {
			HashSet<K> backupMap = createCopy();
			// Write new data in the copied structure
			if (!backupMap.addAll(array)) {
				return false;
			}
			saveCopy(backupMap);
			return true;
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public boolean add(K key) {
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try {
			HashSet<K> backupMap = createCopy();
			// Write new data in the copied structure
			if (!backupMap.add(key)) {
				return false;
			}
			saveCopy(backupMap);
			return true;
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public boolean remove(Object key) {
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try {
			HashSet<K> backupMap = createCopy();
			// Write new data in the copied structure
			if (!backupMap.remove(key)) {
				return false;
			}
			saveCopy(backupMap);
			return true;
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try {
			HashSet<K> backupMap = createCopy();
			// Write new data in the copied structure
			if (!backupMap.retainAll(c)) {
				return false;
			}
			saveCopy(backupMap);
			return true;
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try {
			HashSet<K> backupMap = createCopy();
			// Write new data in the copied structure
			if (!backupMap.removeAll(c)) {
				return false;
			}
			saveCopy(backupMap);
			return true;
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public K removeAndGet(K key) {
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try {
			HashSet<K> backupMap = createCopy();
			// Write new data in the copied structure
			K existingKey = backupMap.removeAndGet(key);
			saveCopy(backupMap);
			return existingKey;
		}
		finally {
			writeLock.unlock();
		}
	}
}
