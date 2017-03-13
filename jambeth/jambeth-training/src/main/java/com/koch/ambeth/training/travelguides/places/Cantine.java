package com.koch.ambeth.training.travelguides.places;

import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.training.travelguides.guides.IGuideBook;
import com.koch.ambeth.training.travelguides.guides.IGuideBookExtendable;

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
