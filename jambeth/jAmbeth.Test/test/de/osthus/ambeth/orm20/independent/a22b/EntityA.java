package de.osthus.ambeth.orm20.independent.a22b;

import de.osthus.ambeth.model.AbstractEntity;

public class EntityA extends AbstractEntity
{
	private EntityB b1;

	private EntityB b2;

	public EntityB getB1()
	{
		return b1;
	}

	public void setB1(EntityB b1)
	{
		this.b1 = b1;
	}

	public EntityB getB2()
	{
		return b2;
	}

	public void setB2(EntityB b2)
	{
		this.b2 = b2;
	}
}
