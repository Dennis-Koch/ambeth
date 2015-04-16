package de.osthus.ambeth.training.travelguides.places;

import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.training.travelguides.guides.IGuideBook;
import de.osthus.ambeth.training.travelguides.guides.IGuideBookExtendable;

public class Cantine implements IStartingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	// get my guide book
	IGuideBook guideBook;

	@Autowired
	IGuideBookExtendable guideBookExtendable;

	@Override
	public void afterStarted() throws Throwable
	{
		guideBook = guideBookExtendable.getBook("Dalian");
		// add the cantine to the book
		guideBook.addPlace("cantine", 3);
	}

}
