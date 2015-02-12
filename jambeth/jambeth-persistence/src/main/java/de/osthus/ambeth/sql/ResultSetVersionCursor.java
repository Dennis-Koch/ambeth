package de.osthus.ambeth.sql;

import de.osthus.ambeth.merge.transfer.ObjRef;

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
