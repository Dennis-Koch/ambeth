package de.osthus.ambeth.sql;

public interface IResultSetProvider
{

	IResultSet getResultSet();

	void skipResultSet();

}
