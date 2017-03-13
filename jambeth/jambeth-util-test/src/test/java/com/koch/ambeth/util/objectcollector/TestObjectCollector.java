package com.koch.ambeth.util.objectcollector;

import java.util.HashSet;
import java.util.Set;

public class TestObjectCollector extends NoOpObjectCollector {
	protected Set<Object> collectables = new HashSet<Object>();

	@Override
	public <T> T create(final Class<T> myClass) {
		T instance = super.create(myClass);

		if (instance instanceof ICollectable) {
			collectables.add(instance);
		}

		return instance;
	}

	@Override
	public void dispose(final Object object) {
		if (object instanceof ICollectable) {
			collectables.remove(object);
		}
	}

	/**
	 * For debugging purposes only! Helps tracking undisposed objects.
	 *
	 * @return Set of undisposed collectable objects.
	 */
	public Set<Object> getCollectablesInternDoNotCall() {
		return collectables;
	}
}
