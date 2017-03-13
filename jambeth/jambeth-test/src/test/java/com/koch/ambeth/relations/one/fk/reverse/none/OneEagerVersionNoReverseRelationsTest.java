package com.koch.ambeth.relations.one.fk.reverse.none;

import org.junit.Assert;

import com.koch.ambeth.merge.proxy.IObjRefContainer;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.TestProperties;

@TestProperties(name = ServiceConfigurationConstants.ToOneDefaultCascadeLoadMode, value = "EAGER_VERSION")
public class OneEagerVersionNoReverseRelationsTest extends AbstractOneNoReverseRelationsTest
{
	@Override
	protected void assertBeforePrefetch(EntityB entityB, String propertyName)
	{
		super.assertBeforePrefetch(entityB, propertyName);

		int relationIndex = ((IObjRefContainer) entityB).get__EntityMetaData().getIndexByRelationName(propertyName);
		Assert.assertNotNull(((IObjRefContainer) entityB).get__ObjRefs(relationIndex));
	}
}
