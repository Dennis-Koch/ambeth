package de.osthus.ambeth.training.travelguides.guides;

import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.training.travelguides.annotation.LogCalls;

@LogCalls
public class DalianGuideBook extends AbstractGuideBook implements IStartingBean
{
	public DalianGuideBook()
	{
		addPlace("cinema", 1);
		addPlace("zoo", 2);
	}

	@LogInstance
	ILogger log;
	@Autowired
	IMyOwnDog dog;

	@Override
	public void afterStarted() throws Throwable
	{
		log.info(this.getClass().getSimpleName() + ": " + dog.getName());
	}

}
