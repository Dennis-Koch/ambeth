package com.koch.ambeth.persistence.sql;

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

public class ResultSetVersionCursor extends ResultSetPkVersionCursor
{
	private static final Object[] EMPTY_ALTERNATE_IDS = new Object[0];

	protected Object[] alternateIds;

	protected int systemColumnCount;

	@Override
	public void afterPropertiesSet()
	{
		super.afterPropertiesSet();
		systemColumnCount = containsVersion ? 2 : 1;
	}

	@Override
	public Object getId(int idIndex)
	{
		if (idIndex == ObjRef.PRIMARY_KEY_INDEX)
		{
			return getId();
		}
		else
		{
			return alternateIds[idIndex];
		}
	}

	@Override
	public int getAlternateIdCount()
	{
		return alternateIds.length;
	}

	@Override
	public boolean moveNext()
	{
		IResultSet resultSet = this.resultSet;
		if (resultSet.moveNext())
		{
			Object[] current = resultSet.getCurrent();
			id = current[0];
			if (containsVersion)
			{
				version = current[1];
			}
			int systemColumnCount = this.systemColumnCount;
			Object[] alternateIds = this.alternateIds;
			if (alternateIds == null)
			{
				int arraySize = current.length - systemColumnCount;
				if (arraySize == 0)
				{
					alternateIds = EMPTY_ALTERNATE_IDS;
				}
				else
				{
					alternateIds = new Object[arraySize];
				}
				this.alternateIds = alternateIds;
			}
			for (int i = current.length; i-- > systemColumnCount;)
			{
				alternateIds[i - systemColumnCount] = current[i];
			}
			return true;
		}
		return false;
	}
}
