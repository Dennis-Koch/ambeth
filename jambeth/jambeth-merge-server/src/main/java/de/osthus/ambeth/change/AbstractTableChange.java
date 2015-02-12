package de.osthus.ambeth.change;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.persistence.ITable;

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
