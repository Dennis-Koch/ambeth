package de.osthus.ambeth.relations.one.fk.reverse.none;

import org.junit.Assert;

import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.testutil.TestProperties;

@TestProperties(name = ServiceConfigurationConstants.ToOneDefaultCascadeLoadMode, value = "EAGER_VERSION")
public class OneEagerVersionNoReverseRelationsTest extends AbstractOneNoReverseRelationsTest
{
	@Override
	protected void assertBeforePrefetch(EntityB entityB, String propertyName)
	{
		super.assertBeforePrefetch(entityB, propertyName);
		Assert.assertNotNull(proxyHelper.getObjRefs(entityB, propertyName));
	}
}
