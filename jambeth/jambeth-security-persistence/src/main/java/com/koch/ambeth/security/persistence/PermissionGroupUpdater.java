package com.koch.ambeth.security.persistence;

/*-
 * #%L
 * jambeth-security-persistence
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.cache.util.PrefetchHandle;
import com.koch.ambeth.cache.util.PrefetchPath;
import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.datachange.model.IDataChangeEntry;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import com.koch.ambeth.ioc.util.IMultithreadingHelper;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IMergeProcess;
import com.koch.ambeth.merge.config.MergeConfigurationConstants;
import com.koch.ambeth.merge.event.EntityMetaDataAddedEvent;
import com.koch.ambeth.merge.event.EntityMetaDataRemovedEvent;
import com.koch.ambeth.merge.event.IEntityMetaDataEvent;
import com.koch.ambeth.merge.metadata.IObjRefFactory;
import com.koch.ambeth.merge.metadata.IPreparedObjRefFactory;
import com.koch.ambeth.merge.proxy.PersistenceContext;
import com.koch.ambeth.merge.proxy.PersistenceContextType;
import com.koch.ambeth.merge.security.ISecurityActivation;
import com.koch.ambeth.merge.security.ISecurityScopeProvider;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.merge.util.IPrefetchConfig;
import com.koch.ambeth.merge.util.IPrefetchHandle;
import com.koch.ambeth.merge.util.IPrefetchHelper;
import com.koch.ambeth.merge.util.setup.IDataSetup;
import com.koch.ambeth.persistence.api.IDatabaseMetaData;
import com.koch.ambeth.persistence.api.IPermissionGroup;
import com.koch.ambeth.persistence.api.IPrimaryKeyProvider;
import com.koch.ambeth.persistence.api.ITableMetaData;
import com.koch.ambeth.persistence.api.sql.ISqlBuilder;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.query.persistence.IVersionCursor;
import com.koch.ambeth.query.persistence.IVersionItem;
import com.koch.ambeth.security.AuthenticationResult;
import com.koch.ambeth.security.CallPermission;
import com.koch.ambeth.security.DefaultAuthentication;
import com.koch.ambeth.security.DefaultAuthorization;
import com.koch.ambeth.security.IAuthentication;
import com.koch.ambeth.security.IAuthorization;
import com.koch.ambeth.security.IAuthorizationManager;
import com.koch.ambeth.security.ISecurityContext;
import com.koch.ambeth.security.ISecurityContextHolder;
import com.koch.ambeth.security.PasswordType;
import com.koch.ambeth.security.model.IUser;
import com.koch.ambeth.security.privilege.IPrivilegeProvider;
import com.koch.ambeth.security.privilege.model.IPrivilege;
import com.koch.ambeth.security.server.IUserIdentifierProvider;
import com.koch.ambeth.security.server.IUserResolver;
import com.koch.ambeth.security.server.SecurityFilterInterceptor;
import com.koch.ambeth.security.server.privilege.IEntityPermissionRule;
import com.koch.ambeth.security.server.privilege.IEntityPermissionRuleEvent;
import com.koch.ambeth.security.server.privilege.IEntityPermissionRuleProvider;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.model.ISecurityScope;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.appendable.AppendableStringBuilder;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptyMap;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.SmartCopyMap;
import com.koch.ambeth.util.collections.SmartCopySet;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.function.CheckedSupplier;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import com.koch.ambeth.util.state.StateRollback;
import com.koch.ambeth.util.transaction.ILightweightTransaction;

import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

@PersistenceContext(PersistenceContextType.NOT_REQUIRED)
public class PermissionGroupUpdater implements IInitializingBean, IPermissionGroupUpdater {
    protected final SmartCopyMap<Class<?>, IQuery<?>> entityTypeToAllEntitiesQuery = new SmartCopyMap<>();
    protected final SmartCopyMap<Class<?>, Class<?>[]> entityTypeToRuleReferredEntitiesMap = new SmartCopyMap<>();
    protected final SmartCopySet<Class<?>> metaDataAvailableSet = new SmartCopySet<>();
    protected final ThreadLocal<Boolean> dataChangeHandlingActiveTL = new ThreadLocal<>();
    @Autowired(optional = true)
    protected IAuthorizationManager authorizationManager;
    @Autowired
    protected IServiceContext beanContext;
    @Autowired
    protected Connection connection;
    @Autowired
    protected IConversionHelper conversionHelper;
    @Autowired
    protected IDatabaseMetaData databaseMetaData;
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
    protected IObjRefFactory objRefFactory;
    @Autowired
    protected IPrefetchHelper prefetchHelper;
    @Autowired
    protected IPrimaryKeyProvider primaryKeyProvider;
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
    protected ILightweightTransaction transaction;
    @Autowired(optional = true)
    protected IUserIdentifierProvider userIdentifierProvider;
    @Autowired(optional = true)
    protected IUserResolver userResolver;
    @Property(name = MergeConfigurationConstants.SecurityActive, defaultValue = "false")
    protected boolean securityActive;
    protected volatile IQuery<IUser> allUsersQuery;
    @LogInstance
    private ILogger log;

    @Override
    public void afterPropertiesSet() throws Throwable {
        if (userResolver != null) {
            ParamChecker.assertNotNull(userIdentifierProvider, "userIdentifierProvider");
        }
    }

    protected IQuery<IUser> getAllUsersQuery() {
        if (allUsersQuery != null) {
            return allUsersQuery;
        }
        allUsersQuery = queryBuilderFactory.create(IUser.class).build();
        return allUsersQuery;
    }

    public void handleEntityMetaDataEvent(IEntityMetaDataEvent entityMetaDataEvent) {
        // meta data has changed so we clear all cached queries because they might have gone illegal now
        entityTypeToAllEntitiesQuery.clear();

        // we track all mapped entities
        if (entityMetaDataEvent instanceof EntityMetaDataAddedEvent) {
            metaDataAvailableSet.addAll(entityMetaDataEvent.getEntityTypes());
        } else if (entityMetaDataEvent instanceof EntityMetaDataRemovedEvent) {
            metaDataAvailableSet.removeAll(entityMetaDataEvent.getEntityTypes());
        }
    }

    public void handleEntityPermissionRuleEvent(IEntityPermissionRuleEvent entityPermissionRuleEvent) {
        entityTypeToRuleReferredEntitiesMap.clear();
    }

    public void handleClearAllCachesEvent(ClearAllCachesEvent clearAllCachesEvent) {
        // meta data has changed so we clear all cached queries because they might have gone illegal now
        entityTypeToAllEntitiesQuery.clear();
        entityTypeToRuleReferredEntitiesMap.clear();
    }

    protected void addTypesOfCachePath(PrefetchPath[] cachePath, Set<Class<?>> entityTypes) {
        if (cachePath == null) {
            return;
        }
        for (PrefetchPath cachePathItem : cachePath) {
            entityTypes.add(cachePathItem.memberType);

            addTypesOfCachePath(cachePathItem.children, entityTypes);
        }
    }

    protected IMap<Class<?>, PgUpdateEntry> createPgUpdateMap(IDataChange dataChange) {
        IDatabaseMetaData databaseMetaData = this.databaseMetaData;
        HashMap<Class<?>, PgUpdateEntry> entityToPgUpdateMap = null;
        for (Class<?> entityType : metaDataAvailableSet) {
            IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
            // if this entity is not managed by Ambeth in the database the pg updater is not necessary
            if (!metaData.isLocalEntity()) {
                continue;
            }
            ITableMetaData table = databaseMetaData.getTableByType(entityType);
            IPermissionGroup permissionGroup = databaseMetaData.getPermissionGroupOfTable(table.getName());
            if (permissionGroup == null) {
                continue;
            }
            PgUpdateEntry pgUpdateEntry = new PgUpdateEntry(entityType, permissionGroup);
            if (entityToPgUpdateMap == null) {
                entityToPgUpdateMap = HashMap.<Class<?>, PgUpdateEntry>create(metaDataAvailableSet.size());
            }
            entityToPgUpdateMap.put(entityType, pgUpdateEntry);
        }
        if (entityToPgUpdateMap == null) {
            return EmptyMap.<Class<?>, PgUpdateEntry>emptyMap();
        }
        evaluateEntityPermissionRules(dataChange, entityToPgUpdateMap);
        return entityToPgUpdateMap;
    }

    protected IDataChange getOrCreateDataChangeOfEntityType(IDataChange dataChange, Class<?> entityType, IMap<Class<?>, IDataChange> entityTypeToDataChangeMap) {
        IDataChange dataChangeOfEntityType = entityTypeToDataChangeMap.get(entityType);
        if (dataChangeOfEntityType != null) {
            return dataChangeOfEntityType;
        }
        dataChangeOfEntityType = dataChange.derive(entityType);
        entityTypeToDataChangeMap.put(entityType, dataChangeOfEntityType);
        return dataChangeOfEntityType;
    }

    protected boolean isDataChangeEmpty(IDataChange dataChange, Class<?> entityType, IMap<Class<?>, IDataChange> entityTypeToDataChangeMap, IMap<Class<?>, Boolean> entityTypeToEmptyFlagMap) {
        IDataChange dataChangeOfEntityType = entityTypeToDataChangeMap.get(entityType);
        if (dataChangeOfEntityType != null) {
            return dataChangeOfEntityType.isEmpty();
        }
        Boolean emptyFlag = entityTypeToEmptyFlagMap.get(entityType);
        if (emptyFlag != null) {
            return emptyFlag.booleanValue();
        }
        emptyFlag = dataChange != null ? Boolean.valueOf(dataChange.isEmptyByType(entityType)) : Boolean.TRUE;
        entityTypeToEmptyFlagMap.put(entityType, emptyFlag);
        return emptyFlag.booleanValue();
    }

    protected void evaluateEntityPermissionRules(IDataChange dataChange, IMap<Class<?>, PgUpdateEntry> entityToPgUpdateMap) {
        if (entityToPgUpdateMap.isEmpty()) {
            return;
        }
        HashMap<Class<?>, IDataChange> entityTypeToDataChangeMap = HashMap.<Class<?>, IDataChange>create(entityToPgUpdateMap.size());
        HashMap<Class<?>, Boolean> entityTypeToEmptyFlagMap = HashMap.<Class<?>, Boolean>create(entityToPgUpdateMap.size());

        for (Entry<Class<?>, PgUpdateEntry> entry : entityToPgUpdateMap) {
            Class<?> entityType = entry.getKey();

            PgUpdateEntry pgUpdateEntry = entry.getValue();
            if (dataChange == null) {
                pgUpdateEntry.setUpdateType(PermissionGroupUpdateType.EACH_ROW);
                continue;
            }
            if (!PermissionGroupUpdateType.EACH_ROW.equals(pgUpdateEntry.getUpdateType())) {
                if (hasChangesOnRuleReferredEntities(entityType, dataChange, entityTypeToDataChangeMap, entityTypeToEmptyFlagMap)) {
                    pgUpdateEntry.setUpdateType(PermissionGroupUpdateType.EACH_ROW);
                }
            }

            if (!PermissionGroupUpdateType.EACH_ROW.equals(pgUpdateEntry.getUpdateType())) {
                if (!isDataChangeEmpty(dataChange, entityType, entityTypeToDataChangeMap, entityTypeToEmptyFlagMap)) {
                    pgUpdateEntry.setUpdateType(PermissionGroupUpdateType.SELECTED_ROW);
                }
            }

            if (PermissionGroupUpdateType.SELECTED_ROW.equals(pgUpdateEntry.getUpdateType())) {
                IDataChange dataChangeOfEntityType = getOrCreateDataChangeOfEntityType(dataChange, entityType, entityTypeToDataChangeMap);
                pgUpdateEntry.setDataChange(dataChangeOfEntityType);
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected boolean hasChangesOnRuleReferredEntities(Class<?> entityType, IDataChange dataChange, IMap<Class<?>, IDataChange> entityTypeToDataChangeMap,
            IMap<Class<?>, Boolean> entityTypeToEmptyFlagMap) {
        Class<?>[] touchedByRuleTypes = entityTypeToRuleReferredEntitiesMap.get(entityType);

        if (touchedByRuleTypes == null) {
            IList<IEntityPermissionRule<?>> entityPermissionRules = entityPermissionRuleProvider.getEntityPermissionRules(entityType);
            IPrefetchConfig prefetchConfig = prefetchHelper.createPrefetch();
            for (IEntityPermissionRule entityPermissionRule : entityPermissionRules) {
                entityPermissionRule.buildPrefetchConfig(entityType, prefetchConfig);
            }
            IPrefetchHandle prefetchHandle = prefetchConfig.build();
            ILinkedMap<Class<?>, PrefetchPath[]> entityTypeToPrefetchSteps = ((PrefetchHandle) prefetchHandle).getEntityTypeToPrefetchSteps();

            HashSet<Class<?>> touchedByRuleTypesSet = HashSet.<Class<?>>create(entityTypeToPrefetchSteps.size());
            for (Entry<Class<?>, PrefetchPath[]> prefetchEntry : entityTypeToPrefetchSteps) {
                Class<?> entityTypeOfPrefetch = prefetchEntry.getKey();
                touchedByRuleTypesSet.add(entityTypeOfPrefetch);

                addTypesOfCachePath(prefetchEntry.getValue(), touchedByRuleTypesSet);
            }
            touchedByRuleTypes = touchedByRuleTypesSet.toArray(Class.class);
            entityTypeToRuleReferredEntitiesMap.put(entityType, touchedByRuleTypes);
        }
        for (Class<?> entityTypeTouchedByRule : touchedByRuleTypes) {
            if (!isDataChangeEmpty(dataChange, entityTypeTouchedByRule, entityTypeToDataChangeMap, entityTypeToEmptyFlagMap)) {
                // if 'entityTypeTouchedByRule' has changes which may be read by the any rule of the current
                // 'entityType':
                // the current permissions for 'entityType' have to be fully re-evaluated because of the
                // "foreign" change
                return true;
            }
        }
        return false;
    }

    @Override
    public <R> R executeWithoutPermissionGroupUpdate(CheckedSupplier<R> runnable) {
        var dataChangeHandlingActive = dataChangeHandlingActiveTL.get();
        dataChangeHandlingActiveTL.set(Boolean.FALSE);
        try {
            return CheckedSupplier.invoke(runnable);
        } finally {
            if (dataChangeHandlingActive != null) {
                dataChangeHandlingActiveTL.set(Boolean.FALSE);
            } else {
                dataChangeHandlingActiveTL.remove();
            }
        }
    }

    @Override
    @PersistenceContext(PersistenceContextType.REQUIRED)
    public void fillEmptyPermissionGroups() {
        updatePermissionGroups(null);
    }

    @Override
    @PersistenceContext(PersistenceContextType.REQUIRED)
    public void updatePermissionGroups(IDataChange dataChange) {
        if (!securityActive) {
            return;
        }
        if (userResolver == null) {
            return;
        }
        if (Boolean.FALSE.equals(dataChangeHandlingActiveTL.get())) {
            return;
        }
        if (metaDataAvailableSet.isEmpty()) {
            return;
        }
        long start = System.currentTimeMillis();
        ISecurityScope[] securityScopes = new ISecurityScope[] {
                new ISecurityScope() {
                    @Override
                    public String getName() {
                        return "dummy";
                    }
                }
        };

        var rollback = StateRollback.chain(chain -> {
            chain.append(securityScopeProvider.pushSecurityScopes(securityScopes));
            chain.append(securityActivation.pushWithoutFiltering());
        });
        try {
            var pgUpdated = updatePermissionGroupsIntern(dataChange);
            if (pgUpdated.booleanValue() && log.isDebugEnabled()) {
                long spent = System.currentTimeMillis() - start;
                log.debug(spent + "ms");
            }
        } finally {
            rollback.rollback();
        }
    }

    protected Boolean updatePermissionGroupsIntern(IDataChange dataChange) {
        var entityToPgUpdateMap = createPgUpdateMap(dataChange);
        if (entityToPgUpdateMap.isEmpty()) {
            return Boolean.FALSE;
        }
        buildPermissionGroupMap(entityToPgUpdateMap, dataChange != null);
        if (entityToPgUpdateMap.isEmpty()) {
            return Boolean.FALSE;
        }
        multithreadingHelper.invokeAndWait(entityToPgUpdateMap, entry -> {
            PgUpdateEntry pgUpdateEntry = entry.getValue();
            IPermissionGroup permissionGroup = pgUpdateEntry.getPermissionGroup();
            ITableMetaData table = permissionGroup.getTargetTable();
            IList<IObjRef> objRefs;
            switch (pgUpdateEntry.getUpdateType()) {
                case SELECTED_ROW: {
                    objRefs = loadSelectedObjRefs(pgUpdateEntry);
                    break;
                }
                case EACH_ROW: {
                    objRefs = loadAllObjRefsOfEntityTable(table, pgUpdateEntry);
                    break;
                }
                default:
                    throw RuntimeExceptionUtil.createEnumNotSupportedException(pgUpdateEntry.getUpdateType());
            }
            ArrayList<IObjRef> permissionObjRefs = new ArrayList<>(objRefs.size());
            for (int a = objRefs.size(); a-- > 0; ) {
                permissionObjRefs.add(new ObjRef(IPermissionGroup.class, ObjRef.PRIMARY_KEY_INDEX, null, null));
            }
            primaryKeyProvider.acquireIds(permissionGroup.getTable(), permissionObjRefs);
            ArrayList<Object> permissionGroupIds = new ArrayList<>(permissionObjRefs.size());
            for (int a = 0, size = permissionObjRefs.size(); a < size; a++) {
                permissionGroupIds.add(permissionObjRefs.get(a).getId());
            }
            updateEntityRows(objRefs, permissionGroupIds, permissionGroup, table);

            pgUpdateEntry.setObjRefs(objRefs);
            pgUpdateEntry.setPermissionGroupIds(permissionGroupIds);
            if (log.isDebugEnabled()) {
                log.debug("updated " + objRefs.size() + " entities of type '" + table.getEntityType().getName() + "'");
            }
        });

        String[] allSids = getAllSids();
        ISecurityScope[] securityScopes = securityScopeProvider.getSecurityScopes();

        IAuthentication[] authentications = new IAuthentication[allSids.length];
        final IAuthorization[] authorizations = new IAuthorization[allSids.length];

        for (int a = allSids.length; a-- > 0; ) {
            String sid = allSids[a];
            authentications[a] = new DefaultAuthentication(sid, "dummyPass".toCharArray(), PasswordType.PLAIN);
            authorizations[a] = mockAuthorization(sid, securityScopes);
        }
        ArrayList<IObjRef> allObjRefs = new ArrayList<>();
        for (Entry<Class<?>, PgUpdateEntry> entry : entityToPgUpdateMap) {
            PgUpdateEntry pgUpdateEntry = entry.getValue();
            pgUpdateEntry.setStartIndexInAllObjRefs(allObjRefs.size());
            allObjRefs.addAll(pgUpdateEntry.getObjRefs());
        }
        final IPrivilege[][] allPrivilegesOfAllUsers = evaluateAllPrivileges(allObjRefs, authentications, authorizations);

        multithreadingHelper.invokeAndWait(entityToPgUpdateMap, entry -> insertPermissionGroupsForUsers(entry.getValue(), authorizations, allPrivilegesOfAllUsers));
        return Boolean.TRUE;
    }

    protected IQuery<?> getAllEntitiesQuery(Class<?> entityType) {
        IQuery<?> allEntitiesQuery = entityTypeToAllEntitiesQuery.get(entityType);
        if (allEntitiesQuery != null) {
            return allEntitiesQuery;
        }
        allEntitiesQuery = queryBuilderFactory.create(entityType).build();
        entityTypeToAllEntitiesQuery.put(entityType, allEntitiesQuery);
        return allEntitiesQuery;
    }

    protected IList<IObjRef> loadSelectedObjRefs(PgUpdateEntry pgUpdateEntry) {
        IObjRefFactory objRefFactory = this.objRefFactory;
        IDataChange dataChange = pgUpdateEntry.getDataChange();
        List<IDataChangeEntry> inserts = dataChange.getInserts();
        List<IDataChangeEntry> updates = dataChange.getUpdates();
        ArrayList<IObjRef> objRefs = new ArrayList<>(inserts.size() + updates.size());
        for (int a = inserts.size(); a-- > 0; ) {
            IDataChangeEntry dataChangeEntry = inserts.get(a);
            objRefs.add(objRefFactory.createObjRef(dataChangeEntry.getEntityType(), dataChangeEntry.getIdNameIndex(), dataChangeEntry.getId(), dataChangeEntry.getVersion()));
        }
        for (int a = updates.size(); a-- > 0; ) {
            IDataChangeEntry dataChangeEntry = updates.get(a);
            objRefs.add(objRefFactory.createObjRef(dataChangeEntry.getEntityType(), dataChangeEntry.getIdNameIndex(), dataChangeEntry.getId(), dataChangeEntry.getVersion()));
        }
        return objRefs;
    }

    protected IList<IObjRef> loadAllObjRefsOfEntityTable(ITableMetaData table, PgUpdateEntry pgUpdateEntry) {
        Class<?> entityType = table.getEntityType();
        IQuery<?> allEntitiesQuery = getAllEntitiesQuery(entityType);

        Class<?> idType = table.getIdField().getFieldType();

        IConversionHelper conversionHelper = this.conversionHelper;
        IVersionCursor versionCursor = allEntitiesQuery.retrieveAsVersions(false);
        try {
            IPreparedObjRefFactory prepareObjRefFactory = objRefFactory.prepareObjRefFactory(entityType, ObjRef.PRIMARY_KEY_INDEX);
            ArrayList<IObjRef> objRefs = new ArrayList<>();
            for (IVersionItem item : versionCursor) {
                Object id = conversionHelper.convertValueToType(idType, item.getId());
                objRefs.add(prepareObjRefFactory.createObjRef(id, item.getVersion()));
            }
            return objRefs;
        } finally {
            versionCursor.dispose();
        }
    }

    protected PreparedStatement buildInsertPermissionGroupStm(IPermissionGroup permissionGroup) throws SQLException {
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

    protected PreparedStatement buildUpdateEntityRowStm(IPermissionGroup permissionGroup, ITableMetaData table) throws SQLException {
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

    protected String[] getAllSids() {
        List<? extends IUser> allUsers = getAllUsersQuery().retrieve();

        String[] allSids = new String[allUsers.size()];
        for (int a = allUsers.size(); a-- > 0; ) {
            IUser user = allUsers.get(a);
            allSids[a] = userIdentifierProvider.getSID(user);
        }
        return allSids;
    }

    protected void buildPermissionGroupMap(IMap<Class<?>, PgUpdateEntry> entityToPgUpdateMap, boolean triggeredByDCE) {
        boolean debugEnabled = log.isDebugEnabled();
        IThreadLocalObjectCollector objectCollector = this.objectCollector.getCurrent();
        StringBuilder sb = debugEnabled ? objectCollector.create(StringBuilder.class) : null;
        try {
            if (debugEnabled) {
                sb.append("PermissionGroup updates");
                if (triggeredByDCE) {
                    sb.append(" triggered by DCE:");
                } else {
                    sb.append(" full rebuild:");
                }
            }
            IList<Class<?>> entityTypes = entityToPgUpdateMap.keyList();
            int maxLength = 0;
            if (debugEnabled) {
                Collections.sort(entityTypes, new Comparator<Class<?>>() {
                    @Override
                    public int compare(Class<?> o1, Class<?> o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
                for (Class<?> entityType : entityTypes) {
                    maxLength = Math.max(entityType.getName().length(), maxLength);
                }
            }
            for (Class<?> entityType : entityTypes) {
                PgUpdateEntry pgUpdateEntry = entityToPgUpdateMap.get(entityType);

                if (PermissionGroupUpdateType.NOTHING.equals(pgUpdateEntry.getUpdateType())) {
                    entityToPgUpdateMap.remove(entityType);
                    continue;
                }
                if (debugEnabled) {
                    sb.append("\n\t");
                    String name = entityType.getName();
                    int length = name.length();
                    sb.append(name).append(": ");
                    while (length < maxLength) {
                        length++;
                        sb.append(' ');
                    }
                    sb.append(pgUpdateEntry.getUpdateType().name());
                }
            }
            if (!entityToPgUpdateMap.isEmpty() && debugEnabled) {
                log.debug(sb);
            }
        } finally {
            if (debugEnabled) {
                objectCollector.dispose(sb);
            }
        }
    }

    protected IPrivilege[][] evaluateAllPrivileges(List<IObjRef> allObjRefs, IAuthentication[] authentications, IAuthorization[] authorizations) {
        IPrivilegeProvider privilegeProvider = this.privilegeProvider;
        ISecurityContext securityContext = securityContextHolder.getCreateContext();
        IAuthentication oldAuthentication = securityContext.getAuthentication();
        IAuthorization oldAuthorization = securityContext.getAuthorization();
        try {
            boolean oldIgnoreInvalidUser = SecurityFilterInterceptor.setIgnoreInvalidUser(true);
            try {
                IPrivilege[][] allPrivilegesOfAllUsers = new IPrivilege[authentications.length][];
                for (int a = authentications.length; a-- > 0; ) {
                    IAuthentication authentication = authentications[a];
                    IAuthorization authorization = authorizations[a];
                    securityContext.setAuthentication(authentication);
                    securityContext.setAuthorization(authorization);

                    allPrivilegesOfAllUsers[a] = privilegeProvider.getPrivilegesByObjRef(allObjRefs).getPrivileges();
                }
                return allPrivilegesOfAllUsers;
            } finally {
                SecurityFilterInterceptor.setIgnoreInvalidUser(oldIgnoreInvalidUser);
            }
        } finally {
            securityContext.setAuthentication(oldAuthentication);
            securityContext.setAuthorization(oldAuthorization);
        }
    }

    protected void insertPermissionGroupsForUsers(PgUpdateEntry pgUpdateEntry, IAuthorization[] authorizations, IPrivilege[][] allPrivilegesOfAllUsers) throws Exception {
        IList<Object> permissionGroupIds = pgUpdateEntry.getPermissionGroupIds();
        int startIndexInAllObjRefs = pgUpdateEntry.getStartIndexInAllObjRefs();

        PreparedStatement insertPermissionGroupPstm = buildInsertPermissionGroupStm(pgUpdateEntry.getPermissionGroup());
        try {
            ParameterMetaData pmd = insertPermissionGroupPstm.getParameterMetaData();
            int readIndex = 3, updateIndex = 4, deleteIndex = 5;
            int readType = pmd.getParameterType(readIndex);
            int updateType = pmd.getParameterType(updateIndex);
            int deleteType = pmd.getParameterType(deleteIndex);

            for (int b = permissionGroupIds.size(); b-- > 0; ) {
                Object permissionGroupId = permissionGroupIds.get(b);

                insertPermissionGroupPstm.setObject(2, permissionGroupId);

                for (int a = authorizations.length; a-- > 0; ) {
                    IAuthorization authorization = authorizations[a];
                    IPrivilege[] allPrivileges = allPrivilegesOfAllUsers[a];

                    insertPermissionGroupPstm.setObject(1, authorization.getSID());

                    IPrivilege privilege = allPrivileges[startIndexInAllObjRefs + b];

                    int readAllowed = privilege == null || privilege.isReadAllowed() ? 1 : 0;
                    int updateAllowed = privilege == null || privilege.isUpdateAllowed() ? 1 : 0;
                    int deleteAllowed = privilege == null || privilege.isDeleteAllowed() ? 1 : 0;

                    setBit(insertPermissionGroupPstm, readIndex, readType, readAllowed);
                    setBit(insertPermissionGroupPstm, updateIndex, updateType, updateAllowed);
                    setBit(insertPermissionGroupPstm, deleteIndex, deleteType, deleteAllowed);
                    insertPermissionGroupPstm.addBatch();
                }
            }
            insertPermissionGroupPstm.executeBatch();
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        } finally {
            insertPermissionGroupPstm.close();
        }
    }

    private void setBit(PreparedStatement pstm, int index, int type, int bit) throws SQLException {
        switch (type) {
            case Types.BIT: {
                pstm.setBoolean(index, bit != 0);
                break;
            }
            default:
                pstm.setInt(index, bit);
        }
    }

    protected void updateEntityRows(IList<IObjRef> objRefs, IList<Object> permissionGroupIds, IPermissionGroup permissionGroup, ITableMetaData table) throws Exception {
        IConversionHelper conversionHelper = this.conversionHelper;
        Class<?> idType = table.getIdField().getFieldType();
        PreparedStatement updateEntityRowPstm = buildUpdateEntityRowStm(permissionGroup, table);
        try {
            for (int a = objRefs.size(); a-- > 0; ) {
                IObjRef objRef = objRefs.get(a);

                Object persistentEntityId = conversionHelper.convertValueToType(idType, objRef.getId());
                updateEntityRowPstm.setObject(1, permissionGroupIds.get(a));
                updateEntityRowPstm.setObject(2, persistentEntityId);

                updateEntityRowPstm.addBatch();
            }
            updateEntityRowPstm.executeBatch();
        } catch (SQLException e) {
            throw RuntimeExceptionUtil.mask(e);
        } finally {
            updateEntityRowPstm.close();
        }
    }

    protected IAuthorization mockAuthorization(final String sid, final ISecurityScope[] securityScopes) {
        if (authorizationManager != null) {
            IAuthorization authorization = authorizationManager.authorize(sid, securityScopes, new AuthenticationResult(sid, false, false, false, false));
            if (authorization == null) {
                throw new IllegalStateException();
            }
            return authorization;
        }
        return new DefaultAuthorization(sid, securityScopes, CallPermission.FORBIDDEN, System.currentTimeMillis(), new AuthenticationResult(sid, false, false, false, false));
    }
}
