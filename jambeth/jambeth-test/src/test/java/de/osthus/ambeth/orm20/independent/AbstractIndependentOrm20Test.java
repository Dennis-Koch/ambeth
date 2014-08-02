package de.osthus.ambeth.orm20.independent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.metadata.RelationMember;
import de.osthus.ambeth.testutil.AbstractInformationBusTest;

public abstract class AbstractIndependentOrm20Test extends AbstractInformationBusTest
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

		RelationMember[] relationMembers = metaData.getRelationMembers();
		assertEquals(0, relationMembers.length);
	}
}