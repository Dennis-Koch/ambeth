package de.osthus.ambeth.sql;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.persistence.IVersionCursor;
import de.osthus.ambeth.persistence.IVersionItem;
import de.osthus.ambeth.util.IDisposable;
import de.osthus.ambeth.util.ParamChecker;

public class ResultSetVersionCursor implements IVersionCursor, IVersionItem, IDisposable, IInitializingBean
{
	private static final Object[] EMPTY_ALTERNATE_IDS = new Object[0];

	protected IResultSet resultSet;

	protected Object id, version;

	protected Object[] alternateIds;

	protected boolean containsVersion = true;

	protected int systemColumnCount;

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertNotNull(resultSet, "ResultSet");
		systemColumnCount = containsVersion ? 2 : 1;
	}

	public void setContainsVersion(boolean containsVersion)
	{
		this.containsVersion = containsVersion;
	}

	public IResultSet getResultSet()
	{
		return resultSet;
	}

	public void setResultSet(IResultSet resultSet)
	{
		this.resultSet = resultSet;
	}

	@Override
	public Object getId()
	{
		return id;
	}

	@Override
	public Object getId(byte idIndex)
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
	public Object getVersion()
	{
		return version;
	}

	@Override
	public byte getAlternateIdCount()
	{
		return (byte) alternateIds.length;
	}

	@Override
	public IVersionItem getCurrent()
	{
		return this;
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

	@Override
	public void dispose()
	{
		if (resultSet != null)
		{
			resultSet.dispose();
			resultSet = null;
		}
	}
}
