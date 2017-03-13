package com.koch.ambeth.orm20;

import com.koch.ambeth.model.AbstractEntity;

public class Boat extends AbstractEntity
{
	protected String name;

	protected Boat()
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
