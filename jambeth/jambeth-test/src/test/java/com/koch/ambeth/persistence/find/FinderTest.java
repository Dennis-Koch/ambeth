package com.koch.ambeth.persistence.find;

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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.koch.ambeth.filter.FilterDescriptor;
import com.koch.ambeth.filter.FilterOperator;
import com.koch.ambeth.filter.IPagingRequest;
import com.koch.ambeth.filter.IPagingResponse;
import com.koch.ambeth.filter.ISortDescriptor;
import com.koch.ambeth.informationbus.persistence.setup.SQLData;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.util.ParamChecker;

@SQLStructure("FinderTest_structure.sql")
@SQLData("FinderTest_data.sql")
@TestProperties(name = ServiceConfigurationConstants.mappingFile,
		value = "com/koch/ambeth/persistence/find/FinderTest_orm.xml")
@TestModule(FinderTestModule.class)
public class FinderTest extends AbstractInformationBusWithPersistenceTest {
	private IEntityBService entityBService;

	@Override
	public void afterPropertiesSet() throws Throwable {
		super.afterPropertiesSet();
		ParamChecker.assertNotNull(entityBService, "EntityBService");
	}

	public void setEntityBService(IEntityBService entityBService) {
		this.entityBService = entityBService;
	}

	@Test
	public void testRetrieve() {
		Entity entity = entityBService.retrieve(1);
		assertNotNull(entity);
		assertEquals(1, entity.getId());
	}

	@Test
	public void testRetrieveList() {
		List<Entity> entities = entityBService.retrieve(Arrays.asList(1, 2));
		assertNotNull(entities);
		assertEquals(2, entities.size());
		assertEquals(1, entities.get(0).getId());
		assertEquals(2, entities.get(1).getId());
	}

	@Test
	public void testFindReferences() {
		IPagingRequest pagingRequest = null;
		FilterDescriptor<Entity> filterDescriptor = new FilterDescriptor<>(Entity.class);
		ISortDescriptor[] sortDescriptors = null;

		filterDescriptor.setMember("Id");
		filterDescriptor.setOperator(FilterOperator.IS_EQUAL_TO);
		filterDescriptor.setValue(Collections.singletonList("2"));

		IPagingResponse<Entity> pagingResponse =
				entityBService.findReferences(pagingRequest, filterDescriptor, sortDescriptors);
		assertNotNull(pagingResponse);
		assertNotNull(pagingResponse.getRefResult());
		assertEquals(1, pagingResponse.getRefResult().size());
		assertEquals(2, pagingResponse.getRefResult().get(0).getId());
		assertNull(pagingResponse.getResult());
	}

	@Test
	public void testFindReferencesAlternateIdName() {
		IPagingRequest pagingRequest = null;
		FilterDescriptor<Entity> filterDescriptor = new FilterDescriptor<>(Entity.class);
		ISortDescriptor[] sortDescriptors = null;

		filterDescriptor.setMember("Id");
		filterDescriptor.setOperator(FilterOperator.IS_EQUAL_TO);
		filterDescriptor.setValue(Collections.singletonList("2"));

		IPagingResponse<Entity> pagingResponse =
				entityBService.findReferencesAlternate(pagingRequest, filterDescriptor, sortDescriptors);
		assertNotNull(pagingResponse);
		assertNotNull(pagingResponse.getRefResult());
		assertEquals(1, pagingResponse.getRefResult().size());
		assertEquals("AI2", pagingResponse.getRefResult().get(0).getId());
		assertNull(pagingResponse.getResult());
	}

	@Test
	public void testFindEntities() {
		IPagingRequest pagingRequest = null;
		FilterDescriptor<Entity> filterDescriptor = new FilterDescriptor<>(Entity.class);
		ISortDescriptor[] sortDescriptors = null;

		filterDescriptor.setMember("Id");
		filterDescriptor.setOperator(FilterOperator.IS_EQUAL_TO);
		filterDescriptor.setValue(Collections.singletonList("2"));

		IPagingResponse<Entity> pagingResponse =
				entityBService.findEntities(pagingRequest, filterDescriptor, sortDescriptors);
		assertNotNull(pagingResponse);
		assertNotNull(pagingResponse.getResult());
		assertEquals(1, pagingResponse.getResult().size());
		assertEquals(2, pagingResponse.getResult().get(0).getId());
		assertNull(pagingResponse.getRefResult());
	}

	@Test
	public void testFindBoth() {
		IPagingRequest pagingRequest = null;
		FilterDescriptor<Entity> filterDescriptor = new FilterDescriptor<>(Entity.class);
		ISortDescriptor[] sortDescriptors = null;

		filterDescriptor.setMember("Id");
		filterDescriptor.setOperator(FilterOperator.IS_EQUAL_TO);
		filterDescriptor.setValue(Collections.singletonList("2"));

		IPagingResponse<Entity> pagingResponse =
				entityBService.findBoth(pagingRequest, filterDescriptor, sortDescriptors);
		assertNotNull(pagingResponse);
		assertNotNull(pagingResponse.getRefResult());
		assertEquals(1, pagingResponse.getRefResult().size());
		assertEquals(2, pagingResponse.getRefResult().get(0).getId());
		assertNotNull(pagingResponse.getResult());
		assertEquals(1, pagingResponse.getResult().size());
		assertEquals(2, pagingResponse.getResult().get(0).getId());
	}
}
