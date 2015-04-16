package de.osthus.ambeth.training.travelguides.guides;

import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

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
