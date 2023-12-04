package com.koch.ambeth.datachange.persistence.services;

/*-
 * #%L
 * jambeth-datachange-persistence
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

import com.koch.ambeth.datachange.persistence.model.DataChangeEventBO;
import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.proxy.MergeContext;
import com.koch.ambeth.merge.proxy.PersistenceContext;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.util.annotation.NoProxy;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.util.List;

@PersistenceContext
@MergeContext
public class DataChangeEventDAO implements IDataChangeEventDAO, IStartingBean {
    @Autowired
    protected Connection connection;

    @Autowired
    protected IQueryBuilderFactory qbf;

    protected IQuery<DataChangeEventBO> retrieveAll;

    @Override
    public void afterStarted() throws Throwable {
        retrieveAll = qbf.create(DataChangeEventBO.class).build();
    }

    @Override
    public void save(DataChangeEventBO dataChangeEvent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<DataChangeEventBO> retrieveAll() {
        return retrieveAll.retrieve();
    }

    @SneakyThrows
    @Override
    @NoProxy
    public void removeBefore(long time) {
        try (var stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM \"DATA_CHANGE_ENTRY\" WHERE \"INSERT_PARENT\" IN (SELECT \"ID\" FROM \"DATA_CHANGE_EVENT\" WHERE \"CHANGE_TIME\" < " + time +
                    ") OR \"UPDATE_PARENT\" IN (SELECT \"ID\" FROM \"DATA_CHANGE_EVENT\" WHERE \"CHANGE_TIME\" < " + time +
                    ") OR \"DELETE_PARENT\" IN (SELECT \"ID\" FROM \"DATA_CHANGE_EVENT\" WHERE \"CHANGE_TIME\" < " + time + ")");
            stmt.execute("DELETE FROM \"DATA_CHANGE_EVENT\" WHERE \"CHANGE_TIME\" < " + time);
        }
    }
}
