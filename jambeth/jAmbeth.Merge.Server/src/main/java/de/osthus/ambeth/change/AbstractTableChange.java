package de.osthus.ambeth.change;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.util.ParamChecker;

public abstract class AbstractTableChange implements ITableChange, IInitializingBean
{
	protected String entityHandlerName;

	protected ITable table;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(entityHandlerName, "EntityHandlerName");
	}

	public void setEntityHandlerName(String entityHandlerName)
	{
		this.entityHandlerName = entityHandlerName;
	}

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
	public void dispose()
	{
		// Intended blank
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
}
