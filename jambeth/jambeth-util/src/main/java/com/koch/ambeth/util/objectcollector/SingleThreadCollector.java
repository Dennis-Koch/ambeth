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

import com.koch.ambeth.util.IDisposable;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.collections.MapLinkedIterator;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class SingleThreadCollector extends LinkedHashMap<Class<?>, SimpleObjectCollectorItem>
		implements IObjectCollector, IThreadLocalObjectCollector, ICollectableControllerExtendable,
		IDisposable {
	protected LinkedHashMap<Class<?>, ICollectableController> typeToControllerMap =
			new LinkedHashMap<>(16, 0.5f);

	protected final IThreadLocalObjectCollector objectCollector;

	protected boolean disposed = false;

	protected int mappingVersion;

	protected long lastCleanup = System.currentTimeMillis();

	protected long cleanupInterval = 60000;

	public SingleThreadCollector(IThreadLocalObjectCollector objectCollector) {
		super(100, 0.5f);
		this.objectCollector = objectCollector;
	}

	@Override
	public void dispose() {
		clear();
		typeToControllerMap.clear();
	}

	public int getMappingVersion() {
		return mappingVersion;
	}

	public void setMappingVersion(int mappingVersion) {
		this.mappingVersion = mappingVersion;
	}

	public void setCleanupInterval(long cleanupInterval) {
		this.cleanupInterval = cleanupInterval;
	}

	@Override
	public IThreadLocalObjectCollector getCurrent() {
		return objectCollector;
	}

	public void clearCollectableControllers() {
		typeToControllerMap.clear();
	}

	@Override
	public void registerCollectableController(ICollectableController collectableController,
			Class<?> handledType) {
		if (typeToControllerMap.containsKey(handledType)) {
			throw new IllegalArgumentException(
					"There is already a CollectableController mapped to type " + handledType);
		}
		typeToControllerMap.put(handledType, collectableController);
	}

	@Override
	public void unregisterCollectableController(ICollectableController collectableController,
			Class<?> handledType) {
		if (typeToControllerMap.get(handledType) != collectableController) {
			throw new IllegalArgumentException(
					"CollectableController " + handledType + " is not mapped to type " + handledType);
		}
		typeToControllerMap.remove(handledType);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T create(final Class<T> myClass) {
		return (T) getOrAllocateOcItem(myClass).getOneInstance();
	}

	@Override
	public void dispose(final Object object) {
		long now = System.currentTimeMillis();
		if (now - lastCleanup > cleanupInterval) {
			cleanUp();
			lastCleanup = now;
		}
		getOrAllocateOcItem(object.getClass()).dispose(object);
	}

	@Override
	public <T> void dispose(Class<T> type, T object) {
		long now = System.currentTimeMillis();
		if (now - lastCleanup > cleanupInterval) {
			cleanUp();
			lastCleanup = now;
		}
		getOrAllocateOcItem(type).dispose(object);
	}

	@Override
	public void cleanUp() {
		MapLinkedIterator<Class<?>, SimpleObjectCollectorItem> iter = iterator();
		while (iter.hasNext()) {
			iter.next().getValue().cleanUp();
		}
	}

	protected IObjectCollectorItem getOrAllocateOcItem(final Class<?> type) {
		IObjectCollectorItem ocItem = get(type);
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

	protected IObjectCollectorItem allocateOcItem(final Class<?> type) throws NoSuchMethodException {
		ICollectableController collectableController = typeToControllerMap.get(type);
		if (collectableController == null) {
			collectableController = new DefaultCollectableController(type, objectCollector);
		}
		SimpleObjectCollectorItem ocItem =
				new SimpleObjectCollectorItem(objectCollector, collectableController, type);
		put(type, ocItem);
		return ocItem;
	}
}
