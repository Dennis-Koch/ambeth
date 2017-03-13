package com.koch.ambeth.mapping;

public class EntityB
{
	protected int id;

	protected String nameOfB;

	public String getNameOfB()
	{
		return nameOfB;
	}

	public void setNameOfB(String nameOfB)
	{
		this.nameOfB = nameOfB;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public EntityA getEntityA()
	{
		return entityA;
	}

	public void setEntityA(EntityA entityA)
	{
		this.entityA = entityA;
	}

	protected EntityA entityA;

}
