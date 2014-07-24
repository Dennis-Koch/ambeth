package de.osthus.ambeth.relations.one.fk.reverse.none;

import org.junit.Assert;

import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.proxy.IObjRefContainer;
import de.osthus.ambeth.testutil.TestProperties;

@TestProperties(name = ServiceConfigurationConstants.ToOneDefaultCascadeLoadMode, value = "LAZY")
public class OneLazyNoReverseRelationsTest extends AbstractOneNoReverseRelationsTest
{
	@Override
	protected void assertBeforePrefetch(EntityB entityB, String propertyName)
	{
		super.assertBeforePrefetch(entityB, propertyName);

		int relationIndex = ((IObjRefContainer) entityB).get__EntityMetaData().getIndexByRelationName(propertyName);
		Assert.assertNull(((IObjRefContainer) entityB).get__ObjRefs(relationIndex));
	}
}