package com.koch.ambeth.training.travelguides.guides;

import java.util.Map;

import com.koch.ambeth.collections.HashMap;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

public class AbstractGuideBook implements IGuideBook
{

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;
	Map<String, Integer> places = new HashMap<String, Integer>();

	public AbstractGuideBook()
	{
		super();
	}

	@Override
	public int guideUs(String place)
	{
		Integer checkPlace = places.get(place);
		if (checkPlace == null)
		{
			return NO_DIRECTION;
		}
		return checkPlace.intValue();
	}

	@Override
	public void addPlace(String place, Integer dir)
	{
		places.put(place, dir);
	}

	@Override
	public void removePlace(String place)
	{
		places.remove(place);
	}

}