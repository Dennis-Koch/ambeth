package com.koch.ambeth.relations;

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

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.cache.ValueHolderState;
import com.koch.ambeth.merge.proxy.IObjRefContainer;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestModule;

@TestModule(RelationsTestModule.class)
public abstract class AbstractRelationsTest extends AbstractInformationBusWithPersistenceTest
{
	@Autowired
	protected ICache cache;

	@Autowired
	protected IRelationsService relationsService;

	protected void assertBeforePrefetch(Object entity, String propertyName)
	{
		IObjRefContainer vhc = (IObjRefContainer) entity;
		int relationIndex = vhc.get__EntityMetaData().getIndexByRelationName(propertyName);
		Assert.assertTrue(ValueHolderState.LAZY == vhc.get__State(relationIndex));
	}

	protected void assertAfterPrefetch(Object entity, String propertyName)
	{
		IObjRefContainer vhc = (IObjRefContainer) entity;
		int relationIndex = vhc.get__EntityMetaData().getIndexByRelationName(propertyName);
		Assert.assertTrue(ValueHolderState.INIT == vhc.get__State(relationIndex));
		Assert.assertNull(vhc.get__ObjRefs(relationIndex));
	}
}
