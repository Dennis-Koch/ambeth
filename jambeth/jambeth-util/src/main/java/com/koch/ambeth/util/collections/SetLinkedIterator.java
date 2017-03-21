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

public class SetLinkedIterator<K> extends AbstractIterator<K> {
	protected SetLinkedEntry<K> currPointer, lastPointer;

	private final AbstractLinkedSet<K> hashSet;

	public SetLinkedIterator(final AbstractLinkedSet<K> hashSet, boolean removeAllowed) {
		super(removeAllowed);
		this.hashSet = hashSet;
		currPointer = hashSet.fastIterationList.getFirstElem();
		lastPointer = null;
	}

	@Override
	public final boolean hasNext() {
		return currPointer != null;
	}

	@Override
	public final K next() {
		final K elem = currPointer.key;
		lastPointer = currPointer;
		currPointer = currPointer.next;
		return elem;
	}

	@Override
	public void remove() {
		if (!removeAllowed) {
			throw new UnsupportedOperationException();
		}
		hashSet.remove(lastPointer.key);
		lastPointer = null;
	}
}
