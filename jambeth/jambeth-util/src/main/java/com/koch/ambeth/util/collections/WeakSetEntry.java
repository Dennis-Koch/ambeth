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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import com.koch.ambeth.util.IPrintable;
import com.koch.ambeth.util.StringBuilderUtil;

public class WeakSetEntry<K> extends WeakReference<K> implements ISetEntry<K>, IPrintable {
	protected final int hash;

	protected WeakSetEntry<K> nextEntry;

	public WeakSetEntry(K referent, int hash, WeakSetEntry<K> nextEntry,
			ReferenceQueue<? super K> referenceQueue) {
		super(referent, referenceQueue);
		this.hash = hash;
		this.nextEntry = nextEntry;
	}

	@Override
	public boolean isValid() {
		return get() != null;
	}

	@Override
	public int getHash() {
		return hash;
	}

	@Override
	public K getKey() {
		return get();
	}

	@Override
	public WeakSetEntry<K> getNextEntry() {
		return nextEntry;
	}

	public void setNextEntry(WeakSetEntry<K> nextEntry) {
		this.nextEntry = nextEntry;
	}

	@Override
	public String toString() {
		K key = get();
		if (key != null) {
			return key.toString();
		}
		return null;
	}

	@Override
	public void toString(StringBuilder sb) {
		StringBuilderUtil.appendPrintable(sb, getKey());
	}
}
