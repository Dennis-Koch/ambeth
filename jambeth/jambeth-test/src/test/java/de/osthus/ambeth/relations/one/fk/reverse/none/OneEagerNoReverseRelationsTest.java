package de.osthus.ambeth.relations.one.fk.reverse.none;

import org.junit.Assert;

import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.proxy.IObjRefContainer;
import de.osthus.ambeth.testutil.TestProperties;

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
