package de.osthus.ambeth.training.travelguides.guides;

import java.util.Map;

import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

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