package com.koch.ambeth.merge.server.change;

import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.persistence.api.ITable;

public abstract class AbstractTableChange implements ITableChange
{
	@Property
	protected String entityHandlerName;

	protected ITable table;

	@Override
	public void setTable(ITable table)
	{
		this.table = table;
	}

	@Override
	public String getEntityHandlerName()
	{
		return entityHandlerName;
	}

	@Override
	public ITable getTable()
	{
		return table;
	}

	@Override
	public int compareTo(ITableChange o)
	{
		return getEntityHandlerName().compareTo(o.getEntityHandlerName());
	}

	@Override
	public String toString()
	{
		return getTable().toString();
	}
}
