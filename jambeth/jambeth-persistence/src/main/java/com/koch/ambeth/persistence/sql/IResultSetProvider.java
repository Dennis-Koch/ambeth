package com.koch.ambeth.persistence.sql;

public interface IResultSetProvider
{

	IResultSet getResultSet();

	void skipResultSet();

}
