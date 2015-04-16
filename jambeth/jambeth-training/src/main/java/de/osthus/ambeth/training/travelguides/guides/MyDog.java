package de.osthus.ambeth.training.travelguides.guides;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class MyDog implements IMyOwnDog
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	String name;

	public MyDog(int counter)
	{
		name = "WUF" + counter;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
}
