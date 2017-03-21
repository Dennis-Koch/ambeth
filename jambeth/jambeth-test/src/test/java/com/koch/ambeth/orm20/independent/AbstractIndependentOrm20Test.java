package com.koch.ambeth.orm20.independent;

/*-
 * #%L
 * jambeth-test
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

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
