package com.koch.ambeth.persistence.jdbc.mapping.models;

/*-
 * #%L
 * jambeth-test
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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.merge.proxy.PersistenceContext;
import com.koch.ambeth.persistence.IServiceUtil;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.jdbc.mapping.ISelfReferencingEntityService;
import com.koch.ambeth.service.proxy.Service;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service(ISelfReferencingEntityService.class)
@PersistenceContext
public class SelfReferencingEntityService implements ISelfReferencingEntityService, IInitializingBean {
    protected IDatabase database;

    protected IServiceUtil serviceUtil;

    @Override
    public void afterPropertiesSet() throws Throwable {
        ParamChecker.assertNotNull(database, "database");
        ParamChecker.assertNotNull(serviceUtil, "serviceUtil");
    }

    public void setDatabase(IDatabase database) {
        this.database = database;
    }

    public void setServiceUtil(IServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public SelfReferencingEntity getSelfReferencingEntityByName(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<SelfReferencingEntity> getSelfReferencingEntitiesByNamesReturnCollection(String... names) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<SelfReferencingEntity> getSelfReferencingEntitiesByNamesReturnList(String... names) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<SelfReferencingEntity> getSelfReferencingEntitiesByNamesReturnSet(String... names) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SelfReferencingEntity[] getSelfReferencingEntitiesByNamesReturnArray(String... names) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SelfReferencingEntity getSelfReferencingEntityByNames(Collection<String> names) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SelfReferencingEntity getSelfReferencingEntityByNames(List<String> names) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SelfReferencingEntity getSelfReferencingEntityByNames(String... names) {
        throw new UnsupportedOperationException();
    }

    public void test(String name) {
        var aieTable = database.getTableByType(SelfReferencingEntity.class);
        var names = new ArrayList<String>();
        names.add(name);
        var selectVersion = aieTable.selectVersion(0, names);
        try {
            System.out.println(selectVersion);
        } finally {
            selectVersion.dispose();
        }
    }

    @Override
    public void updateSelfReferencingEntity(SelfReferencingEntity selfReferencingEntity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteSelfReferencingEntity(SelfReferencingEntity selfReferencingEntity) {
        throw new UnsupportedOperationException();
    }
}
