package com.koch.ambeth.orm20.independent.a3b;

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

import org.junit.Test;

import com.koch.ambeth.orm20.independent.AbstractIndependentOrm20Test;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.testutil.TestProperties;

/**
 * Test for bi-directional one-to-one relation
 */
@TestProperties(name = ServiceConfigurationConstants.mappingFile,
		value = "com/koch/ambeth/orm20/independent/a3b/orm20.xml")
public class Orm20A3BTest extends AbstractIndependentOrm20Test {
	@Test
	public void testRelationMembersA() {
		IEntityMetaData metaData = retrieveMetaData(EntityA.class);
		RelationMember[] relationMembers = metaData.getRelationMembers();
		assertEquals(1, relationMembers.length);

		RelationMember relationMember = relationMembers[0];
		assertEquals("B", relationMember.getName());
		assertEquals(0, metaData.getIndexByRelationName(relationMember.getName()));
		assertEquals(relationMember, metaData.getMemberByName(relationMember.getName()));
		assertEquals(EntityB.class, relationMember.getRealType());
	}

	@Test
	public void testRelationMembersB() {
		IEntityMetaData metaData = retrieveMetaData(EntityB.class);
		RelationMember[] relationMembers = metaData.getRelationMembers();
		assertEquals(1, relationMembers.length);

		RelationMember relationMember = relationMembers[0];
		assertEquals("A", relationMember.getName());
		assertEquals(0, metaData.getIndexByRelationName(relationMember.getName()));
		assertEquals(relationMember, metaData.getMemberByName(relationMember.getName()));
		assertEquals(EntityA.class, relationMember.getRealType());
	}

	@Test
	public void testRelatedTypes() {
		IEntityMetaData metaDataA = retrieveMetaData(EntityA.class);
		Class<?>[] typesRelatingToA = metaDataA.getTypesRelatingToThis();
		assertNotNull(typesRelatingToA);
		assertEquals(1, typesRelatingToA.length);
		metaDataA.isRelatingToThis(typesRelatingToA[0]);

		IEntityMetaData metaDataB = retrieveMetaData(EntityB.class);
		Class<?>[] typesRelatingToB = metaDataB.getTypesRelatingToThis();
		assertNotNull(typesRelatingToB);
		assertEquals(1, typesRelatingToB.length);
		metaDataB.isRelatingToThis(typesRelatingToB[0]);
	}
}
