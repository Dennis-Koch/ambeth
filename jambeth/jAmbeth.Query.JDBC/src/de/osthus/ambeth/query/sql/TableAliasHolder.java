package de.osthus.ambeth.query.sql;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class TableAliasHolder implements ITableAliasHolder
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	private String tableAlias;

	@Override
	public String getTableAlias()
	{
		return tableAlias;
	}

	@Override
	public void setTableAlias(String tableAlias)
	{
		this.tableAlias = tableAlias;
	}
}
