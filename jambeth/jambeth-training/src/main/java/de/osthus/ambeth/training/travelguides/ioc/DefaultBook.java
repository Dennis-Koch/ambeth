package de.osthus.ambeth.training.travelguides.ioc;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.training.travelguides.annotation.LogCalls;
import de.osthus.ambeth.training.travelguides.guides.IGuideBook;

@LogCalls
public class DefaultBook implements IGuideBook
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public int guideUs(String place)
	{
		return 1;
	}

	@Override
	public void removePlace(String place)
	{
	}

	@Override
	public void addPlace(String place, Integer dir)
	{
	}

	public void callMe()
	{
	}

}
