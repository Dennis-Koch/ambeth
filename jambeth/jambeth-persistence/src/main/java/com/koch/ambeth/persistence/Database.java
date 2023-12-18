package com.koch.ambeth.persistence;

/*-
 * #%L
 * jambeth-persistence
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

import com.koch.ambeth.ioc.DefaultExtendableContainer;
import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.api.IContextProvider;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.IDatabaseDisposeHook;
import com.koch.ambeth.persistence.api.IDatabaseMetaData;
import com.koch.ambeth.persistence.api.IDatabasePool;
import com.koch.ambeth.persistence.api.ILink;
import com.koch.ambeth.persistence.api.ISavepoint;
import com.koch.ambeth.persistence.api.ITable;
import com.koch.ambeth.persistence.api.database.IDatabaseProvider;
import com.koch.ambeth.persistence.parallel.IModifyingDatabase;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import com.koch.ambeth.util.proxy.ProxyUtil;
import com.koch.ambeth.util.state.IStateRollback;

import java.util.List;
import java.util.Map;

public class Database implements IDatabase, IInitializingBean, IStartingBean, IDisposableBean {
    protected final HashMap<String, ITable> nameToTableDict = new HashMap<>();
    protected final HashMap<Class<?>, ITable> typeToTableDict = new HashMap<>();
    protected final HashMap<Class<?>, ITable> typeToArchiveTableDict = new HashMap<>();
    protected final HashMap<TablesMapKey, List<ILink>> tablesToLinkDict = new HashMap<>();
    protected final HashMap<Class<?>, IEntityHandler> typeToEntityHandler = new HashMap<>();
    protected final DefaultExtendableContainer<IDatabaseDisposeHook> databaseDisposeHooks = new DefaultExtendableContainer<>(IDatabaseDisposeHook.class, "databaseDisposeHook");
    protected final ArrayList<ITable> tables = new ArrayList<>();
    protected final List<ILink> links = new ArrayList<>();
    @Autowired
    protected IContextProvider contextProvider;
    @Autowired
    protected IDatabaseProvider databaseProvider;
    @Autowired
    protected IDatabaseMetaData metaData;
    @Autowired(optional = true)
    protected IModifyingDatabase modifyingDatabase;
    @Autowired
    protected IThreadLocalObjectCollector objectCollector;
    @Autowired(optional = true)
    protected IDatabasePool pool;
    @Autowired
    protected IServiceContext serviceContext;
    protected long sessionId;
    @LogInstance
    private ILogger log;

    @Override
    public void afterPropertiesSet() throws Throwable {
        // Intended blank
    }

    @Override
    public void afterStarted() {
        // Intended blank
    }

    @Override
    public IDatabaseMetaData getMetaData() {
        return metaData;
    }

    @Override
    public void registerDisposeHook(IDatabaseDisposeHook disposeHook) {
        databaseDisposeHooks.register(disposeHook);
    }

    @Override
    public void unregisterDisposeHook(IDatabaseDisposeHook disposeHook) {
        databaseDisposeHooks.unregister(disposeHook);
    }

    @Override
    public boolean isDisposed() {
        return serviceContext.isDisposed();
    }

    @Override
    public <T> T getAutowiredBeanInContext(Class<T> autowiredType) {
        return serviceContext.getService(autowiredType, false);
    }

    @Override
    public <T> T getNamedBeanInContext(String beanName, Class<T> expectedType) {
        return serviceContext.getService(beanName, expectedType, false);
    }

    @Override
    public IContextProvider getContextProvider() {
        return contextProvider;
    }

    @Override
    public IDatabasePool getPool() {
        return pool;
    }

    public IServiceContext getServiceProvider() {
        return serviceContext;
    }

    public Map<Class<?>, IEntityHandler> getTypeToEntityHandler() {
        return typeToEntityHandler;
    }

    @Override
    public void acquired(boolean readOnly) {
        if (modifyingDatabase != null) {
            modifyingDatabase.setModifyingAllowed(!readOnly);
        }
        contextProvider.acquired();
        contextProvider.setCurrentTime(Long.valueOf(System.currentTimeMillis()));
    }

    @Override
    public void flushAndRelease() {
        var databaseLocal = databaseProvider.getDatabaseLocal();
        var currentDatabase = databaseLocal.get();
        currentDatabase = ProxyUtil.getProxiedBean(currentDatabase);
        if (this == currentDatabase) {
            databaseLocal.remove();
        }
        try {
            flush();
        } finally {
            clear();
            if (pool != null) {
                pool.releaseDatabase(this, true);
            }
        }
    }

    @Override
    public void release(boolean errorOccured) {
        var databaseLocal = databaseProvider.getDatabaseLocal();
        var currentDatabase = databaseLocal.get();
        currentDatabase = ProxyUtil.getProxiedBean(currentDatabase);
        if (this == currentDatabase) {
            databaseLocal.remove();
        }
        clear();
        try {
            if (modifyingDatabase != null) {
                modifyingDatabase.setModifyingAllowed(true);
            }
        } catch (Throwable e) {
            // intended blank
        }
        if (pool != null) {
            pool.releaseDatabase(this, !errorOccured);
        }
    }

    @Override
    public void destroy() throws Throwable {
        var databaseLocal = databaseProvider.getDatabaseLocal();
        var currentDatabase = databaseLocal.get();
        currentDatabase = ProxyUtil.getProxiedBean(currentDatabase);
        if (this == currentDatabase) {
            databaseLocal.remove();
        }
        clear();
        for (IDatabaseDisposeHook disposeHook : databaseDisposeHooks.getExtensionsShared()) {
            disposeHook.databaseDisposed(this);
        }
    }

    protected void clear() {
        contextProvider.clear();
    }

    @Override
    public void dispose() {
        serviceContext.dispose();
    }

    @Override
    public IDatabase getCurrent() {
        return this;
    }

    @Override
    public long getSessionId() {
        return sessionId;
    }

    @Override
    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String getName() {
        return getMetaData().getName();
    }

    @Override
    public String[] getSchemaNames() {
        return getMetaData().getSchemaNames();
    }

    @Override
    public List<ITable> getTables() {
        return tables;
    }

    @Override
    public List<ILink> getLinks() {
        return links;
    }

    @Override
    public ITable getTableByName(String tableName) {
        return nameToTableDict.get(tableName);
    }

    @Override
    public List<ILink> getLinksByTables(ITable table1, ITable table2) {
        var tablesMapKey = new TablesMapKey(table1.getMetaData(), table2.getMetaData());
        return tablesToLinkDict.get(tablesMapKey);
    }

    @Override
    public ITable getTableByType(Class<?> entityType) {
        ParamChecker.assertParamNotNull(entityType, "entityType");
        var table = typeToTableDict.get(entityType);
        if (table == null) {
            var tableMetadata = metaData.getTableByType(entityType);
            if (tableMetadata != null) {
                table = nameToTableDict.get(tableMetadata.getName());
            }
            if (table == null) {
                throw new IllegalStateException("No table found for entity type '" + entityType.getName() + "'");
            } else {
                table.updateLinks();
                typeToTableDict.put(entityType, table);
            }
        }
        return table;
    }

    @Override
    public ITable getArchiveTableByType(Class<?> entityType) {
        return typeToArchiveTableDict.get(entityType);
    }

    @Override
    public void flush() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void revert() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void revert(ISavepoint savepoint) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean test() {
        return true;
    }

    @Override
    public ISavepoint setSavepoint() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void releaseSavepoint(ISavepoint savepoint) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void rollback(ISavepoint savepoint) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public IStateRollback disableConstraints() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void registerNewTable(String tableName) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
