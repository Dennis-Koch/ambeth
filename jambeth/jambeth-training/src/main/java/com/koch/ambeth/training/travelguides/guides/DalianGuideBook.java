package com.koch.ambeth.training.travelguides.guides;

import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.training.travelguides.annotation.LogCalls;

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
