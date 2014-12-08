package de.osthus.ambeth.testutil;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import de.osthus.ambeth.appendable.AppendableStringBuilder;
import de.osthus.ambeth.cache.ClearAllCachesEvent;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.database.DatabaseCallback;
import de.osthus.ambeth.database.ITransaction;
import de.osthus.ambeth.event.IEventDispatcher;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IMergeProcess;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IField;
import de.osthus.ambeth.persistence.IPermissionGroup;
import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.persistence.IVersionCursor;
import de.osthus.ambeth.persistence.IVersionItem;
import de.osthus.ambeth.persistence.jdbc.JdbcUtil;
import de.osthus.ambeth.privilege.IPrivilegeProvider;
import de.osthus.ambeth.privilege.model.IPrivilege;
import de.osthus.ambeth.privilege.model.ITypePrivilege;
import de.osthus.ambeth.privilege.model.impl.SkipAllTypePrivilege;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilderFactory;
import de.osthus.ambeth.security.CallPermission;
import de.osthus.ambeth.security.DefaultAuthentication;
import de.osthus.ambeth.security.IAuthentication;
import de.osthus.ambeth.security.IAuthorization;
import de.osthus.ambeth.security.ISecurityActivation;
import de.osthus.ambeth.security.ISecurityContext;
import de.osthus.ambeth.security.ISecurityContextHolder;
import de.osthus.ambeth.security.ISecurityScopeProvider;
import de.osthus.ambeth.security.IUserIdentifierProvider;
import de.osthus.ambeth.security.IUserResolver;
import de.osthus.ambeth.security.PasswordType;
import de.osthus.ambeth.security.model.IUser;
import de.osthus.ambeth.sql.ISqlBuilder;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.setup.IDataSetup;

public class DataSetupExecutor implements IInitializingBean, IStartingBean
{
	private static final ThreadLocal<Boolean> autoRebuildDataTL = new ThreadLocal<Boolean>();

	public static Boolean setAutoRebuildData(Boolean autoRebuildData)
	{
		Boolean oldValue = autoRebuildDataTL.get();
		if (autoRebuildData == null)
		{
			autoRebuildDataTL.remove();
		}
		else
		{
			autoRebuildDataTL.set(autoRebuildData);
		}
		return oldValue;
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected Connection connection;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IDatabase database;

	@Autowired
	protected IDataSetup dataSetup;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IEventDispatcher eventDispatcher;

	@Autowired
	protected IMergeProcess mergeProcess;

	@Autowired
	protected IPrivilegeProvider privilegeProvider;

	@Autowired
	protected IQueryBuilderFactory queryBuilderFactory;

	@Autowired
	protected ISecurityActivation securityActivation;

	@Autowired
	protected ISecurityContextHolder securityContextHolder;

	@Autowired
	protected ISecurityScopeProvider securityScopeProvider;

	@Autowired
	protected ISqlBuilder sqlBuilder;

	@Autowired
	protected IThreadLocalCleanupController threadLocalCleanupController;

	@Autowired
	protected ITransaction transaction;

	@Autowired(optional = true)
	protected IUserIdentifierProvider userIdentifierProvider;

	@Autowired(optional = true)
	protected IUserResolver userResolver;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		if (userResolver != null)
		{
			ParamChecker.assertNotNull(userIdentifierProvider, "userIdentifierProvider");
		}
	}

	@Override
	public void afterStarted() throws Throwable
	{
		if (Boolean.TRUE.equals(autoRebuildDataTL.get()))
		{
			rebuildData();
		}
	}

	public void rebuildData()
	{
		try
		{
			try
			{
				securityActivation.executeWithoutSecurity(new IResultingBackgroundWorkerDelegate<Object>()
				{
					@Override
					public Object invoke() throws Throwable
					{
						transaction.processAndCommit(new DatabaseCallback()
						{
							@Override
							public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) throws Throwable
							{
								Collection<Object> dataSet = dataSetup.executeDatasetBuilders();
								if (dataSet.size() > 0)
								{
									mergeProcess.process(dataSet, null, null, null, false);
								}
								insertPermissionGroups();
							}
						});
						return null;
					}
				});
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			finally
			{
				eventDispatcher.dispatchEvent(ClearAllCachesEvent.getInstance());
			}
		}
		finally
		{
			// beanContext.getService(ISecurityContextHolder.class).clearContext();
			threadLocalCleanupController.cleanupThreadLocal();
		}
	}

	protected void insertPermissionGroups()
	{
		if (userResolver == null)
		{
			return;
		}
		ISecurityScope[] securityScopes = new ISecurityScope[] { new ISecurityScope()
		{
			@Override
			public String getName()
			{
				return "dummy";
			}
		} };
		try
		{
			securityScopeProvider.executeWithSecurityScopes(new IResultingBackgroundWorkerDelegate<Object>()
			{
				@Override
				public Object invoke() throws Throwable
				{
					insertPermissionGroupsIntern();
					return null;
				}
			}, securityScopes);
		}
		catch (Throwable e1)
		{
			throw RuntimeExceptionUtil.mask(e1);
		}
	}

	protected IList<IObjRef> loadAllObjRefsOfEntityTable(ITable table)
	{
		Class<?> entityType = table.getEntityType();
		IQuery<?> allEntitiesQuery = queryBuilderFactory.create(entityType).build();

		Class<?> idType = table.getIdField().getFieldType();
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
		Class<?> idTypeOfObject = metaData.getIdMember().getRealType();
		Class<?> versionTypeOfObject = metaData.getVersionMember() != null ? metaData.getVersionMember().getRealType() : null;

		IVersionCursor versionCursor = allEntitiesQuery.retrieveAsVersions();
		try
		{
			ArrayList<IObjRef> objRefs = new ArrayList<IObjRef>();
			while (versionCursor.moveNext())
			{
				IVersionItem item = versionCursor.getCurrent();

				Object id = conversionHelper.convertValueToType(idType, item.getId());
				// INTENTIONALLY converting the id in 2 steps: first to the fieldType, then to the idTypeOfObject
				// this is because of the fact that the idTypeOfObject may be an Object.class which does not convert the id
				// correctly by itself
				id = conversionHelper.convertValueToType(idTypeOfObject, id);
				Object version = conversionHelper.convertValueToType(versionTypeOfObject, item.getVersion());
				objRefs.add(new ObjRef(entityType, ObjRef.PRIMARY_KEY_INDEX, id, version));
			}
			return objRefs;
		}
		finally
		{
			versionCursor.dispose();
		}
	}

	protected PreparedStatement buildInsertPermissionGroupStm(IPermissionGroup permissionGroup) throws SQLException
	{
		AppendableStringBuilder sb = new AppendableStringBuilder();
		sb.append("INSERT INTO ");
		sqlBuilder.escapeName(permissionGroup.getTable().getName(), sb).append(" (");
		sqlBuilder.escapeName(permissionGroup.getUserField().getName(), sb).append(',');
		sqlBuilder.escapeName(permissionGroup.getPermissionGroupField().getName(), sb).append(',');
		sqlBuilder.escapeName(permissionGroup.getReadPermissionField().getName(), sb).append(',');
		sqlBuilder.escapeName(permissionGroup.getUpdatePermissionField().getName(), sb).append(',');
		sqlBuilder.escapeName(permissionGroup.getDeletePermissionField().getName(), sb);
		sb.append(") VALUES (?,?,?,?,?)");

		return connection.prepareStatement(sb.toString());
	}

	protected PreparedStatement buildUpdateEntityRowStm(IPermissionGroup permissionGroup, ITable table) throws SQLException
	{
		AppendableStringBuilder sb = new AppendableStringBuilder();
		sb.append("UPDATE ");
		sqlBuilder.escapeName(table.getName(), sb).append(" SET ");
		IField versionField = table.getVersionField();
		sqlBuilder.escapeName(permissionGroup.getPermissionGroupFieldOnTarget().getName(), sb).append("=?");
		if (versionField != null)
		{
			sb.append(',');
			sqlBuilder.escapeName(versionField.getName(), sb).append('=');
			sqlBuilder.escapeName(versionField.getName(), sb).append("+1");
		}
		sb.append(" WHERE ");
		sqlBuilder.escapeName(table.getIdField().getName(), sb).append("=?");

		return connection.prepareStatement(sb.toString());
	}

	protected void insertPermissionGroupsIntern()
	{
		IDatabase database = this.database.getCurrent();

		List<? extends IUser> allUsers = queryBuilderFactory.create(IUser.class).build().retrieve();

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
			IList<IObjRef> objRefs = loadAllObjRefsOfEntityTable(table);
			Object[] permissionGroupIds = createPermissionGroupIds(objRefs, permissionGroup);
			updateEntityRows(objRefs, permissionGroupIds, permissionGroup, table);
			insertPermissionGroupsForUsers(objRefs, permissionGroupIds, allUsers, permissionGroup);
		}
	}

	protected void insertPermissionGroupsForUsers(IList<IObjRef> objRefs, Object[] permissionGroupIds, List<? extends IUser> allUsers,
			IPermissionGroup permissionGroup)
	{
		ISecurityContext securityContext = securityContextHolder.getCreateContext();
		PreparedStatement insertPermissionGroupPstm = null;
		IPrivilegeProvider privilegeProvider = this.privilegeProvider;
		try
		{
			insertPermissionGroupPstm = buildInsertPermissionGroupStm(permissionGroup);

			IAuthentication oldAuthentication = securityContext.getAuthentication();
			IAuthorization oldAuthorization = securityContext.getAuthorization();
			try
			{
				ISecurityScope[] securityScopes = securityScopeProvider.getSecurityScopes();
				for (IUser user : allUsers)
				{
					String sid = userIdentifierProvider.getSID(user);

					securityContext.setAuthentication(new DefaultAuthentication(sid, "dummyPass".toCharArray(), PasswordType.PLAIN));
					securityContext.setAuthorization(mockAuthorization(sid, securityScopes));

					IList<IPrivilege> privileges = privilegeProvider.getPrivilegesByObjRef(objRefs);

					insertPermissionGroupPstm.setObject(1, sid);

					for (int a = permissionGroupIds.length; a-- > 0;)
					{
						Object permissionGroupId = permissionGroupIds[a];

						insertPermissionGroupPstm.setObject(2, permissionGroupId);

						IPrivilege privilege = privileges.get(a);

						int readAllowed = privilege == null || privilege.isReadAllowed() ? 1 : 0;
						int updateAllowed = privilege == null || privilege.isUpdateAllowed() ? 1 : 0;
						int deleteAllowed = privilege == null || privilege.isDeleteAllowed() ? 1 : 0;

						insertPermissionGroupPstm.setInt(3, readAllowed);
						insertPermissionGroupPstm.setInt(4, updateAllowed);
						insertPermissionGroupPstm.setInt(5, deleteAllowed);
						insertPermissionGroupPstm.addBatch();
					}
				}
			}
			finally
			{
				securityContext.setAuthentication(oldAuthentication);
				securityContext.setAuthorization(oldAuthorization);
			}
			insertPermissionGroupPstm.executeBatch();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			JdbcUtil.close(insertPermissionGroupPstm);
		}
	}

	protected Object[] createPermissionGroupIds(IList<IObjRef> objRefs, IPermissionGroup permissionGroup)
	{
		Class<?> permissionGroupIdFieldType = permissionGroup.getPermissionGroupFieldOnTarget().getFieldType();

		Object[] permissionGroupIds = new Object[objRefs.size()];
		for (int a = objRefs.size(); a-- > 0;)
		{
			Object persistentPermissionGroupId = conversionHelper.convertValueToType(permissionGroupIdFieldType, Integer.valueOf(a + 1));
			permissionGroupIds[a] = persistentPermissionGroupId;
		}
		return permissionGroupIds;
	}

	protected void updateEntityRows(IList<IObjRef> objRefs, Object[] permissionGroupIds, IPermissionGroup permissionGroup, ITable table)
	{
		Class<?> idType = table.getIdField().getFieldType();
		PreparedStatement updateEntityRowPstm = null;
		try
		{
			updateEntityRowPstm = buildUpdateEntityRowStm(permissionGroup, table);

			for (int a = objRefs.size(); a-- > 0;)
			{
				IObjRef objRef = objRefs.get(a);

				Object persistentEntityId = conversionHelper.convertValueToType(idType, objRef.getId());
				updateEntityRowPstm.setObject(1, permissionGroupIds[a]);
				updateEntityRowPstm.setObject(2, persistentEntityId);

				updateEntityRowPstm.addBatch();
			}
			updateEntityRowPstm.executeBatch();
		}
		catch (SQLException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			JdbcUtil.close(updateEntityRowPstm);
		}
	}

	protected IAuthorization mockAuthorization(final String sid, final ISecurityScope[] securityScopes)
	{
		return new IAuthorization()
		{
			@Override
			public boolean isValid()
			{
				return true;
			}

			@Override
			public boolean hasActionPermission(String actionPermissionName, ISecurityScope[] securityScopes)
			{
				return true;
			}

			@Override
			public ISecurityScope[] getSecurityScopes()
			{
				return securityScopes;
			}

			@Override
			public String getSID()
			{
				return sid;
			}

			@Override
			public ITypePrivilege getEntityTypePrivilege(Class<?> entityType, ISecurityScope[] securityScopes)
			{
				return SkipAllTypePrivilege.INSTANCE;
			}

			@Override
			public CallPermission getCallPermission(Method serviceOperation, ISecurityScope[] securityScopes)
			{
				return CallPermission.FORBIDDEN;
			}
		};
	}
}
