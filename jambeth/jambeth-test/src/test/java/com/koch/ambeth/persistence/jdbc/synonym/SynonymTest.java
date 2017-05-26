package com.koch.ambeth.persistence.jdbc.synonym;

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

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.koch.ambeth.informationbus.persistence.setup.SQLData;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.informationbus.persistence.setup.SQLTableSynonyms;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.category.ReminderTests;

@Category(ReminderTests.class)
@SQLStructure("Synonym_structure.sql")
@SQLData("Synonym_data.sql")
@SQLTableSynonyms("S_CHILD")
public class SynonymTest extends AbstractInformationBusWithPersistenceTest {
	@LogInstance
	private ILogger log;

	@Test
	public void test1() {
		log.info("test1");
	}

	@Test
	public void test2() {
		log.info("test2");
	}
}
