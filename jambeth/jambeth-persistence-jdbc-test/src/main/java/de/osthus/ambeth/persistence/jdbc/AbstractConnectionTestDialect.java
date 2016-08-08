package de.osthus.ambeth.persistence.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.orm.IOrmPatternMatcher;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;

public abstract class AbstractConnectionTestDialect implements IConnectionTestDialect, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConnectionDialect connectionDialect;

	@Autowired
	protected IOrmPatternMatcher ormPatternMatcher;

	@Property(name = PersistenceConfigurationConstants.DatabaseTableIgnore, mandatory = false)
	protected String ignoredTableProperty;

	protected final HashSet<String> ignoredTables = new HashSet<String>();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		if (ignoredTableProperty != null)
		{
			ignoredTables.addAll(connectionDialect.toDefaultCase(ignoredTableProperty).split("[;:]"));
		}
	}

	@Override
	public void resetStatementCache(Connection connection)
	{
		// intended blank
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

	/**
	 * An SQL query. there must be column name "FULL_NAME" referred to the found table name
	 * 
	 * @param connection
	 * @param stmt
	 * 
	 * @return
	 */
	protected abstract IList<String> queryForAllTables(Connection connection) throws SQLException;

	/**
	 * An SQL query. there must be column name "TRIGGER_NAME" referred to the found trigger name
	 * 
	 * @param connection
	 * 
	 * @return
	 */
	protected abstract IList<String> queryForAllTriggers(Connection connection) throws SQLException;

	protected boolean isTableNameToIgnore(String tableName)
	{
		return false;
	}

	@Override
	public List<String> getTablesWithoutOptimisticLockTrigger(Connection connection) throws SQLException
	{
		HashSet<String> existingOptimisticLockTriggers = new HashSet<String>();
		ArrayList<String> tableNamesWhichNeedOptimisticLockTrigger = new ArrayList<String>();
		for (String tableName : queryForAllTables(connection))
		{
			if (isTableNameToIgnore(tableName))
			{
				continue;
			}
			if (ignoredTables.contains(tableName))
			{
				continue;
			}
			if (ormPatternMatcher.matchesArchivePattern(tableName))
			{
				// archive tables do not need an optimistic lock trigger
				continue;
			}
			tableNamesWhichNeedOptimisticLockTrigger.add(tableName);
		}
		for (String triggerName : queryForAllTriggers(connection))
		{
			if (ormPatternMatcher.matchesOptimisticLockTriggerPattern(triggerName))
			{
				existingOptimisticLockTriggers.add(triggerName);
			}
		}
		int maxProcedureNameLength = connection.getMetaData().getMaxProcedureNameLength();
		for (int a = tableNamesWhichNeedOptimisticLockTrigger.size(); a-- > 0;)
		{
			String permissionGroupName = ormPatternMatcher.buildPermissionGroupFromTableName(tableNamesWhichNeedOptimisticLockTrigger.get(a),
					maxProcedureNameLength);
			if (existingOptimisticLockTriggers.contains(permissionGroupName))
			{
				tableNamesWhichNeedOptimisticLockTrigger.removeAtIndex(a);
			}
		}
		return tableNamesWhichNeedOptimisticLockTrigger;
	}

	protected abstract IList<String> queryForAllPermissionGroupNeedingTables(Connection connection) throws SQLException;

	/**
	 * Result column must be "PERM_GROUP_NAME"
	 * 
	 * @param connection
	 * 
	 * @return
	 */
	protected abstract IList<String> queryForAllPotentialPermissionGroups(Connection connection) throws SQLException;

	@Override
	public List<String> getTablesWithoutPermissionGroup(Connection connection) throws SQLException
	{
		HashSet<String> existingPermissionGroups = new HashSet<String>();
		ArrayList<String> tableNamesWhichNeedPermissionGroup = new ArrayList<String>();
		for (String tableName : queryForAllPermissionGroupNeedingTables(connection))
		{
			if (isTableNameToIgnore(tableName))
			{
				continue;
			}
			if (ignoredTables.contains(tableName))
			{
				continue;
			}
			if (ormPatternMatcher.matchesArchivePattern(tableName))
			{
				// archive tables do not need a permission group
				continue;
			}
			if (ormPatternMatcher.matchesPermissionGroupPattern(tableName))
			{
				// permission groups themselves have no permission group
				existingPermissionGroups.add(tableName);
				continue;
			}
			tableNamesWhichNeedPermissionGroup.add(tableName);
		}
		for (String tableName : queryForAllPotentialPermissionGroups(connection))
		{
			if (ormPatternMatcher.matchesPermissionGroupPattern(tableName))
			{
				existingPermissionGroups.add(tableName);
			}
		}
		int maxProcedureNameLength = connection.getMetaData().getMaxProcedureNameLength();
		for (int a = tableNamesWhichNeedPermissionGroup.size(); a-- > 0;)
		{
			String permissionGroupName = ormPatternMatcher.buildPermissionGroupFromTableName(tableNamesWhichNeedPermissionGroup.get(a), maxProcedureNameLength);
			if (existingPermissionGroups.contains(permissionGroupName))
			{
				tableNamesWhichNeedPermissionGroup.removeAtIndex(a);
			}
		}
		return tableNamesWhichNeedPermissionGroup;
	}

	@Override
	public String[] createAdditionalTriggers(Connection connection, String tableName) throws SQLException
	{
		return new String[0];
	}
}
