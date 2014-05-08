package de.osthus.ambeth.sql;

import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.persistence.IDataCursor;
import de.osthus.ambeth.persistence.IDataItem;
import de.osthus.ambeth.util.IDisposable;
import de.osthus.ambeth.util.ParamChecker;

public class ResultSetDataCursor implements IDataCursor, IDataItem, IDisposable, IInitializingBean
{
	protected IResultSet resultSet;

	protected IMap<String, Integer> propertyToColIndexMap;

	protected Object[] data;

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertNotNull(resultSet, "ResultSet");
		ParamChecker.assertNotNull(propertyToColIndexMap, "PropertyToColIndexMap");
		data = new Object[propertyToColIndexMap.size()];
	}

	public IResultSet getResultSet()
	{
		return resultSet;
	}

	public void setResultSet(IResultSet resultSet)
	{
		this.resultSet = resultSet;
	}

	public void setPropertyToColIndexMap(IMap<String, Integer> propertyToColIndexMap)
	{
		this.propertyToColIndexMap = propertyToColIndexMap;
	}

	@Override
	public int getFieldCount()
	{
		return propertyToColIndexMap.size();
	}

	@Override
	public Object getValue(String propertyName)
	{
		Integer index = propertyToColIndexMap.get(propertyName);
		return data[index];
	}

	@Override
	public Object getValue(int index)
	{
		return data[index];
	}

	@Override
	public IDataItem getCurrent()
	{
		return this;
	}

	@Override
	public boolean moveNext()
	{
		if (this.resultSet.moveNext())
		{
			data = resultSet.getCurrent();
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
		propertyToColIndexMap = null;
	}
}
