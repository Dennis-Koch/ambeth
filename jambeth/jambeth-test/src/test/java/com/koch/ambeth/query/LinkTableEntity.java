package com.koch.ambeth.query;

import com.koch.ambeth.model.AbstractEntity;

public class LinkTableEntity extends AbstractEntity
{
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
