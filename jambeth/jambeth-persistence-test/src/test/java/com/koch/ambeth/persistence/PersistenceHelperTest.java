package com.koch.ambeth.persistence;

/*-
 * #%L
 * jambeth-persistence-test
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

import com.koch.ambeth.persistence.sql.SqlBuilder;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.objectcollector.NoOpObjectCollector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class PersistenceHelperTest {
    private static final int batchSize = 3;

    private static final int preparedBatchSize = 3;

    private PersistenceHelper fixture;

    @Before
    public void setUp() throws Exception {
        fixture = new PersistenceHelper();
        fixture.batchSize = batchSize;
        fixture.preparedBatchSize = preparedBatchSize;

        NoOpObjectCollector oc = new NoOpObjectCollector();
        fixture.objectCollector = oc;

        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.setObjectCollector(oc);
        sqlBuilder.setPersistenceHelper(fixture);
        sqlBuilder.afterPropertiesSet();
        fixture.sqlBuilder = sqlBuilder;
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAfterPropertiesSet_wrongBatchSize() {
        fixture.batchSize = 0;
        fixture.afterPropertiesSet();
    }

    @Test
    public void testSetBatchSize() {
        int newBatchSize = 0;
        fixture.batchSize = newBatchSize;
        assertEquals(newBatchSize, fixture.batchSize);
    }

    @Test
    public void testSetObjectCollector() {
        assertNotNull(fixture.objectCollector);
        fixture.objectCollector = null;
        assertNull(fixture.objectCollector);
    }

    @Test
    public void testSetSqlBuilder() {
        assertNotNull(fixture.sqlBuilder);
        fixture.sqlBuilder = null;
        assertNull(fixture.sqlBuilder);
    }

    @Test
    public void testSplitValues() {
        int batchRows = 3;
        var values = new ArrayList<>();
        for (int i = (batchRows - 1) * preparedBatchSize + 1; i-- > 0; ) {
            values.add(new Object());
        }
        var actual = fixture.splitValues(values);
        assertEquals(batchRows, actual.size());
        boolean last = true;
        for (int i = actual.size(); i-- > 0; ) {
            if (!last) {
                assertEquals(batchSize, actual.get(i).size());
            } else {
                assertEquals(1, actual.get(i).size());
                last = false;
            }
        }
    }

    @Test
    public void testBuildStringListOfValues() {
        int batchRows = 4;
        List<Object> values = new ArrayList<>();
        for (int i = (batchRows - 1) * batchSize + 1; i-- > 0; ) {
            values.add(i);
        }
        var actual = fixture.buildStringListOfValues(values);
        assertEquals(batchRows, actual.size());
    }

    @Test
    public void testBuildStringOfValues() {
        int count = 4; // should have only one digit
        List<Object> values = new ArrayList<>();
        for (int i = count; i-- > 0; ) {
            values.add(i);
        }
        String actual = fixture.buildStringOfValues(values);
        assertEquals(count * 2 - 1, actual.length());
    }

    @Test
    public void testAppendStringOfValues() {
        int count = 3; // should have only one digit
        var values = new ArrayList<>();
        for (int i = count; i-- > 0; ) {
            values.add(new Integer(i).toString());
        }
        String actual = fixture.buildStringOfValues(values);
        assertEquals(count * 4 - 1, actual.length());
    }
}
