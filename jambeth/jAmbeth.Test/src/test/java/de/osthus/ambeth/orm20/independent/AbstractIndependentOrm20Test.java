package de.osthus.ambeth.orm20.independent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import de.osthus.ambeth.ioc.BytecodeModule;
import de.osthus.ambeth.ioc.CompositeIdModule;
import de.osthus.ambeth.ioc.EventModule;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.independent.IndependentEntityMetaDataClientTestModule;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.testutil.AbstractIocTest;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;

@TestModule({ BytecodeModule.class, CompositeIdModule.class, IndependentEntityMetaDataClientTestModule.class, EventModule.class })
public abstract class AbstractIndependentOrm20Test extends AbstractIocTest
{
	protected IEntityMetaDataProvider entityMetaDataProvider;

	public void setFixture(IEntityMetaDataProvider entityMetaDataProvider)
	{
		this.entityMetaDataProvider = entityMetaDataProvider;
	}

	protected IEntityMetaData retrieveMetaData(Class<?> entityType)
	{
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType, true);
		assertNotNull("No metaData found for '" + entityType.getName() + "'", metaData);
		return metaData;
	}

	protected void testZeroRelationMembers(Class<?> entityType)
	{
		IEntityMetaData metaData = retrieveMetaData(entityType);

		IRelationInfoItem[] relationMembers = metaData.getRelationMembers();
		assertEquals(0, relationMembers.length);
	}
}