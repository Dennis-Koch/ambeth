package de.osthus.ambeth.query.subquery;

import de.osthus.ambeth.model.AbstractEntity;

public class EntityA extends AbstractEntity
{
	protected String buid;

	protected EntityB entityB;

	protected EntityA()
	{
		// Intended blank
	}

	public String getBuid()
	{
		return buid;
	}

	public void setBuid(String buid)
	{
		this.buid = buid;
	}

	public EntityB getEntityB()
	{
		return entityB;
	}

	public void setEntityB(EntityB entityB)
	{
		this.entityB = entityB;
	}
}
