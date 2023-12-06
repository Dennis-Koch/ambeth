package com.koch.ambeth.persistence.jdbc.bigstatements;

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
import com.koch.ambeth.merge.IMergeProcess;
import com.koch.ambeth.model.Material;
import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLDataRebuild;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.testutil.category.SlowTests;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.exception.MaskingRuntimeException;
import jakarta.persistence.PersistenceException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.net.SocketException;
import java.sql.SQLRecoverableException;
import java.sql.SQLSyntaxErrorException;

@Category(SlowTests.class)
@TestPropertiesList({
        @TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "orm.xml"),
        @TestProperties(name = PersistenceConfigurationConstants.DatabaseTablePrefix, value = "D_"),
        @TestProperties(name = PersistenceConfigurationConstants.DatabaseFieldPrefix, value = "F_"),
        @TestProperties(name = "ambeth.log.level.com.koch.ambeth.persistence.jdbc.JdbcTable", value = "INFO")
})
@SQLStructure("BigStatement_structure.sql")
@SQLData("BigStatement_data.sql")
@SQLDataRebuild(false)
public class BigStatementTest extends AbstractInformationBusWithPersistenceTest {
    @Test
    public void testBigQuery100000() throws Exception {
        var paramName = "paramName";
        var qb = queryBuilderFactory.create(Material.class);
        var query = qb.build(qb.let(qb.property("Id")).isIn(qb.valueName(paramName)));

        var bigList = new ArrayList<>();
        for (int a = 100000; a-- > 0; ) {
            bigList.add(Integer.valueOf(a + 1));
        }
        try {
            var materials = query.param(paramName, bigList).retrieve();
            Assert.assertNotNull(materials);
            Assert.assertEquals(90006, materials.size());
        } catch (MaskingRuntimeException e) {
            Throwable cause = e.getCause();
            Assert.assertTrue(cause instanceof PersistenceException);
            cause = cause.getCause();
            Assert.assertTrue(cause instanceof SQLSyntaxErrorException);
            Assert.assertEquals("ORA-01745: invalid host/bind variable name\n", cause.getMessage());
            throw e;
        }

    }

    @Test
    public void testBigQuery20000() throws Exception {
        var paramName = "paramName";
        var qb = queryBuilderFactory.create(Material.class);
        var query = qb.build(qb.let(qb.property("Id")).isIn(qb.valueName(paramName)));

        var bigList = new ArrayList<>();
        for (int a = 20000; a-- > 0; ) {
            bigList.add(Integer.valueOf(a + 1));
        }
        try {
            var materials = query.param(paramName, bigList).retrieve();
            Assert.assertNotNull(materials);
            Assert.assertEquals(10006, materials.size());
        } catch (MaskingRuntimeException e) {
            Throwable cause = e.getCause();
            Assert.assertTrue(cause instanceof PersistenceException);
            cause = cause.getCause();
            Assert.assertTrue(cause instanceof SQLRecoverableException);
            cause = cause.getCause();
            Assert.assertTrue(cause instanceof SocketException);
            Assert.assertEquals("Connection reset by peer: socket write error", cause.getMessage());
            throw e;
        }
    }

    @Test
    public void testSelectFields100000() throws Exception {
        var bigList = new ArrayList<>();
        for (int a = 100000; a-- > 0; ) {
            bigList.add(Integer.valueOf(a + 1));
        }
        transaction.processAndCommit(persistenceUnitToDatabaseMap -> {
            var database = persistenceUnitToDatabaseMap.iterator().next().getValue();
            var table = database.getTableByType(Material.class);
            var cursor = table.selectVersion(IObjRef.PRIMARY_KEY_INDEX, bigList);
            try {

            } finally {
                cursor.dispose();
            }
        });
        transaction.processAndCommit(persistenceUnitToDatabaseMap -> {
            var database = persistenceUnitToDatabaseMap.iterator().next().getValue();
            var table = database.getTableByType(Material.class);
            var cursor = table.selectValues(IObjRef.PRIMARY_KEY_INDEX, bigList);
            try {

            } finally {
                cursor.dispose();
            }
        });
    }

    @Test
    public void testMerge100000() throws Exception {
        var qb = queryBuilderFactory.create(Material.class);
        var query = qb.build(qb.all());
        var materials = query.retrieve();
        Assert.assertTrue(materials.size() > 100000);

        for (var material : materials) {
            material.setName(material.getName() + "2");
        }
        beanContext.getService(IMergeProcess.class).process(materials);
    }
}
