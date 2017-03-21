package com.koch.ambeth.cache;

/*-
 * #%L
 * jambeth-cache
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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

public class CacheSoftReference<V> extends SoftReference<V>
		implements ICacheReference, IParentCacheValueHardRef {
	@SuppressWarnings("unused")
	private Object parentCacheValueHardRef;

	private final Class<?> entityType;

	private final byte idIndex;

	private final Object id;

	public CacheSoftReference(V referent, ReferenceQueue<V> queue, Class<?> entityType, byte idIndex,
			Object id) {
		super(referent, queue);
		this.entityType = entityType;
		this.idIndex = idIndex;
		this.id = id;
	}

	@Override
	public Class<?> getEntityType() {
		return entityType;
	}

	@Override
	public byte getIdIndex() {
		return idIndex;
	}

	@Override
	public Object getId() {
		return id;
	}

	@Override
	public void setParentCacheValueHardRef(Object parentCacheValueHardRef) {
		this.parentCacheValueHardRef = parentCacheValueHardRef;
	}
}
