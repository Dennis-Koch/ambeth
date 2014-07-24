package de.osthus.ambeth.relations;

import org.junit.Assert;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.ValueHolderState;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.proxy.IObjRefContainer;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.TestModule;

@TestModule(RelationsTestModule.class)
public abstract class AbstractRelationsTest extends AbstractPersistenceTest
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
