package de.osthus.ambeth.bytecode;

/**
 * Entity based on a class with default and non default constructor
 */
public class EntityB extends EntityA
{
	protected EntityB()
	{
		// Intended blank
	}

	protected EntityB(String name, String value)
	{
		this.name = name;
		this.value = value;
	}
}
