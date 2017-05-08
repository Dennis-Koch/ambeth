package com.koch.ambeth.merge.copy;

/*-
 * #%L
 * jambeth-merge
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

import com.koch.ambeth.util.collections.IdentityHashMap;

/**
 * Encapsulates the internal state of an ObjectCopier operation
 */
public class ObjectCopierState implements IObjectCopierState {
	protected final IdentityHashMap<Object, Object> objectToCloneDict =
			new IdentityHashMap<>();

	protected final ObjectCopier objectCopier;

	public ObjectCopierState(ObjectCopier objectCopier) {
		this.objectCopier = objectCopier;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public <T> void addClone(T source, T clone) {
		if (!objectToCloneDict.putIfNotExists(source, clone)) {
			throw new IllegalStateException(
					"Object '" + source + "' has already been copied before in this recursive operation");
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public <T> void deepCloneProperties(T source, T clone) {
		objectCopier.deepCloneProperties(source, clone, this);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public <T> T clone(T source) {
		return objectCopier.cloneRecursive(source, this);
	}

	/**
	 * Called to prepare this instance for clean reusage
	 */
	public void clear() {
		objectToCloneDict.clear();
	}
}
