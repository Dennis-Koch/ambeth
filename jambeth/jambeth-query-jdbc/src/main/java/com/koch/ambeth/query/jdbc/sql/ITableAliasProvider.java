package com.koch.ambeth.query.jdbc.sql;

public interface ITableAliasProvider
{
	String getNextJoinAlias();

	String getNextSubQueryAlias();
}