package de.osthus.ambeth.relations.one.lazy.fk.reverse.none;

import de.osthus.ambeth.model.AbstractEntity;

public class EntityB extends AbstractEntity
{
	protected String name;

	protected EntityA entityA;

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

	public EntityA getEntityA()
	{
		return entityA;
	}

	public void setEntityA(EntityA entityA)
	{
		this.entityA = entityA;
	}
}
