package de.osthus.ambeth.relations.one.lazy.fk.reverse.none;

import de.osthus.ambeth.model.AbstractEntity;

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
