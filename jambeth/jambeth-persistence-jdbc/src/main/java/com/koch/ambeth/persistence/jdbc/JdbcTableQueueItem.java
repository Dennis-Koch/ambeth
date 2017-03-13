package com.koch.ambeth.persistence.jdbc;

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
