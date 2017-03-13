package com.koch.ambeth.orm20.independent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.testutil.AbstractInformationBusTest;

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