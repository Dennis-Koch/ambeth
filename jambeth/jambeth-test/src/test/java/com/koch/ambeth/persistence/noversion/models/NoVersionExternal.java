package com.koch.ambeth.persistence.noversion.models;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.model.AbstractBusinessObject;

public class NoVersionExternal extends AbstractBusinessObject
{
	@SuppressWarnings("unused")
	@LogInstance(NoVersionExternal.class)
	private ILogger log;

	private int id;

	private String name;

	@Override
	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
}
