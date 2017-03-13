package com.koch.ambeth.relations.many.lazy.fk.forward.none;

import com.koch.ambeth.model.AbstractEntity;

public class EntityA extends AbstractEntity
{
	protected String name;

	protected EntityA()
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
