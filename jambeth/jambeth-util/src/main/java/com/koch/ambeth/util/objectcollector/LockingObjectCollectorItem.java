package com.koch.ambeth.util.objectcollector;

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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockingObjectCollectorItem extends ObjectCollectorItem {
	protected final Lock lock = new ReentrantLock();

	public LockingObjectCollectorItem(IObjectCollector objectCollector,
			Class<? extends ICollectable> constructorClass) {
		super(objectCollector, constructorClass);
	}

	@Override
	protected ICollectable popLastElement() {
		lock.lock();
		try {
			return super.popLastElement();
		}
		finally {
			lock.unlock();
		}
	}

	@Override
	public void dispose(final Object object) {
		ICollectable obj = (ICollectable) object;
		obj.disposeInternDoNotCall();
		lock.lock();
		try {
			super.dispose(object);
		}
		finally {
			lock.unlock();
		}
	}

	@Override
	public void cleanUp() {
		lock.lock();
		try {
			super.cleanUp();
		}
		finally {
			lock.unlock();
		}
	}
}
