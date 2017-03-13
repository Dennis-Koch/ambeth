package com.koch.ambeth.bytecode;

import com.koch.ambeth.merge.IEntityFactory;

/**
 * Entity based on a class with non default constructor. If an entity does not have a default constructor EntityManager expects a constructor having
 * {@link IEntityFactory} as parameter. Other constructors are not used by EntityManager.
 */
public class EntityC extends AbstractEntity
{
	protected Long id;

	protected String name;

	protected String value;

	protected EntityC(IEntityFactory entityFactory)
	{
		super(entityFactory);
	}

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	/**
	 * "Normal" api
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	public String getValue()
	{
		return value;
	}

	/**
	 * "Fluent" api
	 */
	public EntityC setValue(String value)
	{
		this.value = value;
		return this;
	}
}
