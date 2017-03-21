package com.koch.ambeth.mapping;

/*-
 * #%L
 * jambeth-mapping
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

public class CompositIdentityClassKey {
	private final Object entity;

	private final Class<?> type;

	private int hash;

	public CompositIdentityClassKey(Object entity, Class<?> type) {
		this.entity = entity;
		this.type = type;

		hash = System.identityHashCode(entity) * 13;
		if (type != null) {
			hash += type.hashCode() * 23;
		}
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof CompositIdentityClassKey)) {
			return false;
		}
		CompositIdentityClassKey otherKey = (CompositIdentityClassKey) other;
		boolean ee = entity == otherKey.entity;
		boolean te = type == otherKey.type;
		return ee && te;
	}

	@Override
	public int hashCode() {
		return hash;
	}
}
