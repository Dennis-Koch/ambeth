package com.koch.ambeth.persistence.jdbc.connection;

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

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.jdbc.JdbcUtil;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.util.IClassLoaderProvider;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import jakarta.persistence.PersistenceException;
import lombok.SneakyThrows;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionFactory extends AbstractConnectionFactory {
    private static final Method m_getConnection;

    static {
        var typeIsOsgiLoaded = false;
        try {
            var frameworkUtilClass = Thread.currentThread().getContextClassLoader().loadClass("org.osgi.framework.FrameworkUtil");
            var bundle = frameworkUtilClass.getMethod("getBundle", Class.class).invoke(ConnectionFactory.class);
            typeIsOsgiLoaded = bundle != null;
        } catch (ClassNotFoundException e) {
            // no OSGi in classpath
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            // should never happen if OSGi is in classpath
            throw new IllegalStateException(e);
        }
        if (typeIsOsgiLoaded) {
            m_getConnection = ReflectUtil.getDeclaredMethod(false, DriverManager.class, Connection.class, "getConnection", String.class, java.util.Properties.class, Class.class);
            m_getConnection.setAccessible(true);
        } else {
            m_getConnection = null;
        }
    }

    @Autowired
    protected IClassLoaderProvider classLoaderProvider;
    @Autowired
    protected IDatabaseConnectionUrlProvider databaseConnectionUrlProvider;
    @Property(name = PersistenceJdbcConfigurationConstants.DatabaseUser)
    protected String userName;
    @Property(name = PersistenceJdbcConfigurationConstants.DatabasePass)
    protected String userPass;
    @Property(name = PersistenceJdbcConfigurationConstants.DatabaseRetryConnect, defaultValue = "1")
    protected int tryCount;
    @LogInstance
    private ILogger log;

    @Override
    protected Connection createIntern() throws Exception {
        var connectionUrl = databaseConnectionUrlProvider.getConnectionUrl();
        try {
            if (log.isInfoEnabled()) {
                log.info("Creating jdbc connection to '" + connectionUrl + "' with user='" + userName + "'");
            }
            var success = false;
            var connection = callDriverManagerGetConnection(connectionUrl, userName, userPass);
            try {
                if (log.isDebugEnabled()) {
                    log.debug("[" + System.identityHashCode(connection) + "] created connection");
                }
                var dbmd = connection.getMetaData();
                if (dbmd.supportsTransactionIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ)) {
                    connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                } else if (dbmd.supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_COMMITTED)) {
                    connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                } else if (dbmd.supportsTransactionIsolationLevel(Connection.TRANSACTION_SERIALIZABLE)) {
                    connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                } else {
                    throw new PersistenceException(
                            "At least TRANSACTION_SERIALIZABLE is required from a JDBC database provider as a supported transaction isolation level: '" + connection + "' does not provide this");
                }
                success = true;
                return connection;
            } finally {
                if (!success) {
                    JdbcUtil.close(connection);
                }
            }
        } catch (Throwable e) {
            throw RuntimeExceptionUtil.mask(e, "Error occured while connecting to '" + connectionUrl + "' with user='" + userName + "'");
        }
    }

    @SneakyThrows
    protected Connection callDriverManagerGetConnection(String connectionUrl, String userName, String userPass) {
        if (m_getConnection == null) {
            var rollback = classLoaderProvider.pushClassLoader();
            try {
                return DriverManager.getConnection(connectionUrl, userName, userPass);
            } finally {
                rollback.rollback();
            }
        }
        // OSGi mode: it is necessary to call the private method DriverManager#getConnection(String,java.util.Properties,Class) via reflection when the current classloader is used as the current
        // thread's context classloader by calling IClassLoaderProvider#pushClassLoader. this is needed for some OSGi scenarios where the class loader of this "ConnectionFactory" class may not be
        // allowed to resolve the registered JDBC driver from another classloader. That is why the latter case will fail when calling the "official" public DriverManager#getConnection(...) overloads.

        var info = new java.util.Properties();

        info.put("user", userName);
        info.put("password", userPass);

        var rollback = classLoaderProvider.pushClassLoader();
        try {
            return (Connection) m_getConnection.invoke(null, connectionUrl, info, null);
        } finally {
            rollback.rollback();
        }
    }
}
