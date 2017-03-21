package com.koch.ambeth.util.objectcollector;

/*-
 * #%L
 * jambeth-util-test
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
