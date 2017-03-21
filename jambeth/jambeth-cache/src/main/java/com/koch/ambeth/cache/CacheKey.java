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

public class CacheKey {
	protected Class<?> entityType;

	protected byte idIndex;

	protected Object id;

	public Class<?> getEntityType() {
		return entityType;
	}

	public void setEntityType(Class<?> entityType) {
		this.entityType = entityType;
	}

	public byte getIdIndex() {
		return idIndex;
	}

	public void setIdIndex(byte idIndex) {
		this.idIndex = idIndex;
	}

	public Object getId() {
		return id;
	}

	public void setId(Object id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		return entityType.hashCode() ^ id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof CacheKey)) {
			return false;
		}
		CacheKey other = (CacheKey) obj;
		return id.equals(other.getId()) && entityType.equals(other.getEntityType())
				&& idIndex == other.getIdIndex();
	}

	@Override
	public String toString() {
		return "CacheKey: " + entityType.getName() + "(" + idIndex + "," + id + ")";
	}
}
