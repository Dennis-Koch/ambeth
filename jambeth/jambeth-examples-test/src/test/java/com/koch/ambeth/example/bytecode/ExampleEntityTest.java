package com.koch.ambeth.example.bytecode;

/*-
 * #%L
 * jambeth-examples-test
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

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusTest;
import com.koch.ambeth.testutil.TestProperties;

@TestProperties(name = ServiceConfigurationConstants.mappingFile,
		value = "ExampleEntityTest_orm.xml")
public class ExampleEntityTest extends AbstractInformationBusTest {
	@Autowired
	protected IEntityFactory entityFactory;

	@Test
	public void myTest() {
		ExampleEntity exampleEntity = entityFactory.createEntity(ExampleEntity.class);
		Assert.assertEquals(ExampleEntity.class.getName() + "-null", exampleEntity.toString());
	}
}
