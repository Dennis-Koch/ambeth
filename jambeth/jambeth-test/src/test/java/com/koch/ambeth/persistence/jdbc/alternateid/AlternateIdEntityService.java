package com.koch.ambeth.persistence.jdbc.alternateid;

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

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.proxy.PersistenceContext;
import com.koch.ambeth.persistence.IServiceUtil;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.service.proxy.Service;
import com.koch.ambeth.util.collections.ArrayList;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service(IAlternateIdEntityService.class)
@PersistenceContext
public class AlternateIdEntityService implements IAlternateIdEntityService {
    @Autowired
    protected IDatabase database;

    @Autowired
    protected IServiceUtil serviceUtil;

    @Override
    public AlternateIdEntity getAlternateIdEntityByName(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<AlternateIdEntity> getAlternateIdEntitiesByNamesReturnCollection(String... names) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<AlternateIdEntity> getAlternateIdEntitiesByNamesReturnList(String... names) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<AlternateIdEntity> getAlternateIdEntitiesByNamesReturnSet(String... names) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AlternateIdEntity[] getAlternateIdEntitiesByNamesReturnArray(String... names) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AlternateIdEntity getAlternateIdEntityByNames(Collection<String> names) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AlternateIdEntity getAlternateIdEntityByNames(List<String> names) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AlternateIdEntity getAlternateIdEntityByNames(String... names) {
        throw new UnsupportedOperationException();
    }

    public void test(String name) {
        var aieTable = database.getTableByType(AlternateIdEntity.class);
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
    public void updateAlternateIdEntity(AlternateIdEntity alternateIdEntity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAlternateIdEntity(AlternateIdEntity alternateIdEntity) {
        throw new UnsupportedOperationException();
    }
}
