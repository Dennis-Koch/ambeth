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

import java.util.concurrent.locks.Lock;

import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptyList;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IListElem;
import com.koch.ambeth.util.collections.InterfaceFastList;

public class ClassExtendableListContainer<V> extends ClassExtendableContainer<V> {
	public ClassExtendableListContainer(String message, String keyMessage) {
		super(message, keyMessage, true);
	}

	@Override
	public V getExtension(Class<?> key) {
		IList<V> extensions = getExtensions(key);
		return !extensions.isEmpty() ? extensions.get(0) : null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IList<V> getExtensions(Class<?> key) {
		if (key == null) {
			return EmptyList.<V>getInstance();
		}
		Object extension = classEntry.get(key);
		if (extension == null) {
			Lock writeLock = getWriteLock();
			writeLock.lock();
			try {
				extension = classEntry.get(key);
				if (extension == null) {
					ClassEntry<V> classEntry = copyStructure();

					classEntry.put(key, alreadyHandled);
					classEntry.typeToDefEntryMap.put(key, alreadyHandled);
					checkToWeakRegisterExistingExtensions(key, classEntry);
					this.classEntry = classEntry;

					extension = classEntry.get(key);
					if (extension == null) {
						return EmptyList.<V>getInstance();
					}
				}
			}
			finally {
				writeLock.unlock();
			}
		}
		if (extension == alreadyHandled) {
			// Already tried
			return EmptyList.<V>getInstance();
		}
		return (IList<V>) extension;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void typeToDefEntryMapChanged(ClassEntry<V> classEntry, Class<?> key) {
		Object obj = classEntry.typeToDefEntryMap.get(key);
		if (obj == null) {
			classEntry.remove(key);
			return;
		}
		if (obj == alreadyHandled) {
			classEntry.put(key, alreadyHandled);
			return;
		}
		Object existingItem = classEntry.get(key);
		ArrayList<V> list = (ArrayList<V>) (existingItem == alreadyHandled ? null : existingItem);
		if (list == null) {
			list = new ArrayList<>();
			classEntry.put(key, list);
		}
		if (obj instanceof DefEntry) {
			V extension = ((DefEntry<V>) obj).extension;
			if (!list.contains(extension)) {
				list.add(extension);
			}
			return;
		}
		IListElem<DefEntry<V>> pointer = ((InterfaceFastList<DefEntry<V>>) obj).first();
		while (pointer != null) {
			V extension = pointer.getElemValue().extension;
			if (!list.contains(extension)) {
				list.add(extension);
			}
			pointer = pointer.getNext();
		}
	}
}
