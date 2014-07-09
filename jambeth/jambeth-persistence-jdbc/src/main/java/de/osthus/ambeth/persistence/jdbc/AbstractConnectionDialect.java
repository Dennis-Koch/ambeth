package de.osthus.ambeth.persistence.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.ITransactionState;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;

public abstract class AbstractConnectionDialect implements IConnectionDialect
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired(optional = true)
	protected ITransactionState transactionState;

	@Property(name = PersistenceConfigurationConstants.ExternalTransactionManager, defaultValue = "false")
	protected boolean externalTransactionManager;

	@Override
	public boolean useVersionOnOptimisticUpdate()
	{
		return false;
	}

	@Override
	public void commit(Connection connection) throws SQLException
	{
		Boolean active = transactionState != null ? transactionState.isExternalTransactionManagerActive() : null;
		if (active == null)
		{
			active = Boolean.valueOf(externalTransactionManager);
		}
		if (active.booleanValue())
		{
			// No Action!
			// Transactions are externally managed.
		}
		else
		{
			connection.commit();
		}
	}

	@Override
	public void rollback(Connection connection) throws SQLException
	{
		Boolean active = transactionState != null ? transactionState.isExternalTransactionManagerActive() : null;
		if (active == null)
		{
			active = Boolean.valueOf(externalTransactionManager);
		}
		if (active.booleanValue())
		{
			// No Action!
			// Transactions are externally managed.
		}
		else
		{
			connection.rollback();
		}
	}

	protected String escapeName(String schemaName, String tableName)
	{
		if (schemaName == null)
		{
			return "\"" + tableName + "\"";
		}
		return "\"" + schemaName + "\".\"" + tableName + "\"";
	}

	protected void printResultSet(ResultSet rs) throws SQLException
	{
		ResultSetMetaData metaData = rs.getMetaData();
		int columnCount = metaData.getColumnCount();
		for (int a = 0, size = columnCount; a < size; a++)
		{
			System.out.print(metaData.getColumnLabel(a + 1));
			System.out.print("\t\t");
		}
		System.out.println("\t\t");
		while (rs.next())
		{
			for (int a = 0, size = columnCount; a < size; a++)
			{
				System.out.print(rs.getObject(a + 1));
				System.out.print("\t\t");
			}
			System.out.println();
		}
	}
}
