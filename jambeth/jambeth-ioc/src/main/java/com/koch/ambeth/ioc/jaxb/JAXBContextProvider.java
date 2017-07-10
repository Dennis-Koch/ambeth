package com.koch.ambeth.ioc.jaxb;

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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.koch.ambeth.util.collections.WeakHashMap;

public class JAXBContextProvider implements IJAXBContextProvider {
	protected final WeakHashMap<Class<?>[], Reference<JAXBContext>> queuedContextsMap = new WeakHashMap<Class<?>[], Reference<JAXBContext>>() {
		@Override
		protected boolean equalKeys(java.lang.Class<?>[] key,
				com.koch.ambeth.util.collections.IMapEntry<java.lang.Class<?>[], Reference<JAXBContext>> entry) {
			return Arrays.equals(key, entry.getKey());
		}

		@Override
		protected int extractHash(java.lang.Class<?>[] key) {
			return Arrays.hashCode(key);
		}
	};

	protected final Lock writeLock = new ReentrantLock();

	protected JAXBContext getExistingContext(Class<?>[] classesToBeBound) {
		Reference<JAXBContext> queuedContextsR = queuedContextsMap.get(classesToBeBound);
		if (queuedContextsR == null) {
			return null;
		}
		return queuedContextsR.get();
	}

	@Override
	public JAXBContext acquireSharedContext(Class<?>... classesToBeBound) throws JAXBException {
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try {
			JAXBContext context = getExistingContext(classesToBeBound);
			if (context != null) {
				return context;
			}
		}
		finally {
			writeLock.unlock();
		}
		JAXBContext context = JAXBContext.newInstance(classesToBeBound);

		writeLock.lock();
		try {
			JAXBContext existingContext = getExistingContext(classesToBeBound);
			if (existingContext != null) {
				return existingContext;
			}
			queuedContextsMap.put(classesToBeBound, new WeakReference<>(context));
			return context;
		}
		finally {
			writeLock.unlock();
		}
	}
}
