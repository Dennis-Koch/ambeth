package de.osthus.ambeth.orm20.independent.a3b;

import de.osthus.ambeth.model.AbstractEntity;

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
