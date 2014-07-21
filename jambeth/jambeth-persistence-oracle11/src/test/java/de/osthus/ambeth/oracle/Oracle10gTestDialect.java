package de.osthus.ambeth.oracle;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.IocBootstrapModule;
import de.osthus.ambeth.ioc.factory.BeanContextFactory;
import de.osthus.ambeth.oracle.RandomUserScript.RandomUserModule;
import de.osthus.ambeth.persistence.jdbc.AbstractConnectionTestDialect;
import de.osthus.ambeth.persistence.jdbc.JdbcUtil;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;

public class Oracle10gTestDialect extends AbstractConnectionTestDialect
{
	@Override
	public boolean createTestUserIfSupported(Throwable reason, String userName, String userPassword, IProperties testProps) throws SQLException
	{
		if (!(reason instanceof SQLException))
		{
			return false;
		}
		if (((SQLException) reason).getErrorCode() != 1017) // ORA-01017: invalid username/password; logon denied
		{
			return false;
		}
		// try to recover by trying to create the necessary user with the default credentials of sys
		Properties createUserProps = new Properties(testProps);
		createUserProps.put(RandomUserScript.SCRIPT_IS_CREATE, "true");
		createUserProps.put(RandomUserScript.SCRIPT_USER_NAME, userName);
		createUserProps.put(RandomUserScript.SCRIPT_USER_PASS, userPassword);
		createUserProps.put(PersistenceJdbcConfigurationConstants.DatabaseUser, "sys as sysdba");
		createUserProps.put(PersistenceJdbcConfigurationConstants.DatabasePass, "developer");
		IServiceContext bootstrapContext = BeanContextFactory.createBootstrap(createUserProps);
		try
		{
			bootstrapContext.createService("randomUser", RandomUserModule.class, IocBootstrapModule.class);
		}
		finally
		{
			bootstrapContext.dispose();
		}
		return true;
	}

	@Override
	public void preProcessConnectionForTest(Connection connection, String[] schemaNames, boolean forcePreProcessing)
	{
		// intended blank
	}

	@SuppressWarnings("resource")
	@Override
	public boolean isEmptySchema(Connection connection) throws SQLException
	{
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = connection.createStatement();
			rs = stmt.executeQuery("SELECT tname FROM tab");
			while (rs.next())
			{
				if (!Oracle10gDialect.BIN_TABLE_NAME.matcher(rs.getString("tname")).matches()
						&& !Oracle10gDialect.IDX_TABLE_NAME.matcher(rs.getString("tname")).matches())
				{
					return false;
				}
			}
			rs.close();
			rs = stmt
					.executeQuery("SELECT object_type, object_name FROM user_objects WHERE object_type IN ('FUNCTION', 'INDEX', 'PACKAGE', 'PACKAGE BODY', 'PROCEDURE', 'SEQUENCE', 'TABLE', 'TYPE', 'VIEW')");
			return !rs.next();
		}
		finally
		{
			JdbcUtil.close(stmt, rs);
		}
	}

	@Override
	public String createOptimisticLockTrigger(Connection connection, String tableName) throws SQLException
	{
		if (Oracle10gDialect.BIN_TABLE_NAME.matcher(tableName).matches() || Oracle10gDialect.IDX_TABLE_NAME.matcher(tableName).matches())
		{
			return "";
		}
		int maxNameLength = connection.getMetaData().getMaxProcedureNameLength();
		StringBuilder sb = new StringBuilder();
		String forTriggerName = tableName;
		if (forTriggerName.length() >= maxNameLength - 3 - 3) // Substract 3 chars 'TR_' and 3 chars '_OL'
		{
			forTriggerName = forTriggerName.substring(0, maxNameLength - 3 - 3);
		}
		sb.append("create or replace TRIGGER \"TR_").append(forTriggerName).append("_OL\"");
		sb.append("	BEFORE UPDATE ON \"").append(tableName).append("\" FOR EACH ROW");
		sb.append(" BEGIN");
		sb.append(" if( :new.\"VERSION\" <= :old.\"VERSION\" ) then");
		sb.append(" raise_application_error( -");
		sb.append(Oracle10gDialect.getOptimisticLockErrorCode()).append(", 'Optimistic Lock Exception');");
		sb.append(" end if;");
		sb.append(" END;");
		return sb.toString();
	}

	@Override
	public List<String> getTablesWithoutOptimisticLockTrigger(Connection connection) throws SQLException
	{
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = connection.createStatement();
			stmt.execute("SELECT T.TNAME as TNAME FROM TAB T JOIN COLS C ON T.TNAME = C.TABLE_NAME WHERE C.COLUMN_NAME = 'VERSION'");
			rs = stmt.getResultSet();
			ArrayList<String> tableNames = new ArrayList<String>();
			while (rs.next())
			{
				String tableName = rs.getString("TNAME");
				if (Oracle10gDialect.BIN_TABLE_NAME.matcher(tableName).matches())
				{
					continue;
				}
				String tableNameLower = tableName.toLowerCase();
				if (tableNameLower.startsWith("link_") || tableNameLower.startsWith("l_"))
				{
					continue;
				}
				tableNames.add(tableName);
			}
			return tableNames;
		}
		finally
		{
			JdbcUtil.close(stmt, rs);
		}
	}

	@Override
	public String prepareCommand(String sqlCommand)
	{
		return sqlCommand;
	}

	@Override
	public List<String> buildDropAllSchemaContent(Connection conn, String schemaName)
	{
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = conn.createStatement();
			stmt.execute("SELECT TNAME, TABTYPE FROM TAB");
			rs = stmt.getResultSet();
			List<String> sql = new ArrayList<String>();
			while (rs.next())
			{
				String tableName = rs.getString(1);
				if (Oracle10gDialect.BIN_TABLE_NAME.matcher(tableName).matches() || Oracle10gDialect.IDX_TABLE_NAME.matcher(tableName).matches())
				{
					continue;
				}
				String tableType = rs.getString(2);
				if ("VIEW".equalsIgnoreCase(tableType))
				{
					sql.add("DROP VIEW " + escapeName(schemaName, tableName) + " CASCADE CONSTRAINTS");
				}
				else if ("TABLE".equalsIgnoreCase(tableType))
				{
					sql.add("DROP TABLE " + escapeName(schemaName, tableName) + " CASCADE CONSTRAINTS");
				}
				else if ("SYNONYM".equalsIgnoreCase(tableType))
				{
					sql.add("DROP SYNONYM " + escapeName(schemaName, tableName));
				}
			}
			JdbcUtil.close(rs);
			rs = stmt
					.executeQuery("SELECT object_type, object_name FROM user_objects WHERE object_type IN ('FUNCTION', 'INDEX', 'PACKAGE', 'PACKAGE BODY', 'PROCEDURE', 'SEQUENCE', 'SYNONYM', 'TABLE', 'TYPE', 'VIEW')");
			while (rs.next())
			{
				String objectType = rs.getString("object_type");
				String objectName = rs.getString("object_name");
				if (Oracle10gDialect.BIN_TABLE_NAME.matcher(objectName).matches() || Oracle10gDialect.IDX_TABLE_NAME.matcher(objectName).matches())
				{
					continue;
				}
				sql.add("DROP " + objectType + " " + escapeName(schemaName, objectName));
			}
			sql.add("PURGE RECYCLEBIN");
			return sql;
		}
		catch (SQLException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			JdbcUtil.close(stmt, rs);
		}
	}
}
