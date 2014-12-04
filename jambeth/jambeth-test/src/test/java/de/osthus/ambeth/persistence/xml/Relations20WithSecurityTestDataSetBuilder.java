package de.osthus.ambeth.persistence.xml;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Collection;

import de.osthus.ambeth.appendable.AppendableStringBuilder;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IField;
import de.osthus.ambeth.persistence.IPermissionGroup;
import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.persistence.jdbc.JdbcUtil;
import de.osthus.ambeth.security.SecurityTest;
import de.osthus.ambeth.sql.ISqlBuilder;
import de.osthus.ambeth.util.setup.AbstractDatasetBuilder;
import de.osthus.ambeth.util.setup.IDatasetBuilder;

public class Relations20WithSecurityTestDataSetBuilder extends AbstractDatasetBuilder
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected Connection connection;

	@Autowired
	protected IDatabase database;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected ISqlBuilder sqlBuilder;

	@Override
	protected void buildDatasetInternal()
	{
		IThreadLocalObjectCollector current = objectCollector.getCurrent();
		AppendableStringBuilder sb = current.create(AppendableStringBuilder.class);
		Statement stm = null;
		try
		{
			stm = connection.createStatement();

			for (ITable table : database.getTables())
			{
				Class<?> entityType = table.getEntityType();
				if (entityType == null)
				{
					continue;
				}
				IPermissionGroup permissionGroup = database.getPermissionGroupOfTable(table.getName());
				if (permissionGroup == null)
				{
					continue;
				}
				String tableName = permissionGroup.getTable().getName();

				String permissionGroupId = "1";

				sb.reset();
				sb.append("INSERT INTO ");
				sqlBuilder.escapeName(tableName, sb).append(" (");
				sqlBuilder.escapeName(permissionGroup.getUserField().getName(), sb).append(',');
				sqlBuilder.escapeName(permissionGroup.getPermissionGroupField().getName(), sb).append(',');
				sqlBuilder.escapeName(permissionGroup.getReadPermissionField().getName(), sb).append(") VALUES ('").append(SecurityTest.userName1).append("',")
						.append(permissionGroupId).append(",1)");

				stm.execute(sb.toString());

				IField versionField = table.getVersionField();

				sb.reset();
				sb.append("UPDATE ");
				sqlBuilder.escapeName(table.getName(), sb).append(" SET ");
				sqlBuilder.escapeName(permissionGroup.getPermissionGroupFieldOnTarget().getName(), sb).append("=").append(permissionGroupId);

				if (versionField != null)
				{
					sb.append(',');
					sqlBuilder.escapeName(versionField.getName(), sb).append('=');
					sqlBuilder.escapeName(versionField.getName(), sb).append("+1");
				}
				stm.executeUpdate(sb.toString());
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			JdbcUtil.close(stm);
		}
	}

	@Override
	public Collection<Class<? extends IDatasetBuilder>> getDependsOn()
	{
		return null;
	}
}
