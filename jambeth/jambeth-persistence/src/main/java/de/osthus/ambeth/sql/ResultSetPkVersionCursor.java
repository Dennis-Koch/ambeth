package de.osthus.ambeth.sql;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.persistence.IVersionCursor;
import de.osthus.ambeth.persistence.IVersionItem;
import de.osthus.ambeth.util.IDisposable;
import de.osthus.ambeth.util.ParamChecker;

public class ResultSetPkVersionCursor implements IVersionCursor, IVersionItem, IDisposable, IInitializingBean
{
	protected IResultSet resultSet;

	protected Object id, version;

	protected boolean containsVersion = true;

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertNotNull(resultSet, "ResultSet");
	}

	public void setContainsVersion(boolean containsVersion)
	{
		this.containsVersion = containsVersion;
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
	public Object getId(int idIndex)
	{
		if (idIndex == ObjRef.PRIMARY_KEY_INDEX)
		{
			return getId();
		}
		throw new UnsupportedOperationException("No alternate ids have been fetched");
	}

	@Override
	public Object getVersion()
	{
		return version;
	}

	@Override
	public int getAlternateIdCount()
	{
		return 0;
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
