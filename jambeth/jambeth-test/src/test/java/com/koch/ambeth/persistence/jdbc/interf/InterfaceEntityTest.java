package com.koch.ambeth.persistence.jdbc.interf;

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

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.merge.IMergeProcess;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.util.ParamChecker;

@TestModule({})
@SQLStructure("structure.sql")
@TestProperties(name = ServiceConfigurationConstants.mappingFile,
		value = "com/koch/ambeth/persistence/jdbc/interf/orm.xml")
public class InterfaceEntityTest extends AbstractInformationBusWithPersistenceTest {
	protected IMergeProcess mergeProcess;

	@Override
	public void afterPropertiesSet() throws Throwable {
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(mergeProcess, "MergeProcess");
	}

	public void setMergeProcess(IMergeProcess mergeProcess) {
		this.mergeProcess = mergeProcess;
	}

	protected ITestEntity createEntity(String name) {
		ITestEntity entity = entityFactory.createEntity(ITestEntity.class);
		entity.setName(name);

		mergeProcess.process(entity);
		return entity;
	}

	@Test
	public void createEntity() {
		String myName = "name55";
		ITestEntity entity = createEntity(myName);

		Assert.assertTrue("Wrong id", entity.getId() > 0);
		Assert.assertEquals("Wrong version!", (short) 1, entity.getVersion());
	}

	@Test
	public void editEntity() {
		String myName = "name55";
		ITestEntity entity = createEntity(myName);

		entity.setName(myName + "x");
		mergeProcess.process(entity);
		Assert.assertEquals("Wrong version!", (short) 2, entity.getVersion());
	}
}
