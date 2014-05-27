package de.osthus.ambeth.relations.many.lazy.fk.reverse.none;

import java.util.List;

import de.osthus.ambeth.model.AbstractEntity;

public class EntityB extends AbstractEntity
{
	protected String name;

	protected List<EntityA> entityAs;

	protected EntityB()
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

	public List<EntityA> getEntityAs()
	{
		return entityAs;
	}

	public void setEntityAs(List<EntityA> entityAs)
	{
		this.entityAs = entityAs;
	}
}
