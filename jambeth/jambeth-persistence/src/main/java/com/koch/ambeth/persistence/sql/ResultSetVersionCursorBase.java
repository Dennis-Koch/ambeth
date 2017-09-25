package com.koch.ambeth.persistence.sql;

import java.util.Iterator;

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

import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.query.persistence.IVersionItem;

public class ResultSetVersionCursorBase extends ResultSetPkVersionCursorBase {
	private static final Object[] EMPTY_ALTERNATE_IDS = new Object[0];

	protected Object[] alternateIds;

	protected int systemColumnCount;

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		systemColumnCount = containsVersion ? 2 : 1;
	}

	@Override
	public Object getId(int idIndex) {
		if (idIndex == ObjRef.PRIMARY_KEY_INDEX) {
			return getId();
		}
		else {
			return alternateIds[idIndex];
		}
	}

	@Override
	public int getAlternateIdCount() {
		return alternateIds.length;
	}

	@Override
	public IVersionItem next() {
		if (resultSetIter == null) {
			resultSetIter = resultSet.iterator();
		}
		Iterator<Object[]> resultSetIter = this.resultSetIter;
		Object[] current = resultSetIter.next();
		if (current == null) {
			return null;
		}
		processResultSetItem(current);

		return this;
	}

	protected void processResultSetItem(Object[] current) {
		id = current[0];
		if (containsVersion) {
			version = current[1];
		}
		int systemColumnCount = this.systemColumnCount;
		Object[] alternateIds = this.alternateIds;
		if (alternateIds == null) {
			int arraySize = current.length - systemColumnCount;
			if (arraySize == 0) {
				alternateIds = EMPTY_ALTERNATE_IDS;
			}
			else {
				alternateIds = new Object[arraySize];
			}
			this.alternateIds = alternateIds;
		}
		for (int i = current.length; i-- > systemColumnCount;) {
			alternateIds[i - systemColumnCount] = current[i];
		}
	}
}
