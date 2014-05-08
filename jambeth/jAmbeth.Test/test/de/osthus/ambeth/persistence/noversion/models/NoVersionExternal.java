package de.osthus.ambeth.persistence.noversion.models;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.model.AbstractBusinessObject;

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
