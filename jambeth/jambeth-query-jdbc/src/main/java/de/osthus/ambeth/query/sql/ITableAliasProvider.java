package de.osthus.ambeth.query.sql;

public interface ITableAliasProvider
{
	String getNextJoinAlias();

	String getNextSubQueryAlias();
}