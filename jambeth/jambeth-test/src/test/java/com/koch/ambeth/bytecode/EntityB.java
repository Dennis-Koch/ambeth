package com.koch.ambeth.bytecode;

/**
 * Entity based on a class with default and non default constructor
 */
public class EntityB extends EntityA
{
	private Long id;

	protected EntityB()
	{
		// Intended blank
	}

	@Override
	public Long getId()
	{
		return id;
	}

	public EntityB setId(Long id)
	{
		this.id = id;
		return this;
	}

	protected EntityB(String name, String value)
	{
		this.name = name;
		this.value = value;
	}
}
