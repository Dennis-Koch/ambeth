package de.osthus.ambeth.persistence.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public abstract class AbstractConnectionTestDialect implements IConnectionTestDialect
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

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

	@Override
	public boolean createTestUserIfSupported(Throwable reason, String userName, String userPassword, IProperties testProps) throws SQLException
	{
		return false;
	}

	@Override
	public void dropCreatedTestUser(String userName, String userPassword, IProperties testProps) throws SQLException
	{
		// intended blank
	}

	@Override
	public void preStructureRebuild(Connection connection) throws SQLException
	{
		// intended blank
	}

	protected String prepareCommandIntern(String sqlCommand, String regex, String replacement)
	{
		return Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(sqlCommand).replaceAll(replacement);
	}

	protected String prepareCommandInternWithGroup(String sqlCommand, String regex, String replacement)
	{
		Pattern pattern = Pattern.compile("(.*)" + regex + "(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		return concat(sqlCommand, replacement, pattern);
	}

	protected String concat(String sqlCommand, String replacement, Pattern pattern)
	{
		Matcher matcher = pattern.matcher(sqlCommand);
		if (!matcher.matches())
		{
			return sqlCommand;
		}
		String left = concat(matcher.group(1), replacement, pattern);
		String right = concat(matcher.group(3), replacement, pattern);
		return left + replacement.replace("\\2", matcher.group(2)) + right;
	}
}
