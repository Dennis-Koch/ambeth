package de.osthus.ambeth.persistence.find;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import de.osthus.ambeth.filter.model.FilterDescriptor;
import de.osthus.ambeth.filter.model.FilterOperator;
import de.osthus.ambeth.filter.model.IPagingRequest;
import de.osthus.ambeth.filter.model.IPagingResponse;
import de.osthus.ambeth.filter.model.ISortDescriptor;
import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.util.ParamChecker;

@SQLStructure("FinderTest_structure.sql")
@SQLData("FinderTest_data.sql")
@TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/persistence/find/FinderTest_orm.xml")
@TestModule(FinderTestModule.class)
public class FinderTest extends AbstractPersistenceTest
{
	private IEntityBService entityBService;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();
		ParamChecker.assertNotNull(entityBService, "EntityBService");
	}

	public void setEntityBService(IEntityBService entityBService)
	{
		this.entityBService = entityBService;
	}

	@Test
	public void testRetrieve()
	{
		Entity entity = entityBService.retrieve(1);
		assertNotNull(entity);
		assertEquals(1, entity.getId());
	}

	@Test
	public void testRetrieveList()
	{
		List<Entity> entities = entityBService.retrieve(Arrays.asList(1, 2));
		assertNotNull(entities);
		assertEquals(2, entities.size());
		assertEquals(1, entities.get(0).getId());
		assertEquals(2, entities.get(1).getId());
	}

	@Test
	public void testFindReferences()
	{
		IPagingRequest pagingRequest = null;
		FilterDescriptor<Entity> filterDescriptor = new FilterDescriptor<Entity>(Entity.class);
		ISortDescriptor[] sortDescriptors = null;

		filterDescriptor.setMember("Id");
		filterDescriptor.setOperator(FilterOperator.IS_EQUAL_TO);
		filterDescriptor.setValue(Collections.singletonList("2"));

		IPagingResponse<Entity> pagingResponse = entityBService.findReferences(pagingRequest, filterDescriptor, sortDescriptors);
		assertNotNull(pagingResponse);
		assertNotNull(pagingResponse.getRefResult());
		assertEquals(1, pagingResponse.getRefResult().size());
		assertEquals(2, pagingResponse.getRefResult().get(0).getId());
		assertNull(pagingResponse.getResult());
	}

	@Test
	public void testFindEntities()
	{
		IPagingRequest pagingRequest = null;
		FilterDescriptor<Entity> filterDescriptor = new FilterDescriptor<Entity>(Entity.class);
		ISortDescriptor[] sortDescriptors = null;

		filterDescriptor.setMember("Id");
		filterDescriptor.setOperator(FilterOperator.IS_EQUAL_TO);
		filterDescriptor.setValue(Collections.singletonList("2"));

		IPagingResponse<Entity> pagingResponse = entityBService.findEntities(pagingRequest, filterDescriptor, sortDescriptors);
		assertNotNull(pagingResponse);
		assertNotNull(pagingResponse.getResult());
		assertEquals(1, pagingResponse.getResult().size());
		assertEquals(2, pagingResponse.getResult().get(0).getId());
		assertNull(pagingResponse.getRefResult());
	}

	@Test
	public void testFindBoth()
	{
		IPagingRequest pagingRequest = null;
		FilterDescriptor<Entity> filterDescriptor = new FilterDescriptor<Entity>(Entity.class);
		ISortDescriptor[] sortDescriptors = null;

		filterDescriptor.setMember("Id");
		filterDescriptor.setOperator(FilterOperator.IS_EQUAL_TO);
		filterDescriptor.setValue(Collections.singletonList("2"));

		IPagingResponse<Entity> pagingResponse = entityBService.findBoth(pagingRequest, filterDescriptor, sortDescriptors);
		assertNotNull(pagingResponse);
		assertNotNull(pagingResponse.getRefResult());
		assertEquals(1, pagingResponse.getRefResult().size());
		assertEquals(2, pagingResponse.getRefResult().get(0).getId());
		assertNotNull(pagingResponse.getResult());
		assertEquals(1, pagingResponse.getResult().size());
		assertEquals(2, pagingResponse.getResult().get(0).getId());
	}
}
