package de.osthus.ambeth.relations.one.fk.reverse.none;

import junit.framework.Assert;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.testutil.TestProperties;

@TestProperties(name = ServiceConfigurationConstants.ToOneDefaultCascadeLoadMode, value = "EAGER")
public class OneEagerNoReverseRelationsTest extends AbstractOneNoReverseRelationsTest
{
	@Override
	protected void assertBeforePrefetch(EntityB entityB, String propertyName)
	{
		Assert.assertTrue(Boolean.TRUE.equals(proxyHelper.isInitialized(entityB, propertyName)));
		Assert.assertNull(proxyHelper.getObjRefs(entityB, propertyName));
	}
}
