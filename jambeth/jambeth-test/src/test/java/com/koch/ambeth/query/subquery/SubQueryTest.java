package com.koch.ambeth.query.subquery;

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
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.ISubQuery;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptyList;
import com.koch.ambeth.util.collections.HashMap;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/query/subquery/SubQuery_orm.xml")
@SQLStructure("SubQuery_structure.sql")
@SQLData("SubQuery_data.sql")
public class SubQueryTest extends AbstractInformationBusWithPersistenceTest {
    protected IQueryBuilder<EntityA> qb;

    @Override
    public void afterPropertiesSet() throws Throwable {
        super.afterPropertiesSet();

        qb = queryBuilderFactory.create(EntityA.class);
    }

    @Test
    public void testBuildSubQuery() throws Exception {
        ISubQuery<EntityA> subQuery = qb.buildSubQuery();
        assertNotNull(subQuery);

        String[] sqlParts = subQuery.getSqlParts(new HashMap<>(), new ArrayList<>(0), EmptyList.<String>getInstance());
        assertNotNull(sqlParts);
        assertEquals(4, sqlParts.length);
        assertNull(sqlParts[0]);
        assertEquals(null, sqlParts[1]);
        assertEquals(null, sqlParts[2]);
        assertEquals(null, sqlParts[3]);
    }

    @Test
    public void testBuildQueryWithSubQuery() throws Exception {
        IQueryBuilder<EntityA> qbSub = queryBuilderFactory.create(EntityA.class);
        IOperand versionMain = qb.property("Version");
        IOperand buidSub = qbSub.property("Buid");
        IOperand versionB = qbSub.property("EntityB.Version");
        ISubQuery<EntityA> subQuery = qbSub.buildSubQuery(qbSub.or(qbSub.let(qbSub.property("EntityB.Buid")).isIn(qbSub.value("BUID 11")), qbSub.let(versionB).isEqualTo(versionMain)));

        IQuery<EntityA> query = qb.build(qb.let(qb.property("Buid")).isIn(qb.subQuery(subQuery, buidSub)));
        assertNotNull(query);

        List<EntityA> entityAs = query.retrieve();
        assertEquals(2, entityAs.size());
    }

    @Test
    public void testSubQueryInFunction() throws Exception {
        IQueryBuilder<EntityA> qbSub = queryBuilderFactory.create(EntityA.class);
        IOperand versionMain = qb.property("Version");
        IOperand buidSub = qbSub.property("Buid");
        IOperand versionB = qbSub.property("EntityB.Version");
        ISubQuery<EntityA> subQuery = qbSub.buildSubQuery(qbSub.or(qbSub.let(qbSub.property("EntityB.Buid")).isIn(qbSub.value("BUID 11")), qbSub.let(versionB).isEqualTo(versionMain)));

        IQuery<EntityA> query = qb.build(qb.function("EXISTS", qb.subQuery(subQuery, buidSub)));
        assertNotNull(query);

        List<EntityA> entityAs = query.retrieve();
        assertEquals(4, entityAs.size());
    }
}
