package com.koch.ambeth.training.travelguides.guides;

import java.util.Collection;
import java.util.List;

import com.koch.ambeth.cache.Cached;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.proxy.MergeContext;
import com.koch.ambeth.proxy.PersistenceContext;
import com.koch.ambeth.proxy.PersistenceContextType;
import com.koch.ambeth.proxy.Service;
import com.koch.ambeth.security.SecurityContext;
import com.koch.ambeth.security.SecurityContextType;
import com.koch.ambeth.training.travelguides.annotation.LogCalls;
import com.koch.ambeth.training.travelguides.model.City;

/**
 * The SopService contains methods to load or save SOPs.
 */
@MergeContext
@SecurityContext(SecurityContextType.AUTHORIZED)
@Service(ICityService.class)
@PersistenceContext(PersistenceContextType.REQUIRED)
@LogCalls
public class CityService implements ICityService, IStartingBean
{

	@LogInstance
	ILogger log;

	@Autowired
	IBook myBook;

	@Autowired
	IServiceContext serviceContext;

	@Override
	@Cached(returnMisses = true)
	public List<City> retrieveSops(int... ids)
	{
		throw new UnsupportedOperationException("should be provided by Ambeth");
	}

	@Override
	public void saveCity(Collection<City> city)
	{
		throw new UnsupportedOperationException("should be provided by Ambeth");
	}

	@Override
	public void saveCity(City... citys)
	{
		throw new UnsupportedOperationException("should be provided by Ambeth");
	}

	@Autowired
	IMyOwnDog dog;

	@Override
	public void afterStarted() throws Throwable
	{

		log.info(myBook.read() + myBook.toString());

		IBook myBook2 = serviceContext.getService(IBook.class);
		log.info(myBook2.read() + myBook2.toString());

		IBook myBook3 = serviceContext.getService(IBook.class);
		log.info(myBook3.read() + myBook3.toString());

		log.info(this.getClass().getSimpleName() + ": " + dog.getName());

	}

}
