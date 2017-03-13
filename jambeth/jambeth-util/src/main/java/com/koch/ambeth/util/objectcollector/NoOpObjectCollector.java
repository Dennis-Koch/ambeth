package com.koch.ambeth.util.objectcollector;

import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class NoOpObjectCollector
		implements IObjectCollector, IThreadLocalObjectCollector, ICollectableControllerExtendable {
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

	@Override
	public <T> T create(final Class<T> myClass) {
		try {
			T instance = myClass.newInstance();
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
