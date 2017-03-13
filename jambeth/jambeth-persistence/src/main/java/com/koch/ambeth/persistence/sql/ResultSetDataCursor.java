package com.koch.ambeth.persistence.sql;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.query.persistence.IDataCursor;
import com.koch.ambeth.query.persistence.IDataItem;
import com.koch.ambeth.util.IDisposable;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.IMap;

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
