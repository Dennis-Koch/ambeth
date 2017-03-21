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
import java.util.Set;

public class CleanupInvalidKeysSet<K> extends HashSet<K> {
	protected final IInvalidKeyChecker<K> invalidKeyChecker;

	protected int modCountDownCount;

	public CleanupInvalidKeysSet(IInvalidKeyChecker<K> invalidKeyChecker) {
		super();
		this.invalidKeyChecker = invalidKeyChecker;
		resetCounter();
	}

	public CleanupInvalidKeysSet(IInvalidKeyChecker<K> invalidKeyChecker,
			Collection<? extends K> sourceCollection) {
		super(sourceCollection);
		this.invalidKeyChecker = invalidKeyChecker;
		resetCounter();
	}

	public CleanupInvalidKeysSet(IInvalidKeyChecker<K> invalidKeyChecker, float loadFactor) {
		super(loadFactor);
		this.invalidKeyChecker = invalidKeyChecker;
		resetCounter();
	}

	@SuppressWarnings("rawtypes")
	public CleanupInvalidKeysSet(IInvalidKeyChecker<K> invalidKeyChecker, int initialCapacity,
			float loadFactor, Class<? extends ISetEntry> entryClass) {
		super(initialCapacity, loadFactor, entryClass);
		this.invalidKeyChecker = invalidKeyChecker;
		resetCounter();
	}

	public CleanupInvalidKeysSet(IInvalidKeyChecker<K> invalidKeyChecker, int initialCapacity,
			float loadFactor) {
		super(initialCapacity, loadFactor);
		this.invalidKeyChecker = invalidKeyChecker;
		resetCounter();
	}

	public CleanupInvalidKeysSet(IInvalidKeyChecker<K> invalidKeyChecker, int initialCapacity) {
		super(initialCapacity);
		this.invalidKeyChecker = invalidKeyChecker;
		resetCounter();
	}

	public CleanupInvalidKeysSet(IInvalidKeyChecker<K> invalidKeyChecker, K[] sourceArray) {
		super(sourceArray);
		this.invalidKeyChecker = invalidKeyChecker;
		resetCounter();
	}

	public CleanupInvalidKeysSet(IInvalidKeyChecker<K> invalidKeyChecker, Set<? extends K> map) {
		super(map);
		this.invalidKeyChecker = invalidKeyChecker;
		resetCounter();
	}

	@Override
	protected void entryAdded(ISetEntry<K> entry) {
		super.entryAdded(entry);
		setChanged();
	}

	@Override
	protected void entryRemoved(ISetEntry<K> entry) {
		super.entryRemoved(entry);
		setChanged();
	}

	protected void resetCounter() {
		modCountDownCount = threshold / 2 + 1;
	}

	protected void setChanged() {
		if (--modCountDownCount != 0) {
			return;
		}
		// size() can never be 0 after super.entryAdded(). So this statement is only true if we want the
		// "one-time" cleanup operation
		// between two transfer() calls
		modCountDownCount = 0; // set the counter to zero to suppress triggering of "setChanged()" from
														// entryRemoved()
		transfer(table);
		resetCounter();
	}

	@Override
	protected void resize(int newCapacity) {
		modCountDownCount = 0; // set the counter to zero to suppress triggering of "setChanged()" from
														// entryRemoved()
		super.resize(newCapacity);
		resetCounter();
	}

	@Override
	protected boolean isEntryValid(ISetEntry<K> entry) {
		return invalidKeyChecker.isKeyValid(entry.getKey());
	}
}
