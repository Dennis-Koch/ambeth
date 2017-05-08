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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class ObjectCollector
		implements IObjectCollector, IThreadLocalObjectCollector, ICollectableControllerExtendable {
	// protected static IObjectCollector objectCollector;
	//
	// public static IObjectCollector getInstance()
	// {
	// if (objectCollector == null)
	// {
	// ObjectCollector objectCollector = new ObjectCollector();
	// ThreadLocalObjectCollector tlObjectCollector = new SensitiveThreadLocalObjectCollector();
	// objectCollector.setThreadLocalObjectCollector(tlObjectCollector);
	// ObjectCollector.objectCollector = objectCollector;
	// }
	// return objectCollector;
	// }

	protected final HashMap<Class<?>, IObjectCollectorItem> unusedListMap =
			new HashMap<>(100, 0.5f);

	protected final HashMap<Class<?>, ICollectableController> typeToControllerMap =
			new HashMap<>(16, 0.5f);

	protected final Lock typeToControllerMapLock = new ReentrantLock();

	protected ThreadLocalObjectCollector threadLocalObjectCollector;

	public void setThreadLocalObjectCollector(ThreadLocalObjectCollector threadLocalObjectCollector) {
		this.threadLocalObjectCollector = threadLocalObjectCollector;
	}

	@Override
	public void registerCollectableController(ICollectableController collectableController,
			Class<?> handledType) {
		typeToControllerMapLock.lock();
		try {
			if (typeToControllerMap.containsKey(handledType)) {
				throw new IllegalArgumentException(
						"There is already a CollectableController mapped to type " + handledType);
			}
			typeToControllerMap.put(handledType, collectableController);
		}
		finally {
			typeToControllerMapLock.unlock();
		}
		threadLocalObjectCollector.registerCollectableController(handledType, collectableController);
	}

	@Override
	public void unregisterCollectableController(ICollectableController collectableController,
			Class<?> handledType) {
		typeToControllerMapLock.lock();
		try {
			if (typeToControllerMap.get(handledType) != collectableController) {
				throw new IllegalArgumentException("CollectableController " + collectableController
						+ " is not mapped to type " + handledType);
			}
			typeToControllerMap.remove(handledType);
		}
		finally {
			typeToControllerMapLock.unlock();
		}
		threadLocalObjectCollector.unregisterCollectableController(handledType, collectableController);
	}

	@Override
	public IThreadLocalObjectCollector getCurrent() {
		return threadLocalObjectCollector.getCurrent();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T create(final Class<T> myClass) {
		return (T) getOrAllocateOcItem(myClass).getOneInstance();
	}

	@Override
	public void dispose(final Object object) {
		getOrAllocateOcItem(object.getClass()).dispose(object);
	}

	@Override
	public <T> void dispose(Class<T> type, T object) {
		getOrAllocateOcItem(type).dispose(object);
	}

	@Override
	public void cleanUp() {
		typeToControllerMapLock.lock();
		try {
			Iterator<Entry<Class<?>, IObjectCollectorItem>> iter = unusedListMap.entrySet().iterator();
			while (iter.hasNext()) {
				iter.next().getValue().cleanUp();
			}
		}
		finally {
			typeToControllerMapLock.unlock();
		}
	}

	protected IObjectCollectorItem getOrAllocateOcItem(final Class<?> type) {
		typeToControllerMapLock.lock();
		try {
			IObjectCollectorItem ocItem = unusedListMap.get(type);
			if (ocItem != null) {
				return ocItem;
			}
			try {
				return allocateOcItem(type);
			}
			catch (NoSuchMethodException e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		finally {
			typeToControllerMapLock.unlock();
		}
	}

	protected IObjectCollectorItem allocateOcItem(final Class<?> type) throws NoSuchMethodException {
		ICollectableController collectableController = typeToControllerMap.get(type);
		if (collectableController == null) {
			collectableController = new DefaultCollectableController(type, this);
		}
		IObjectCollectorItem ocItem =
				new SimpleLockingObjectCollectorItem(this, collectableController, type);
		unusedListMap.put(type, ocItem);
		return ocItem;
	}
}
