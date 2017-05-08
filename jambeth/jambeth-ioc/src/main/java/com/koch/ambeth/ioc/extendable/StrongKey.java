package com.koch.ambeth.ioc.extendable;

/*-
 * #%L
 * jambeth-ioc
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

public class StrongKey<V> {
	protected final V extension;

	protected final Class<?> strongType;

	public StrongKey(V extension, Class<?> strongType) {
		this.extension = extension;
		this.strongType = strongType;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof StrongKey)) {
			return false;
		}
		StrongKey<?> other = (StrongKey<?>) obj;
		return extension == other.extension && strongType.equals(other.strongType);
	}

	@Override
	public int hashCode() {
		return extension.hashCode() ^ strongType.hashCode();
	}

	@Override
	public String toString() {
		return "(Key: " + strongType.getName() + " Extension: " + extension.toString() + ")";
	}
}
