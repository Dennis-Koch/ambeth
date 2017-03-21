package com.koch.ambeth.query.jdbc;

/*-
 * #%L
 * jambeth-query-jdbc
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

import com.koch.ambeth.query.IQueryKey;
import com.koch.ambeth.util.ParamChecker;

public class StringQueryKey implements IQueryKey {
	protected final Class<?> entityType;

	protected final String value;

	public StringQueryKey(Class<?> entityType, String value) {
		ParamChecker.assertParamNotNull(entityType, "entityType");
		ParamChecker.assertParamNotNull(value, "value");
		this.entityType = entityType;
		this.value = value;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof StringQueryKey)) {
			return false;
		}
		StringQueryKey other = (StringQueryKey) obj;

		return entityType.equals(other.entityType) && value.equals(other.value);
	}

	@Override
	public int hashCode() {
		return entityType.hashCode() ^ value.hashCode();
	}

	@Override
	public String toString() {
		return entityType.getName() + value;
	}
}
