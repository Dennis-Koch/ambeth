package com.koch.ambeth.persistence.database;

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

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.threadlocal.Forkable;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.IDatabasePool;
import com.koch.ambeth.persistence.api.database.IDatabaseProvider;
import com.koch.ambeth.util.proxy.ITargetProvider;
import com.koch.ambeth.util.threading.SensitiveThreadLocal;
import com.koch.ambeth.util.transaction.ILightweightTransaction;
import jakarta.persistence.PersistenceException;

public class DatabaseProvider implements ITargetProvider, IDatabaseProvider, IThreadLocalCleanupBean {
    @Forkable
    protected final SensitiveThreadLocal<IDatabase> databaseTL = new SensitiveThreadLocal<>();
    @Autowired
    protected IDatabasePool databasePool;

    @Autowired
    protected ILightweightTransaction transaction;

    @Property(defaultValue = "PERSISTENT")
    protected DatabaseType databaseType;

    @Override
    public void cleanupThreadLocal() {
        databaseTL.remove();
    }

    @Override
    public IDatabase tryGetInstance() {
        return databaseTL.get();
    }

    @Override
    public ThreadLocal<IDatabase> getDatabaseLocal() {
        return databaseTL;
    }

    @Override
    public IDatabase acquireInstance() {
        return acquireInstance(false);
    }

    @Override
    public IDatabase acquireInstance(boolean readonly) {
        var database = tryGetInstance();
        if (database != null) {
            throw new PersistenceException("Instance already acquired. Maybe you must not acquire instances at your current application scope?");
        }
        database = databasePool.acquireDatabase(readonly);
        databaseTL.set(database);
        return database;
    }

    public IDatabase getInstance() {
        var database = tryGetInstance();
        if (database != null) {
            return database;
        }
        // maybe we have a lazy transaction. if so we now ensure a transactional state and try to resolve the DB handle again
        if (transaction.isLazyActive()) {
            // resolve database handle now "eagerly" again
            transaction.runInTransaction(() -> {
            });
        }
        database = tryGetInstance();
        if (database != null) {
            return database;
        }
        throw new PersistenceException(
                "No instance acquired, yet. It should have been done at this point!" + " If this exception happens within a service request from a client your service implementing method" +
                        " might not be specified as virtual. A service method virtual must be to allow dynamic proxying" + " for database session operations");
    }

    @Override
    public Object getTarget() {
        return getInstance();
    }
}
