package com.koch.ambeth.mapping;

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
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusTest;
import com.koch.ambeth.testutil.TestProperties;

@TestProperties(name = ServiceConfigurationConstants.mappingFile,
		value = "com/koch/ambeth/mapping/orm-mapping.xml")
public class MappingProviderDataTest extends AbstractInformationBusTest {

	@Autowired
	IPropertyExpansionProvider propertyExpansionProvider;
	@Autowired
	IEntityFactory entityFactory;

	@Test
	public void testgetPropertyOneProperty() {
		EntityB entityB = entityFactory.createEntity(EntityB.class);
		EntityA entityA = entityFactory.createEntity(EntityA.class);
		entityB.setEntityA(entityA);

		PropertyExpansion propertyExpansion =
				propertyExpansionProvider.getPropertyExpansion(EntityB.class, "EntityA");
		assertEquals(entityA, propertyExpansion.getValue(entityB));
	}

	@Test
	public void testgetPropertyTwoSteps() {
		EntityB entityB = entityFactory.createEntity(EntityB.class);
		EntityA entityA = entityFactory.createEntity(EntityA.class);
		entityB.setEntityA(entityA);
		entityA.setName("KARL");

		PropertyExpansion propertyExpansion =
				propertyExpansionProvider.getPropertyExpansion(EntityB.class, "EntityA.Name");
		assertEquals(entityA.getName(), propertyExpansion.getValue(entityB));
	}

	@Test
	public void testsetPropertyOneStep() {
		EntityB entityB = entityFactory.createEntity(EntityB.class);

		PropertyExpansion propertyExpansion =
				propertyExpansionProvider.getPropertyExpansion(EntityB.class, "NameOfB");
		propertyExpansion.setValue(entityB, "HUGO");
		assertEquals("HUGO", entityB.getNameOfB());
	}

	@Test
	public void testsetPropertyTwoSteps() {
		EntityB entityB = entityFactory.createEntity(EntityB.class);
		assertNull(entityB.getEntityA());
		PropertyExpansion propertyExpansion =
				propertyExpansionProvider.getPropertyExpansion(EntityB.class, "EntityA.Name");
		propertyExpansion.setValue(entityB, "HUGO");
		assertNotNull(entityB.getEntityA());
		assertEquals("HUGO", entityB.getEntityA().getName());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFailOnBadPropertyName() {
		PropertyExpansion propertyExpansion =
				propertyExpansionProvider.getPropertyExpansion(EntityB.class, "nameOfB");
	}

}
