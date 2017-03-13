package com.koch.ambeth.training.travelguides.guides;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

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
