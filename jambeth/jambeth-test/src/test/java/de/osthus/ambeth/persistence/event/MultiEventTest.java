package de.osthus.ambeth.persistence.event;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import de.osthus.ambeth.cache.CacheFactoryDirective;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.ICacheFactory;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.util.ParamChecker;

@SQLData("MultiEvent_data.sql")
@SQLStructure("MultiEvent_structure.sql")
@TestModule(MultiEventTestModule.class)
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/persistence/event/orm.xml")
public class MultiEventTest extends AbstractPersistenceTest
{
	private ICacheFactory cacheFactory;

	private IMultiEventService multiEventService;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(cacheFactory, "cacheFactory");
		ParamChecker.assertNotNull(multiEventService, "multiEventService");
	}

	public void setCacheFactory(ICacheFactory cacheFactory)
	{
		this.cacheFactory = cacheFactory;
	}

	public void setMultiEventService(IMultiEventService multiEventService)
	{
		this.multiEventService = multiEventService;
	}

	@Test
	public void testMultipleSaveCallsInOneMethod() throws Exception
	{
		ICache cache = cacheFactory.create(CacheFactoryDirective.SubscribeTransactionalDCE, "test");
		List<MultiEventEntity> multiEventEntities = cache.getObjects(MultiEventEntity.class, Arrays.asList(1, 2, 3));
		assertEquals(3, multiEventEntities.size());

		for (MultiEventEntity entity : multiEventEntities)
		{
			String name = entity.getName();
			name = name.replace(".1", ".2");
			entity.setName(name);
		}
		multiEventService.doMultipleThings(multiEventEntities);

		cache = cacheFactory.create(CacheFactoryDirective.SubscribeTransactionalDCE, "test");
		multiEventEntities = cache.getObjects(MultiEventEntity.class, Arrays.asList(1, 2, 3));
		for (MultiEventEntity entity : multiEventEntities)
		{
			String name = entity.getName();
			assertEquals("2", name.substring(name.length() - 1));
		}
	}

	@Test
	public void testChangeAndResaveInOneMethod() throws Exception
	{
		ICache cache = cacheFactory.create(CacheFactoryDirective.NoDCE, "test");
		List<MultiEventEntity> multiEventEntities = cache.getObjects(MultiEventEntity.class, Arrays.asList(1, 2, 3));
		assertEquals(3, multiEventEntities.size());

		for (MultiEventEntity entity : multiEventEntities)
		{
			String name = entity.getName();
			name = name.replace(".1", ".2");
			entity.setName(name);
		}
		multiEventService.doMultipleThings2(multiEventEntities);

		cache = cacheFactory.create(CacheFactoryDirective.NoDCE, "test");
		multiEventEntities = cache.getObjects(MultiEventEntity.class, Arrays.asList(1, 2, 3));
		for (MultiEventEntity entity : multiEventEntities)
		{
			String name = entity.getName();
			assertEquals("3", name.substring(name.length() - 1));
		}
	}
}