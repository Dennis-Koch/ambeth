package de.osthus.ambeth.security;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import de.osthus.ambeth.appendable.AppendableStringBuilder;
import de.osthus.ambeth.cache.ClearAllCachesEvent;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.SmartCopyMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.database.ITransaction;
import de.osthus.ambeth.event.IEntityMetaDataEvent;
import de.osthus.ambeth.event.IEventDispatcher;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
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
import de.osthus.ambeth.privilege.IPrivilegeProvider;
import de.osthus.ambeth.privilege.model.IPrivilege;
import de.osthus.ambeth.privilege.model.ITypePrivilege;
import de.osthus.ambeth.privilege.model.impl.SkipAllTypePrivilege;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilderFactory;
import de.osthus.ambeth.security.config.SecurityConfigurationConstants;
import de.osthus.ambeth.security.model.IUser;
import de.osthus.ambeth.sql.ISqlBuilder;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.IMultithreadingHelper;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.setup.IDataSetup;

public class PermissionGroupUpdater implements IInitializingBean, IPermissionGroupUpdater, IStartingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext beanContext;

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
	protected IMultithreadingHelper multithreadingHelper;

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

	@Property(name = SecurityConfigurationConstants.SecurityActive, defaultValue = "false")
	protected boolean securityActive;

	protected final SmartCopyMap<Class<?>, IQuery<?>> entityTypeToAllEntitiesQuery = new SmartCopyMap<Class<?>, IQuery<?>>();

	protected IQuery<IUser> allUsersQuery;

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
		allUsersQuery = queryBuilderFactory.create(IUser.class).build();
	}

	public void handleEntityMetaDataEvent(IEntityMetaDataEvent entityMetaDataEvent)
	{
		// meta data has changed so we clear all cached queries because they might have gone illegal now
		entityTypeToAllEntitiesQuery.clear();
	}

	public void handleClearAllCachesEvent(ClearAllCachesEvent clearAllCachesEvent)
	{
		// meta data has changed so we clear all cached queries because they might have gone illegal now
		entityTypeToAllEntitiesQuery.clear();
	}

	@Override
	public void insertPermissionGroups()
	{
		if (!securityActive)
		{
			return;
		}
		if (userResolver == null)
		{
			return;
		}
		long start = System.currentTimeMillis();
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
					ArrayList<Runnable> runnables = new ArrayList<Runnable>();
					insertPermissionGroupsIntern(runnables);

					multithreadingHelper.invokeInParallel(beanContext, true, runnables.toArray(Runnable.class));
					return null;
				}
			}, securityScopes);
			long end = System.currentTimeMillis();
			log.info((end - start) + "ms");
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected IQuery<?> getAllEntitiesQuery(Class<?> entityType)
	{
		IQuery<?> allEntitiesQuery = entityTypeToAllEntitiesQuery.get(entityType);
		if (allEntitiesQuery != null)
		{
			return allEntitiesQuery;
		}
		allEntitiesQuery = queryBuilderFactory.create(entityType).build();
		entityTypeToAllEntitiesQuery.put(entityType, allEntitiesQuery);
		return allEntitiesQuery;
	}

	protected IList<IObjRef> loadAllObjRefsOfEntityTable(ITable table)
	{
		Class<?> entityType = table.getEntityType();
		IQuery<?> allEntitiesQuery = getAllEntitiesQuery(entityType);

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

	protected String[] getAllSids()
	{
		List<? extends IUser> allUsers = allUsersQuery.retrieve();

		String[] allSids = new String[allUsers.size()];
		for (int a = allUsers.size(); a-- > 0;)
		{
			IUser user = allUsers.get(a);
			allSids[a] = userIdentifierProvider.getSID(user);
		}
		return allSids;
	}

	protected void insertPermissionGroupsIntern(List<Runnable> runnables) throws Throwable
	{
		IDatabase database = this.database.getCurrent();
		final String[] allSids = getAllSids();

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
			final ITable fTable = table;
			final IPermissionGroup fPermissionGroup = permissionGroup;
			Runnable runnable = new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						IList<IObjRef> objRefs = loadAllObjRefsOfEntityTable(fTable);
						Object[] permissionGroupIds = createPermissionGroupIds(objRefs, fPermissionGroup);
						updateEntityRows(objRefs, permissionGroupIds, fPermissionGroup, fTable);
						insertPermissionGroupsForUsers(objRefs, permissionGroupIds, allSids, fPermissionGroup);
					}
					catch (Throwable e)
					{
						throw RuntimeExceptionUtil.mask(e);
					}
				}
			};
			runnables.add(runnable);
		}
	}

	protected void insertPermissionGroupsForUsers(IList<IObjRef> objRefs, Object[] permissionGroupIds, String[] allSids, IPermissionGroup permissionGroup)
			throws Throwable
	{
		IPrivilegeProvider privilegeProvider = this.privilegeProvider;
		ISecurityContext securityContext = securityContextHolder.getCreateContext();
		PreparedStatement insertPermissionGroupPstm = buildInsertPermissionGroupStm(permissionGroup);
		try
		{
			IAuthentication oldAuthentication = securityContext.getAuthentication();
			IAuthorization oldAuthorization = securityContext.getAuthorization();
			try
			{
				ISecurityScope[] securityScopes = securityScopeProvider.getSecurityScopes();
				for (String sid : allSids)
				{
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
			insertPermissionGroupPstm.close();
		}
	}

	private int id = 0;

	protected Object[] createPermissionGroupIds(IList<IObjRef> objRefs, IPermissionGroup permissionGroup)
	{
		Class<?> permissionGroupIdFieldType = permissionGroup.getPermissionGroupFieldOnTarget().getFieldType();

		Object[] permissionGroupIds = new Object[objRefs.size()];
		for (int a = objRefs.size(); a-- > 0;)
		{
			Object persistentPermissionGroupId = conversionHelper.convertValueToType(permissionGroupIdFieldType, Integer.valueOf(++id));
			permissionGroupIds[a] = persistentPermissionGroupId;
		}
		return permissionGroupIds;
	}

	protected void updateEntityRows(IList<IObjRef> objRefs, Object[] permissionGroupIds, IPermissionGroup permissionGroup, ITable table) throws Throwable
	{
		Class<?> idType = table.getIdField().getFieldType();
		PreparedStatement updateEntityRowPstm = buildUpdateEntityRowStm(permissionGroup, table);
		try
		{
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
			updateEntityRowPstm.close();
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
