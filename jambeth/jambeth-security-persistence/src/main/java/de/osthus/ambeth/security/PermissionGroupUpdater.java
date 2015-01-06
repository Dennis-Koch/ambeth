package de.osthus.ambeth.security;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.appendable.AppendableStringBuilder;
import de.osthus.ambeth.cache.ClearAllCachesEvent;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.SmartCopyMap;
import de.osthus.ambeth.collections.SmartCopySet;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.database.ITransaction;
import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.datachange.model.IDataChangeEntry;
import de.osthus.ambeth.event.EntityMetaDataAddedEvent;
import de.osthus.ambeth.event.EntityMetaDataRemovedEvent;
import de.osthus.ambeth.event.IEntityMetaDataEvent;
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
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IPermissionGroup;
import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.persistence.IVersionCursor;
import de.osthus.ambeth.persistence.IVersionItem;
import de.osthus.ambeth.privilege.IEntityPermissionRule;
import de.osthus.ambeth.privilege.IEntityPermissionRuleEvent;
import de.osthus.ambeth.privilege.IEntityPermissionRuleProvider;
import de.osthus.ambeth.privilege.IPrivilegeProvider;
import de.osthus.ambeth.privilege.model.IPrivilege;
import de.osthus.ambeth.privilege.model.ITypePrivilege;
import de.osthus.ambeth.privilege.model.impl.SkipAllTypePrivilege;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilderFactory;
import de.osthus.ambeth.security.config.SecurityConfigurationConstants;
import de.osthus.ambeth.security.model.IUser;
import de.osthus.ambeth.sql.ISqlBuilder;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.util.CachePath;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.IMultithreadingHelper;
import de.osthus.ambeth.util.IPrefetchConfig;
import de.osthus.ambeth.util.IPrefetchHandle;
import de.osthus.ambeth.util.IPrefetchHelper;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.PrefetchHandle;
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
	protected IEntityPermissionRuleProvider entityPermissionRuleProvider;

	@Autowired
	protected IMergeProcess mergeProcess;

	@Autowired
	protected IMultithreadingHelper multithreadingHelper;

	@Autowired
	protected IPrefetchHelper prefetchHelper;

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
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected ITransaction transaction;

	@Autowired(optional = true)
	protected IUserIdentifierProvider userIdentifierProvider;

	@Autowired(optional = true)
	protected IUserResolver userResolver;

	@Property(name = SecurityConfigurationConstants.SecurityActive, defaultValue = "false")
	protected boolean securityActive;

	protected final SmartCopyMap<Class<?>, IQuery<?>> entityTypeToAllEntitiesQuery = new SmartCopyMap<Class<?>, IQuery<?>>();

	protected final SmartCopyMap<Class<?>, Class<?>[]> entityTypeToRuleReferredEntitiesMap = new SmartCopyMap<Class<?>, Class<?>[]>();

	protected final SmartCopySet<Class<?>> metaDataAvailableSet = new SmartCopySet<Class<?>>();

	protected final ThreadLocal<Boolean> dataChangeHandlingActiveTL = new ThreadLocal<Boolean>();

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
		if (securityActive)
		{
			allUsersQuery = queryBuilderFactory.create(IUser.class).build();
		}
	}

	public void handleEntityMetaDataEvent(IEntityMetaDataEvent entityMetaDataEvent)
	{
		// meta data has changed so we clear all cached queries because they might have gone illegal now
		entityTypeToAllEntitiesQuery.clear();

		// we track all mapped entities
		if (entityMetaDataEvent instanceof EntityMetaDataAddedEvent)
		{
			metaDataAvailableSet.addAll(entityMetaDataEvent.getEntityTypes());
		}
		else if (entityMetaDataEvent instanceof EntityMetaDataRemovedEvent)
		{
			metaDataAvailableSet.removeAll(entityMetaDataEvent.getEntityTypes());
		}
	}

	public void handleEntityPermissionRuleEvent(IEntityPermissionRuleEvent entityPermissionRuleEvent)
	{
		entityTypeToRuleReferredEntitiesMap.clear();
	}

	public void handleClearAllCachesEvent(ClearAllCachesEvent clearAllCachesEvent)
	{
		// meta data has changed so we clear all cached queries because they might have gone illegal now
		entityTypeToAllEntitiesQuery.clear();
		entityTypeToRuleReferredEntitiesMap.clear();
	}

	protected void addTypesOfCachePath(CachePath[] cachePath, Set<Class<?>> entityTypes)
	{
		if (cachePath == null)
		{
			return;
		}
		for (CachePath cachePathItem : cachePath)
		{
			entityTypes.add(cachePathItem.memberType);

			addTypesOfCachePath(cachePathItem.children, entityTypes);
		}
	}

	protected IMap<Class<?>, PgUpdateEntry> createPgUpdateMap(IDataChange dataChange)
	{
		HashMap<Class<?>, PgUpdateEntry> entityToPgUpdateMap = HashMap.<Class<?>, PgUpdateEntry> create(metaDataAvailableSet.size());
		IDatabase database = this.database.getCurrent();
		for (Class<?> entityType : metaDataAvailableSet)
		{
			ITable table = database.getTableByType(entityType);
			IPermissionGroup permissionGroup = database.getPermissionGroupOfTable(table.getName());
			if (permissionGroup == null)
			{
				continue;
			}
			PgUpdateEntry pgUpdateEntry = new PgUpdateEntry(entityType, permissionGroup);
			entityToPgUpdateMap.put(entityType, pgUpdateEntry);
		}
		evaluateEntityPermissionRules(dataChange, entityToPgUpdateMap);
		return entityToPgUpdateMap;
	}

	protected IDataChange getOrCreateDataChangeOfEntityType(IDataChange dataChange, Class<?> entityType, IMap<Class<?>, IDataChange> entityTypeToDataChangeMap)
	{
		IDataChange dataChangeOfEntityType = entityTypeToDataChangeMap.get(entityType);
		if (dataChangeOfEntityType != null)
		{
			return dataChangeOfEntityType;
		}
		dataChangeOfEntityType = dataChange.derive(entityType);
		entityTypeToDataChangeMap.put(entityType, dataChangeOfEntityType);
		return dataChangeOfEntityType;
	}

	protected boolean isDataChangeEmpty(IDataChange dataChange, Class<?> entityType, IMap<Class<?>, IDataChange> entityTypeToDataChangeMap,
			IMap<Class<?>, Boolean> entityTypeToEmptyFlagMap)
	{
		IDataChange dataChangeOfEntityType = entityTypeToDataChangeMap.get(entityType);
		if (dataChangeOfEntityType != null)
		{
			return dataChangeOfEntityType.isEmpty();
		}
		Boolean emptyFlag = entityTypeToEmptyFlagMap.get(entityType);
		if (emptyFlag != null)
		{
			return emptyFlag.booleanValue();
		}
		emptyFlag = dataChange != null ? Boolean.valueOf(dataChange.isEmptyByType(entityType)) : Boolean.TRUE;
		entityTypeToEmptyFlagMap.put(entityType, emptyFlag);
		return emptyFlag.booleanValue();
	}

	protected void evaluateEntityPermissionRules(IDataChange dataChange, IMap<Class<?>, PgUpdateEntry> entityToPgUpdateMap)
	{
		HashMap<Class<?>, IDataChange> entityTypeToDataChangeMap = HashMap.<Class<?>, IDataChange> create(entityToPgUpdateMap.size());
		HashMap<Class<?>, Boolean> entityTypeToEmptyFlagMap = HashMap.<Class<?>, Boolean> create(entityToPgUpdateMap.size());

		for (Entry<Class<?>, PgUpdateEntry> entry : entityToPgUpdateMap)
		{
			Class<?> entityType = entry.getKey();

			PgUpdateEntry pgUpdateEntry = entry.getValue();
			if (dataChange == null)
			{
				pgUpdateEntry.setUpdateType(PermissionGroupUpdateType.EACH_ROW);
				continue;
			}
			if (!PermissionGroupUpdateType.EACH_ROW.equals(pgUpdateEntry.getUpdateType()))
			{
				if (hasChangesOnRuleReferredEntities(entityType, dataChange, entityTypeToDataChangeMap, entityTypeToEmptyFlagMap))
				{
					pgUpdateEntry.setUpdateType(PermissionGroupUpdateType.EACH_ROW);
				}
			}

			if (!PermissionGroupUpdateType.EACH_ROW.equals(pgUpdateEntry.getUpdateType()))
			{
				if (!isDataChangeEmpty(dataChange, entityType, entityTypeToDataChangeMap, entityTypeToEmptyFlagMap))
				{
					pgUpdateEntry.setUpdateType(PermissionGroupUpdateType.SELECTED_ROW);
				}
			}

			if (PermissionGroupUpdateType.SELECTED_ROW.equals(pgUpdateEntry.getUpdateType()))
			{
				IDataChange dataChangeOfEntityType = getOrCreateDataChangeOfEntityType(dataChange, entityType, entityTypeToDataChangeMap);
				pgUpdateEntry.setDataChange(dataChangeOfEntityType);
			}
		}
	}

	protected boolean hasChangesOnRuleReferredEntities(Class<?> entityType, IDataChange dataChange, IMap<Class<?>, IDataChange> entityTypeToDataChangeMap,
			IMap<Class<?>, Boolean> entityTypeToEmptyFlagMap)
	{
		Class<?>[] touchedByRuleTypes = entityTypeToRuleReferredEntitiesMap.get(entityType);

		if (touchedByRuleTypes == null)
		{
			IList<IEntityPermissionRule<?>> entityPermissionRules = entityPermissionRuleProvider.getEntityPermissionRules(entityType);
			IPrefetchConfig prefetchConfig = prefetchHelper.createPrefetch();
			for (IEntityPermissionRule entityPermissionRule : entityPermissionRules)
			{
				entityPermissionRule.buildPrefetchConfig(entityType, prefetchConfig);
			}
			IPrefetchHandle prefetchHandle = prefetchConfig.build();
			ILinkedMap<Class<?>, CachePath[]> entityTypeToPrefetchSteps = ((PrefetchHandle) prefetchHandle).getEntityTypeToPrefetchSteps();

			HashSet<Class<?>> touchedByRuleTypesSet = HashSet.<Class<?>> create(entityTypeToPrefetchSteps.size());
			for (Entry<Class<?>, CachePath[]> prefetchEntry : entityTypeToPrefetchSteps)
			{
				Class<?> entityTypeOfPrefetch = prefetchEntry.getKey();
				touchedByRuleTypesSet.add(entityTypeOfPrefetch);

				addTypesOfCachePath(prefetchEntry.getValue(), touchedByRuleTypesSet);
			}
			touchedByRuleTypes = touchedByRuleTypesSet.toArray(Class.class);
			entityTypeToRuleReferredEntitiesMap.put(entityType, touchedByRuleTypes);
		}
		for (Class<?> entityTypeTouchedByRule : touchedByRuleTypes)
		{
			if (!isDataChangeEmpty(dataChange, entityTypeTouchedByRule, entityTypeToDataChangeMap, entityTypeToEmptyFlagMap))
			{
				// if 'entityTypeTouchedByRule' has changes which may be read by the any rule of the current 'entityType':
				// the current permissions for 'entityType' have to be fully re-evaluated because of the "foreign" change
				return true;
			}
		}
		return false;
	}

	@Override
	public <R> R executeWithoutPermissionGroupUpdate(IResultingBackgroundWorkerDelegate<R> runnable)
	{
		Boolean dataChangeHandlingActive = dataChangeHandlingActiveTL.get();
		dataChangeHandlingActiveTL.set(Boolean.FALSE);
		try
		{
			return runnable.invoke();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			if (dataChangeHandlingActive != null)
			{
				dataChangeHandlingActiveTL.set(Boolean.FALSE);
			}
			else
			{
				dataChangeHandlingActiveTL.remove();
			}
		}
	}

	@Override
	public void fillEmptyPermissionGroups()
	{
		updatePermissionGroups(null);
	}

	@Override
	public void updatePermissionGroups(final IDataChange dataChange)
	{
		if (!securityActive)
		{
			return;
		}
		if (userResolver == null)
		{
			return;
		}
		if (Boolean.FALSE.equals(dataChangeHandlingActiveTL.get()))
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
			securityScopeProvider.executeWithSecurityScopes(new IBackgroundWorkerDelegate()
			{
				@Override
				public void invoke() throws Throwable
				{
					IMap<Class<?>, PgUpdateEntry> entityToPgUpdateMap = createPgUpdateMap(dataChange);
					ArrayList<PermissionGroupUpdateForkItem> forkItems = new ArrayList<PermissionGroupUpdateForkItem>();
					insertPermissionGroupsIntern(entityToPgUpdateMap, forkItems, dataChange != null);

					multithreadingHelper.invokeAndWait(forkItems, new IBackgroundWorkerParamDelegate<PermissionGroupUpdateForkItem>()
					{
						@Override
						public void invoke(PermissionGroupUpdateForkItem itemOfFork) throws Throwable
						{
							PgUpdateEntry pgUpdateEntry = itemOfFork.pgUpdateEntry;
							IPermissionGroup permissionGroup = pgUpdateEntry.getPermissionGroup();
							ITable table = permissionGroup.getTargetTable();
							IList<IObjRef> objRefs;
							switch (pgUpdateEntry.getUpdateType())
							{
								case NOTHING:
								{
									return;
								}
								case SELECTED_ROW:
								{
									objRefs = loadSelectedObjRefs(pgUpdateEntry);
									break;
								}
								case EACH_ROW:
								{
									objRefs = loadAllObjRefsOfEntityTable(table, pgUpdateEntry);
									break;
								}
								default:
									throw RuntimeExceptionUtil.createEnumNotSupportedException(pgUpdateEntry.getUpdateType());
							}
							Object[] permissionGroupIds = createPermissionGroupIds(objRefs, permissionGroup);
							updateEntityRows(objRefs, permissionGroupIds, permissionGroup, table);
							insertPermissionGroupsForUsers(objRefs, permissionGroupIds, itemOfFork.allSids, permissionGroup);
						}
					});
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

	protected IList<IObjRef> loadSelectedObjRefs(PgUpdateEntry pgUpdateEntry)
	{
		IDataChange dataChange = pgUpdateEntry.getDataChange();
		List<IDataChangeEntry> inserts = dataChange.getInserts();
		List<IDataChangeEntry> updates = dataChange.getUpdates();
		ArrayList<IObjRef> objRefs = new ArrayList<IObjRef>(inserts.size() + updates.size());
		for (int a = inserts.size(); a-- > 0;)
		{
			IDataChangeEntry dataChangeEntry = inserts.get(a);
			objRefs.add(new ObjRef(dataChangeEntry.getEntityType(), dataChangeEntry.getIdNameIndex(), dataChangeEntry.getId(), dataChangeEntry.getVersion()));
		}
		for (int a = updates.size(); a-- > 0;)
		{
			IDataChangeEntry dataChangeEntry = updates.get(a);
			objRefs.add(new ObjRef(dataChangeEntry.getEntityType(), dataChangeEntry.getIdNameIndex(), dataChangeEntry.getId(), dataChangeEntry.getVersion()));
		}
		return objRefs;
	}

	protected IList<IObjRef> loadAllObjRefsOfEntityTable(ITable table, PgUpdateEntry pgUpdateEntry)
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
		// IField versionField = table.getVersionField();
		sqlBuilder.escapeName(permissionGroup.getPermissionGroupFieldOnTarget().getName(), sb).append("=?");
		// if (versionField != null)
		// {
		// sb.append(',');
		// sqlBuilder.escapeName(versionField.getName(), sb).append('=');
		// sqlBuilder.escapeName(versionField.getName(), sb).append("+1");
		// }
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

	protected void insertPermissionGroupsIntern(IMap<Class<?>, PgUpdateEntry> entityToPgUpdateMap, List<PermissionGroupUpdateForkItem> runnables,
			boolean triggeredByDCE) throws Throwable
	{
		String[] allSids = getAllSids();

		boolean debugEnabled = log.isDebugEnabled();
		IThreadLocalObjectCollector objectCollector = this.objectCollector.getCurrent();
		StringBuilder sb = debugEnabled ? objectCollector.create(StringBuilder.class) : null;
		try
		{
			if (debugEnabled)
			{
				sb.append("PermissionGroup updates");
				if (triggeredByDCE)
				{
					sb.append(" triggered by DCE:");
				}
				else
				{
					sb.append(" full rebuild:");
				}
			}
			IList<Class<?>> entityTypes = entityToPgUpdateMap.keyList();
			int maxLength = 0;
			if (debugEnabled)
			{
				Collections.sort(entityTypes, new Comparator<Class<?>>()
				{
					@Override
					public int compare(Class<?> o1, Class<?> o2)
					{
						return o1.getName().compareTo(o2.getName());
					}
				});
				for (Class<?> entityType : entityTypes)
				{
					maxLength = Math.max(entityType.getName().length(), maxLength);
				}
			}
			for (Class<?> entityType : entityTypes)
			{
				PgUpdateEntry pgUpdateEntry = entityToPgUpdateMap.get(entityType);
				if (PermissionGroupUpdateType.NOTHING.equals(pgUpdateEntry.getUpdateType()))
				{
					continue;
				}
				PermissionGroupUpdateForkItem runnable = new PermissionGroupUpdateForkItem(allSids, pgUpdateEntry);
				runnables.add(runnable);

				if (debugEnabled)
				{
					sb.append("\n\t");
					String name = entityType.getName();
					int length = name.length();
					sb.append(name).append(": ");
					while (length < maxLength)
					{
						length++;
						sb.append(' ');
					}
					sb.append(pgUpdateEntry.getUpdateType().name());
				}
			}
			if (runnables.size() > 0 && debugEnabled)
			{
				log.debug(sb);
			}
		}
		finally
		{
			if (debugEnabled)
			{
				objectCollector.dispose(sb);
			}
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

	private volatile int id = 0;

	private final Lock idLock = new ReentrantLock();

	protected Object[] createPermissionGroupIds(IList<IObjRef> objRefs, IPermissionGroup permissionGroup)
	{
		Class<?> permissionGroupIdFieldType = permissionGroup.getPermissionGroupFieldOnTarget().getFieldType();

		int id;
		idLock.lock();
		try
		{
			id = this.id;
			this.id += objRefs.size();
		}
		finally
		{
			idLock.unlock();
		}
		IConversionHelper conversionHelper = this.conversionHelper;
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
		IConversionHelper conversionHelper = this.conversionHelper;
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
