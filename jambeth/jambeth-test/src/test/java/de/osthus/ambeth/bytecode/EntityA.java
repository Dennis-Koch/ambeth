package de.osthus.ambeth.bytecode;

/**
 * Entity based on a class with default constructor
 */
public abstract class EntityA
{
	public abstract Long getId();

	protected String name;

	protected String value;

	protected EntityA()
	{
		// Intended blank
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
	public EntityA setValue(String value)
	{
		this.value = value;
		return this;
	}
}
