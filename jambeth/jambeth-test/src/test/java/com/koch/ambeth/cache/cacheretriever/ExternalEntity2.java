package com.koch.ambeth.cache.cacheretriever;

import com.koch.ambeth.model.AbstractEntity;

public class ExternalEntity2 extends AbstractEntity
{
	protected String name;

	protected int value;

	protected ExternalEntity parent;

	protected LocalEntity local;

	protected ExternalEntity2()
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

	public LocalEntity getLocal()
	{
		return local;
	}

	public void setLocal(LocalEntity local)
	{
		this.local = local;
	}
}
