package de.osthus.ambeth.sql;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.util.IDisposable;
import de.osthus.ambeth.util.ParamChecker;

public class DefaultResultSetProvider implements IResultSetProvider, IInitializingBean, IDisposable
{

	protected ISqlConnection connection;
	protected String tableName;
	protected String fieldsSQL;
	protected String whereSQL;

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertNotNull(this.connection, "Connection");
		ParamChecker.assertNotNull(this.tableName, "TableName");
		ParamChecker.assertNotNull(this.fieldsSQL, "FieldsSQL");
	}

	@Override
	public void dispose()
	{
		this.connection = null;
		this.tableName = null;
		this.fieldsSQL = null;
		this.whereSQL = null;
	}

	public ISqlConnection getConnection()
	{
		return connection;
	}

	public void setConnection(ISqlConnection connection)
	{
		this.connection = connection;
	}

	public String getTableName()
	{
		return tableName;
	}

	public void setTableName(String tableName)
	{
		this.tableName = tableName;
	}

	public String getFieldsSQL()
	{
		return fieldsSQL;
	}

	public void setFieldsSQL(String fieldsSQL)
	{
		this.fieldsSQL = fieldsSQL;
	}

	public String getWhereSQL()
	{
		return whereSQL;
	}

	public void setWhereSQL(String whereSQL)
	{
		this.whereSQL = whereSQL;
	}

	@Override
	public IResultSet getResultSet()
	{
		return this.connection.selectFields(this.tableName, this.fieldsSQL, this.whereSQL, null);
	}

	@Override
	public void skipResultSet()
	{
		dispose();
	}

}
