package com.koch.ambeth.training.travelguides.ioc;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.training.travelguides.annotation.LogCalls;
import com.koch.ambeth.training.travelguides.guides.IGuideBook;

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
