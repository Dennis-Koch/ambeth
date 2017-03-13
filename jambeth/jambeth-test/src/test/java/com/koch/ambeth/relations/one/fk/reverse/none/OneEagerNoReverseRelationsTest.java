package com.koch.ambeth.relations.one.fk.reverse.none;

import org.junit.Assert;

import com.koch.ambeth.merge.proxy.IObjRefContainer;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.TestProperties;

@TestProperties(name = ServiceConfigurationConstants.ToOneDefaultCascadeLoadMode, value = "EAGER")
public class OneEagerNoReverseRelationsTest extends AbstractOneNoReverseRelationsTest
{
	@Override
	protected void assertBeforePrefetch(EntityB entityB, String propertyName)
	{
		int relationIndex = ((IObjRefContainer) entityB).get__EntityMetaData().getIndexByRelationName(propertyName);
		Assert.assertTrue(((IObjRefContainer) entityB).is__Initialized(relationIndex));
		Assert.assertNull(((IObjRefContainer) entityB).get__ObjRefs(relationIndex));
	}
}
