package de.osthus.ambeth.training.travelguides.guides;

import java.util.Collection;
import java.util.List;

import de.osthus.ambeth.cache.Cached;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.MergeContext;
import de.osthus.ambeth.proxy.PersistenceContext;
import de.osthus.ambeth.proxy.PersistenceContextType;
import de.osthus.ambeth.proxy.Service;
import de.osthus.ambeth.security.SecurityContext;
import de.osthus.ambeth.security.SecurityContextType;
import de.osthus.ambeth.training.travelguides.annotation.LogCalls;
import de.osthus.ambeth.training.travelguides.model.City;

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
