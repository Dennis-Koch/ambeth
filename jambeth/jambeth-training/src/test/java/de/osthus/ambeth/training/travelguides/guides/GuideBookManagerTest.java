package com.koch.ambeth.training.travelguides.guides;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.koch.ambeth.cache.ICache;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.training.travelguides.ioc.HelloWorldModule;
import com.koch.ambeth.training.travelguides.model.City;
import com.koch.ambeth.training.travelguides.setup.AbstractGuideBookTest;

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
