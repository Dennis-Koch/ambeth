package com.koch.ambeth.query.jdbc.sql;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

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
