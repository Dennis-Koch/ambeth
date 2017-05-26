package com.koch.ambeth.util;

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

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestProperties;

@SQLStructure("LongId_structure.sql")
@TestProperties(name = ServiceConfigurationConstants.mappingFile,
		value = "com/koch/ambeth/util/long-orm.xml")
public class LongIdTest extends AbstractInformationBusWithPersistenceTest {
	// This test already succeeds if it reaches this point. We test a framework setup problem with
	// Objects as member types.
	@Test
	public void test() {
		assertTrue(true); // Testing...testing...
	}
}
