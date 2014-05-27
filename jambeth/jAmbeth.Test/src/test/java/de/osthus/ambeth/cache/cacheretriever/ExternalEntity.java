package de.osthus.ambeth.cache.cacheretriever;

import de.osthus.ambeth.model.AbstractEntity;

public class ExternalEntity extends AbstractEntity
{
	protected String name;

	protected int value;

	protected ExternalEntity parent;

	protected ExternalEntity()
	{
		// Intended blank
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public int getValue()
	{
		return value;
	}

	public void setValue(int value)
	{
		this.value = value;
	}

	public ExternalEntity getParent()
	{
		return parent;
	}

	public void setParent(ExternalEntity parent)
	{
		this.parent = parent;
	}
}
