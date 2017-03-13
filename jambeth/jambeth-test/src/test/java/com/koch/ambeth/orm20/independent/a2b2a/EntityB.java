package com.koch.ambeth.orm20.independent.a2b2a;

import com.koch.ambeth.model.AbstractEntity;

public class EntityB extends AbstractEntity
{
	private EntityA a;

	public EntityA getA()
	{
		return a;
	}

	public void setA(EntityA a)
	{
		this.a = a;
	}

}
