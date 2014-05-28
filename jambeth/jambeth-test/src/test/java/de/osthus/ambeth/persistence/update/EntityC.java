package de.osthus.ambeth.persistence.update;

import de.osthus.ambeth.model.AbstractEntity;

public class EntityC extends AbstractEntity
{
	protected EntityA other;

	protected EntityC()
	{
		// Intended blank
	}

	public EntityA getOther()
	{
		return other;
	}

	public void setOther(EntityA other)
	{
		this.other = other;
	}
}
