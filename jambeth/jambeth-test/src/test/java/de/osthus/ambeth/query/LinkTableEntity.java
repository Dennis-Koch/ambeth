package de.osthus.ambeth.query;

import de.osthus.ambeth.model.AbstractEntity;

public class LinkTableEntity extends AbstractEntity
{
	public static final String Name = "Name";

	protected String name;

	protected LinkTableEntity()
	{
		// Intended blank
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
