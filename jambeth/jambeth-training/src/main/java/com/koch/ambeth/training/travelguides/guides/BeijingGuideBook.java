package com.koch.ambeth.training.travelguides.guides;

import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

public class BeijingGuideBook extends AbstractGuideBook implements IStartingBean
{

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
