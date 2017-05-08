package com.koch.ambeth.merge.server.service;

/*-
 * #%L
 * jambeth-merge-server
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

public class CheckForPreviousParentKey {
	protected final Class<?> entityType;

	protected final String memberName;

	public CheckForPreviousParentKey(Class<?> entityType, String memberName) {
		this.entityType = entityType;
		this.memberName = memberName;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof CheckForPreviousParentKey)) {
			return false;
		}
		CheckForPreviousParentKey other = (CheckForPreviousParentKey) obj;
		return entityType.equals(other.entityType) && memberName.equals(other.memberName);
	}

	@Override
	public int hashCode() {
		return entityType.hashCode() ^ memberName.hashCode();
	}

	@Override
	public String toString() {
		return entityType.getSimpleName() + "." + memberName;
	}
}
