package com.koch.ambeth.util.objectcollector;

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
