package com.koch.ambeth.training.travelguides;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.training.travelguides.guides.IGuideBook;
import com.koch.ambeth.training.travelguides.guides.IGuideBookExtendable;

public class TravelGuideService
{
	@LogInstance
	private ILogger log;

	@Autowired
	IGuideBookExtendable guideBookExtendable;

	public void speak()
	{
		IGuideBook guideBook = guideBookExtendable.getBook("Dalian");
		Integer dirCinema = guideBook.guideUs("cinema");
		Integer dirCantine = guideBook.guideUs("cantine");
		log.info("GO: " + dirCinema);
		log.info("GO: " + dirCantine);
	}
}
