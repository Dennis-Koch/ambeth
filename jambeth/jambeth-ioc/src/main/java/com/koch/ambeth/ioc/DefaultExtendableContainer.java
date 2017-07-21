package com.koch.ambeth.ioc;

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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.concurrent.locks.Lock;

import com.koch.ambeth.ioc.extendable.IExtendableContainer;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.IdentitySmartCopySet;

public class DefaultExtendableContainer<V> extends IdentitySmartCopySet<V>
		implements IExtendableContainer<V> {
	protected final String message;

	protected final Class<V> type;

	protected final V[] emptyArray;

	protected V[] array;

	@SuppressWarnings("unchecked")
	public DefaultExtendableContainer(Class<V> type, String message) {
		this.type = type;
		this.message = message;
		emptyArray = (V[]) Array.newInstance(type, 0);
	}

	@Override
	public void register(V listener) {
		ParamChecker.assertParamNotNull(listener, message);
		Lock lock = getWriteLock();
		lock.lock();
		try {
			boolean add = add(listener);
			ParamChecker.assertTrue(add, message);
			array = null;
		}
		finally {
			lock.unlock();
		}
	}

	@Override
	public void unregister(V listener) {
		ParamChecker.assertParamNotNull(listener, message);
		Lock lock = getWriteLock();
		lock.lock();
		try {
			ParamChecker.assertTrue(remove(listener), message);
			array = null;
		}
		finally {
			lock.unlock();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public V[] getExtensions() {
		int size = size();
		if (size == 0) {
			return emptyArray;
		}
		V[] array = (V[]) Array.newInstance(type, size);
		toArray(array);
		return array;
	}

	@Override
	public V[] getExtensionsShared() {
		V[] array = this.array;
		if (array != null) {
			return array;
		}
		array = getExtensions();
		this.array = array;
		return array;
	}

	@Override
	public void getExtensions(Collection<V> targetExtensionList) {
		toList(targetExtensionList);
	}
}
