package com.koch.ambeth.persistence.update;

import com.koch.ambeth.model.AbstractEntity;

public class EntityB extends AbstractEntity
{
	protected EntityA other;

	protected EntityB()
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
