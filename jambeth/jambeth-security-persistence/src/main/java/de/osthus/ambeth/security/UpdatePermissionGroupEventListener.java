package de.osthus.ambeth.security;

import java.sql.Connection;

import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.datachange.model.IDataChangeOfSession;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilderFactory;
import de.osthus.ambeth.security.model.IUser;
import de.osthus.ambeth.sql.ISqlBuilder;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;

public class UpdatePermissionGroupEventListener implements IStartingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected Connection connection;

	@Autowired
	protected IDatabase database;

	@Autowired
	protected IQueryBuilderFactory queryBuilderFactory;

	@Autowired
	protected ISecurityActivation securityActivation;

	@Autowired
	protected IPermissionGroupUpdater permissionGroupUpdater;

	@Autowired
	protected ISqlBuilder sqlBuilder;

	protected IQuery<IUser> allUsersQuery;

	@Override
	public void afterStarted() throws Throwable
	{
		allUsersQuery = queryBuilderFactory.create(IUser.class).build();
	}

	public void handleDataChangeOfSession(IDataChangeOfSession dataChangeOfSession) throws Throwable
	{
		final IDataChange dataChange = dataChangeOfSession.getDataChange();
		if (dataChange.isEmpty())
		{
			return;
		}
		securityActivation.executeWithoutFiltering(new IResultingBackgroundWorkerDelegate<Object>()
		{
			@Override
			public Object invoke() throws Throwable
			{
				handleDataChangeIntern(dataChange);
				return null;
			}
		});
	}

	protected void handleDataChangeIntern(IDataChange dataChange)
	{
		permissionGroupUpdater.insertPermissionGroups();
		permissionGroupUpdater.insertPermissionGroups();
		permissionGroupUpdater.insertPermissionGroups();
		// IDatabase database = this.database.getCurrent();
		//
		// List<? extends IUser> allUsers = allUsersQuery.retrieve();
		//
		// for (ITable table : database.getTables())
		// {
		// Class<?> entityType = table.getEntityType();
		// if (entityType == null)
		// {
		// continue;
		// }
		// IPermissionGroup permissionGroup = database.getPermissionGroupOfTable(table.getName());
		// if (permissionGroup == null)
		// {
		// continue;
		// }
		// // IList<IObjRef> objRefs = loadAllObjRefsOfEntityTable(table);
		// // Object[] permissionGroupIds = createPermissionGroupIds(objRefs, permissionGroup);
		// // updateEntityRows(objRefs, permissionGroupIds, permissionGroup, table);
		// // insertPermissionGroupsForUsers(objRefs, permissionGroupIds, allUsers, permissionGroup);
		// }
		//
		// List<IDataChangeEntry> inserts = dataChange.getInserts();
		//
		// for (int a = inserts.size(); a-- > 0;)
		// {
		// IDataChangeEntry insertEntry = inserts.get(a);
		// ITable table = database.getTableByType(insertEntry.getEntityType());
		// IPermissionGroup permissionGroup = database.getPermissionGroupOfTable(table.getName());
		// if (permissionGroup == null)
		// {
		// // nothing to do
		// continue;
		// }
		// // permissionGroup.getTable()
		// }
		System.out.println();
	}
	// protected PreparedStatement buildDeletePermissionGroupStm(IPermissionGroup permissionGroup) throws SQLException
	// {
	// AppendableStringBuilder sb = new AppendableStringBuilder();
	// sb.append("DELETE FROM ");
	// sqlBuilder.escapeName(permissionGroup.getTable().getName(), sb).append(" WHERE");
	// how should the row be identified it the permission group id is already lost in the deleted row? Maybe a IMergeListener implementation could catch this
	// information before a DCE is fired
	//
	// return connection.prepareStatement(sb.toString());
	// }
}
