package com.koch.ambeth.relations;

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
