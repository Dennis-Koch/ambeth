package com.koch.ambeth.util.objectcollector;

import java.util.function.Supplier;

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

import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class NoOpObjectCollector
		implements IObjectCollector, IThreadLocalObjectCollector, ICollectableControllerExtendable {
	private final IConstructorTypeProvider constructorTypeProvider;

	public NoOpObjectCollector(IConstructorTypeProvider constructorTypeProvider) {
		this.constructorTypeProvider = constructorTypeProvider;
	}

	@Override
	public void registerCollectableController(ICollectableController collectableController,
			Class<?> handledType) {
		// Intended blank
	}

	@Override
	public void unregisterCollectableController(ICollectableController collectableController,
			Class<?> handledType) {
		// Intended blank
	}

	@Override
	public IThreadLocalObjectCollector getCurrent() {
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T create(final Class<T> myClass) {
		try {
			T instance = (T) constructorTypeProvider.getConstructorType(Supplier.class, myClass).get();
			if (instance instanceof IObjectCollectorAware) {
				((IObjectCollectorAware) instance).setObjectCollector(this);
			}
			if (instance instanceof ICollectable) {
				((ICollectable) instance).initInternDoNotCall();
			}
			return instance;
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e, "Error occured while instantiating type " + myClass);
		}
	}

	@Override
	public void dispose(final Object object) {
		if (object instanceof ICollectable) {
			((ICollectable) object).disposeInternDoNotCall();
		}
		if (object instanceof IObjectCollectorAware) {
			((IObjectCollectorAware) object).setObjectCollector(null);
		}
	}

	@Override
	public <T> void dispose(Class<T> type, T object) {
		if (object instanceof ICollectable) {
			((ICollectable) object).disposeInternDoNotCall();
		}
		if (object instanceof IObjectCollectorAware) {
			((IObjectCollectorAware) object).setObjectCollector(null);
		}
	}

	@Override
	public void cleanUp() {
		// Intended blank
	}
}
