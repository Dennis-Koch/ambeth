package de.osthus.ambeth.bytecode;

import de.osthus.ambeth.merge.IEntityFactory;

/**
 * Entity based on a class with non default constructor. If an entity does not have a default constructor EntityManager expects a constructor having
 * {@link IEntityFactory} as parameter. Other constructors are not used by EntityManager.
 */
public class EntityC extends AbstractEntity
{
	protected String name;

	protected String value;

	protected EntityC(IEntityFactory entityFactory)
	{
		super(entityFactory);
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
