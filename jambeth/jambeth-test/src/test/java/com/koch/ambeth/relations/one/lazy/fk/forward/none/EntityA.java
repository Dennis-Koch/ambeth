package com.koch.ambeth.relations.one.lazy.fk.forward.none;

import com.koch.ambeth.model.AbstractEntity;

public class EntityA extends AbstractEntity
{
	private String name;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
}
