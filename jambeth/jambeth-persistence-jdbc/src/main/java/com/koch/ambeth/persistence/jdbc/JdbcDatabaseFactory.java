package com.koch.ambeth.persistence.jdbc;

/*-
 * #%L
 * jambeth-persistence-jdbc
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
import com.koch.ambeth.ioc.IBeanContextAware;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.persistence.ContextProvider;
import com.koch.ambeth.persistence.IConnectionHolder;
import com.koch.ambeth.persistence.api.IContextProvider;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.IDatabaseMetaData;
import com.koch.ambeth.persistence.api.IDatabasePool;
import com.koch.ambeth.persistence.api.database.IDatabaseProvider;
import com.koch.ambeth.persistence.connection.IPreparedConnectionHolder;
import com.koch.ambeth.persistence.database.IDatabaseFactory;
import com.koch.ambeth.persistence.database.IDatabaseMapper;
import com.koch.ambeth.persistence.database.IDatabaseMapperExtendable;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.persistence.parallel.IModifyingDatabase;
import com.koch.ambeth.persistence.parallel.ModifyingDatabase;
import com.koch.ambeth.persistence.util.AlreadyLinkedCache;
import com.koch.ambeth.persistence.util.IAlreadyLinkedCache;
import com.koch.ambeth.security.IAuthorizationChangeListenerExtendable;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class JdbcDatabaseFactory implements IDatabaseFactory, IDatabaseMapperExtendable {
    public static final String ADDITIONAL_MODULES_PROP = "AdditionalModules";
    protected final DefaultExtendableContainer<IDatabaseMapper> databaseMappers = new DefaultExtendableContainer<>(IDatabaseMapper.class, "databaseMapper");
    protected final Lock writeLock = new ReentrantLock();
    @Autowired
    protected IConnectionFactory connectionFactory;
    @Autowired
    protected IConnectionHolder connectionHolder;
    @Autowired
    protected IDatabaseMetaData databaseMetaData;
    @Autowired
    protected IDatabaseProvider databaseProvider;

    @Autowired
    protected IServiceContext serviceContext;
    @Property(name = PersistenceJdbcConfigurationConstants.DatabaseSchemaCacheActive, defaultValue = "false")
    protected boolean schemaCacheActive;
    protected Class<?>[] additionalModules;
    protected transient boolean firstInstance = true;

    public void setAdditionalModules(Class<?>[] additionalModules) {
        this.additionalModules = additionalModules;
    }

    @Override
    public void registerDatabaseMapper(IDatabaseMapper databaseMapper) {
        databaseMappers.register(databaseMapper);
    }

    @Override
    public void unregisterDatabaseMapper(IDatabaseMapper databaseMapper) {
        databaseMappers.unregister(databaseMapper);
    }

    @Override
    public void activate(IDatabase database) {
        var connection = database.getAutowiredBeanInContext(Connection.class);
        connectionFactory.create(connection);
    }

    @SneakyThrows
    @Override
    public void passivate(IDatabase database) {
        var connection = database.getAutowiredBeanInContext(Connection.class);
        connection.close();
    }

    @SneakyThrows
    @Override
    public IDatabase createDatabaseInstance(final IDatabasePool pool) {
        Connection conn = null;
        boolean success = false;
        try {
            conn = connectionFactory.create();

            writeLock.lock();
            try {
                if (firstInstance) {
                    firstInstance = false;

                    var connectionHolder = this.connectionHolder;
                    var oldConnection = connectionHolder.getConnection();
                    if (oldConnection != null) {
                        connectionHolder.setConnection(null);
                    }
                    try {
                        connectionHolder.setConnection(conn);
                        ((JDBCDatabaseMetaData) databaseMetaData).init(conn);
                    } finally {
                        connectionHolder.setConnection(oldConnection);
                    }
                }
            } finally {
                writeLock.unlock();
            }
            var fConn = conn;

            IServiceContext childService;
            var connectionHolder = this.connectionHolder;
            var oldConnection = connectionHolder.getConnection();
            if (oldConnection != null) {
                connectionHolder.setConnection(null);
            }
            try {
                connectionHolder.setConnection(conn);
                childService = serviceContext.createService("jdbc-session", childContextFactory -> {
                    childContextFactory.registerExternalBean(pool).autowireable(IDatabasePool.class);
                    childContextFactory.registerExternalBean(fConn).autowireable(Connection.class);
                    childContextFactory.registerAutowireableBean(IModifyingDatabase.class, ModifyingDatabase.class);
                    childContextFactory.registerAutowireableBean(IAlreadyLinkedCache.class, AlreadyLinkedCache.class);

                    IBeanConfiguration contextProviderBC = childContextFactory.registerAutowireableBean(IContextProvider.class, ContextProvider.class);
                    childContextFactory.link(contextProviderBC).to(IAuthorizationChangeListenerExtendable.class);

                    if (!fConn.isWrapperFor(IPreparedConnectionHolder.class) || !fConn.unwrap(IPreparedConnectionHolder.class).isPreparedConnection()) {
                        childContextFactory.registerBean(ConnectionShutdownBean.class);
                    }
                    childContextFactory.registerExternalBean(databaseProvider).autowireable(IDatabaseProvider.class);

                    childContextFactory.registerBean("database", JDBCDatabaseWrapper.class)//
                                       .autowireable(IDatabase.class);

                    if (additionalModules != null) {
                        for (int a = additionalModules.length; a-- > 0; ) {
                            childContextFactory.registerBean(additionalModules[a]);
                        }
                    }
                });
                success = true;
            } finally {
                connectionHolder.setConnection(oldConnection);
            }
            // Re-bind the LCI to the child context. This is to allow bean injection to subsequently
            // created LogStatements from the child context
            if (conn.isWrapperFor(IBeanContextAware.class)) {
                conn.unwrap(IBeanContextAware.class).setBeanContext(childService);
            }
            var database = childService.getService(IDatabase.class);

            // IDatabaseMapper[] databaseMappers =
            // databaseMapperEC.getListeners();
            //
            // for (IDatabaseMapper databaseMapper : databaseMappers)
            // {
            // databaseMapper.mapFields(database.getDatabase());
            // }
            success = true;
            return database;
        } finally {
            if (!success && conn != null) {
                JdbcUtil.close(conn);
            }
        }
    }
}
