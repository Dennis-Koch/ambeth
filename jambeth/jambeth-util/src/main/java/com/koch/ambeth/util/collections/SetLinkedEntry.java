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

import com.koch.ambeth.util.IPrintable;
import com.koch.ambeth.util.StringBuilderUtil;

public class SetLinkedEntry<K> extends AbstractListElem<SetLinkedEntry<K>>
		implements ISetEntry<K>, IPrintable {
	protected final int hash;

	protected SetLinkedEntry<K> nextEntry;

	protected final K key;

	public SetLinkedEntry() {
		// For GenericFastList
		hash = 0;
		key = null;
	}

	public SetLinkedEntry(int hash, K key, SetLinkedEntry<K> nextEntry) {
		this.hash = hash;
		this.key = key;
		this.nextEntry = nextEntry;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public K getKey() {
		return key;
	}

	@Override
	public int getHash() {
		return hash;
	}

	@Override
	public SetLinkedEntry<K> getNextEntry() {
		return nextEntry;
	}

	public void setNextEntry(SetLinkedEntry<K> nextEntry) {
		this.nextEntry = nextEntry;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb) {
		StringBuilderUtil.appendPrintable(sb, getKey());
	}
}
