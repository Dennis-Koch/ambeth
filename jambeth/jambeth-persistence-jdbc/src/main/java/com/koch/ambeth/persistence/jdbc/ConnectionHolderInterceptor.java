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

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.threadlocal.Forkable;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.persistence.IConnectionHolder;
import com.koch.ambeth.util.proxy.AbstractSimpleInterceptor;
import com.koch.ambeth.util.proxy.MethodProxy;
import com.koch.ambeth.util.threading.SensitiveThreadLocal;
import com.koch.ambeth.util.transaction.ILightweightTransaction;

import java.lang.reflect.Method;
import java.sql.Connection;

public class ConnectionHolderInterceptor extends AbstractSimpleInterceptor implements IConnectionHolder, IThreadLocalCleanupBean {
    public static final String P_CONNECTION = "Connection";
    @Forkable
    protected final ThreadLocal<Connection> connectionTL = new SensitiveThreadLocal<>();

    @Autowired
    protected ILightweightTransaction transaction;

    @Override
    public void cleanupThreadLocal() {
        if (connectionTL.get() != null) {
            throw new IllegalStateException("At this point the thread-local connection has to be already cleaned up gracefully");
        }
    }

    @Override
    protected Object interceptIntern(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        var connection = getConnection();
        if (connection == null) {
            // maybe we have a lazy transaction. if so we now ensure a transactional state from this moment on and try to resolve the DB handle again
            if (transaction.initializeTransactionIfLazyActive()) {
                connection = getConnection();
            }
            if (connection == null) {
                throw new IllegalStateException("No connection currently applied. This often occurs if a " + Connection.class.getName() + "-bean is used without scoping the call through the " +
                        ILightweightTransaction.class.getName() + "-bean");
            }
        }
        return proxy.invoke(connection, args);
    }

    @Override
    public Connection getConnection() {
        return connectionTL.get();
    }

    @Override
    public void setConnection(Connection connection) {
        var oldConnection = connectionTL.get();
        if (oldConnection != null && connection != null && oldConnection != connection) {
            throw new IllegalStateException("Thread-local connection instance already applied!. This is a fatal state");
        }
        if (connection == null) {
            connectionTL.remove();
        } else {
            connectionTL.set(connection);
        }
    }
}
