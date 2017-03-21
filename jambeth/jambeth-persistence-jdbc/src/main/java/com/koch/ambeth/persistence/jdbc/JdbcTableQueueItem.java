package com.koch.ambeth.persistence.jdbc;

/*-
 * #%L
 * jambeth-persistence-jdbc
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

import java.sql.Array;
import java.sql.PreparedStatement;

import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.ILinkedMap;

public class JdbcTableQueueItem
{
	private final PreparedStatement pstm;

	private final java.sql.Array array;

	private final ILinkedMap<Object, IObjRef> persistenceIdToOriMap;

	private final ILinkedMap<Object, Object> persistenceIdToPersistenceVersionMap;

	public JdbcTableQueueItem(PreparedStatement pstm, Array array, ILinkedMap<Object, IObjRef> persistenceIdToOriMap,
			ILinkedMap<Object, Object> persistenceIdToPersistenceVersionMap)
	{
		this.pstm = pstm;
		this.array = array;
		this.persistenceIdToOriMap = persistenceIdToOriMap;
		this.persistenceIdToPersistenceVersionMap = persistenceIdToPersistenceVersionMap;
	}

	public java.sql.Array getArray()
	{
		return array;
	}

	public ILinkedMap<Object, IObjRef> getPersistenceIdToOriMap()
	{
		return persistenceIdToOriMap;
	}

	public ILinkedMap<Object, Object> getPersistenceIdToPersistenceVersionMap()
	{
		return persistenceIdToPersistenceVersionMap;
	}

	public PreparedStatement getPstm()
	{
		return pstm;
	}
}
