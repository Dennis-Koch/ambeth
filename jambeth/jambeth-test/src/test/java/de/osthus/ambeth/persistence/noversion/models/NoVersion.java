package de.osthus.ambeth.persistence.noversion.models;

import de.osthus.ambeth.model.AbstractBusinessObject;

public class NoVersion extends AbstractBusinessObject
{
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
