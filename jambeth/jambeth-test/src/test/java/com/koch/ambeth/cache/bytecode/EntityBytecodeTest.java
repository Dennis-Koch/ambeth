package com.koch.ambeth.cache.bytecode;

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

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.cache.IRootCache;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.merge.IObjRefHelper;
import com.koch.ambeth.merge.IProxyHelper;
import com.koch.ambeth.merge.cache.ValueHolderState;
import com.koch.ambeth.merge.proxy.IObjRefContainer;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.testutil.AbstractInformationBusTest;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.testutil.TestRebuildContext;

@TestPropertiesList({@TestProperties(name = ServiceConfigurationConstants.mappingFile,
		value = "com/koch/ambeth/cache/bytecode/EntityBytecodeTest-orm.xml")})
@TestRebuildContext
public class EntityBytecodeTest extends AbstractInformationBusTest {
	@LogInstance
	protected ILogger log;

	@Autowired
	protected IRootCache rootCache;

	@Autowired
	protected IEntityFactory entityFactory;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IObjRefHelper oriHelper;

	@Autowired
	protected IProxyHelper proxyHelper;

	@Test
	public void testValueHolderWithoutField() throws Exception {
		IObjRefContainer testEntity = (IObjRefContainer) entityFactory.createEntity(TestEntity.class);
		int relationIndex = testEntity.get__EntityMetaData().getIndexByRelationName("ChildrenNoField");
		Assert.assertFalse(ValueHolderState.INIT == testEntity.get__State(relationIndex));
	}

	@Test
	public void testValueHolderWithProtectedField() throws Exception {
		IObjRefContainer testEntity = (IObjRefContainer) entityFactory.createEntity(TestEntity.class);
		int relationIndex =
				testEntity.get__EntityMetaData().getIndexByRelationName("ChildrenWithProtectedField");
		Assert.assertFalse(ValueHolderState.INIT == testEntity.get__State(relationIndex));
	}

	@Test
	public void testValueHolderWithPrivateField() throws Exception {
		IObjRefContainer testEntity = (IObjRefContainer) entityFactory.createEntity(TestEntity.class);
		int relationIndex =
				testEntity.get__EntityMetaData().getIndexByRelationName("ChildrenWithPrivateField");
		Assert.assertFalse(ValueHolderState.INIT == testEntity.get__State(relationIndex));
	}

	@Test
	public void testInterfaceEntity() throws Exception {
		IObjRefContainer testEntity2 =
				(IObjRefContainer) entityFactory.createEntity(ITestEntity2.class);
		int relationIndex =
				testEntity2.get__EntityMetaData().getIndexByRelationName("ChildrenWithProtectedField");
		Assert.assertFalse(ValueHolderState.INIT == testEntity2.get__State(relationIndex));
	}

	@Test
	public void testInterfaceEntityReadDate() throws Exception {
		ITestEntity2 testEntity = entityFactory.createEntity(ITestEntity2.class);

		testEntity.setMyDate(new Date(System.currentTimeMillis()));

		testEntity.setId(1);
		testEntity.setVersion(1);

		rootCache.put(testEntity);

		IObjRefContainer testEntity2 = (IObjRefContainer) testEntity;
		int relationIndex =
				testEntity2.get__EntityMetaData().getIndexByRelationName("ChildrenWithProtectedField");

		Assert.assertFalse(ValueHolderState.INIT == testEntity2.get__State(relationIndex));
	}
}
