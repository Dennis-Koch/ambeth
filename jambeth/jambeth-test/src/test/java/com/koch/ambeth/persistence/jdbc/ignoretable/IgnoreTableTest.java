package com.koch.ambeth.persistence.jdbc.ignoretable;

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

import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.database.DatabaseCallback;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.util.collections.ILinkedMap;
import org.junit.Test;

@SQLStructure("IgnoreTable_structure.sql")
public class IgnoreTableTest extends AbstractInformationBusWithPersistenceTest {
    @Test
    public void testAutoIndexFalse() {
        transaction.processAndCommit(new DatabaseCallback() {

            @Override
            public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) throws Exception {

            }
        });
    }
}
