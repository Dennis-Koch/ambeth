package com.koch.ambeth.persistence.jdbc.compositeid;

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

import com.koch.ambeth.informationbus.persistence.setup.SQLData;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.persistence.jdbc.compositeid.models.CompositeIdEntity;
import com.koch.ambeth.persistence.jdbc.compositeid.models.ICompositeIdEntityService;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import org.junit.Test;

import static org.junit.Assert.*;

@TestModule({ CompositeIdTestModule.class })
@TestPropertiesList({
        @TestProperties(name = PersistenceJdbcConfigurationConstants.DatabaseBehaviourStrict, value = "true"),
        @TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/persistence/jdbc/compositeid/orm.xml")
})
@SQLStructure("CompositeId_structure.sql")
@SQLData("CompositeId_data.sql")
public class CompositeIdTest extends AbstractInformationBusWithPersistenceTest {
    @Autowired
    protected ICache cache;

    @Autowired
    protected ICompositeIdEntityService compositeIdEntityService;

    @Autowired
    protected IQueryBuilderFactory queryBuilderFactory;

    @Test
    public void testCreate() {
        var entity = entityFactory.createEntity(CompositeIdEntity.class);
        entity.setAltId1(5);
        entity.setAltId2("6");
        entity.setAltId3(7);
        entity.setAltId4("8");
        entity.setName("zehn tausend");
        compositeIdEntityService.create(entity);
    }

    @Test
    public void testUpdate() {
        String newName = "eins punkt zwei";
        var entity = cache.getObject(CompositeIdEntity.class, 1, "eins");
        assertNotEquals(newName, entity.getName());
        entity.setName(newName);
        compositeIdEntityService.update(entity);

        var actual = cache.getObject(CompositeIdEntity.class, 1, "eins");
        assertEquals(newName, actual.getName());
    }

    @Test
    public void testDelete() {
        var entity = cache.getObject(CompositeIdEntity.class, 1, "eins");
        assertNotNull(entity);

        compositeIdEntityService.delete(entity);

        var actual = cache.getObject(CompositeIdEntity.class, 1, "eins");
        assertNull(actual);
    }

    @Test
    public void testQuery() {
        var qb = queryBuilderFactory.create(CompositeIdEntity.class);
        var query = qb.build(qb.let(qb.property(CompositeIdEntity.PROP_NAME)).isEqualTo(qb.value("name_einszwo1")));
        var result = query.retrieve();
        assertEquals(1, result.size());
        var actual = result.get(0);
        assertEquals(1, actual.getId1());
        assertEquals("einszwo", actual.getId2());
        assertEquals("name_einszwo1", actual.getName());
    }
}
