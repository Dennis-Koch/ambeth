package com.koch.ambeth.orm20.independent.a2b2a;

import com.koch.ambeth.model.AbstractEntity;

public class EntityA extends AbstractEntity
{
	private EntityB b;

	public EntityB getB()
	{
		return b;
	}

	public void setB(EntityB b)
	{
		this.b = b;
	}
}
