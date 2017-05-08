package com.koch.ambeth.persistence;

/*-
 * #%L
 * jambeth-persistence
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

import com.koch.ambeth.persistence.api.ITableMetaData;

public class TablesMapKey {
	private ITableMetaData table1, table2;

	public TablesMapKey(ITableMetaData table1, ITableMetaData table2) {
		this.table1 = table1;
		this.table2 = table2;
	}

	@Override
	public int hashCode() {
		return table1.hashCode() ^ table2.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof TablesMapKey)) {
			return false;
		}
		return equals((TablesMapKey) obj);
	}

	public boolean equals(TablesMapKey other) {
		return (table1.equals(other.table1) && table2.equals(other.table2))
				|| (table1.equals(other.table2) && table2.equals(other.table1));
	}
}
