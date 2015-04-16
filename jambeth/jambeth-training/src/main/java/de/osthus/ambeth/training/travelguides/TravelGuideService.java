package de.osthus.ambeth.training.travelguides;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.training.travelguides.guides.IGuideBook;
import de.osthus.ambeth.training.travelguides.guides.IGuideBookExtendable;

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
