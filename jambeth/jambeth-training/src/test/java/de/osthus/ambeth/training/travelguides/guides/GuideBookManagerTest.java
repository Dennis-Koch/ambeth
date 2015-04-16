package de.osthus.ambeth.training.travelguides.guides;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.training.travelguides.ioc.HelloWorldModule;
import de.osthus.ambeth.training.travelguides.model.City;
import de.osthus.ambeth.training.travelguides.setup.AbstractGuideBookTest;

@TestModule({ HelloWorldModule.class })
public class GuideBookManagerTest extends AbstractGuideBookTest
{

	@Autowired
	ICache cache;

	@Autowired
	IEntityFactory entityFactory;

	@Autowired
	ICityService cityService;

	@LogInstance
	private ILogger log;
	private int id;

	@Before
	public void setUpTest()
	{
		City city = entityFactory.createEntity(City.class);
		City city2 = entityFactory.createEntity(City.class);
		log.info("before save " + city.getId());
		city.setName("Dalian");
		city2.setName("Aachen");
		cityService.saveCity(city, city2);
		log.info("after save " + city.getId());
		id = city.getId();
	}

	@Test
	public void testConnection()
	{

		City object = cache.getObject(City.class, id);
		Assert.assertNotNull(object);
	}

}
