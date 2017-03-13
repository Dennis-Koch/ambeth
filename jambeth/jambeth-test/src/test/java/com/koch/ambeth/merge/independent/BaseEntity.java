package com.koch.ambeth.merge.independent;

public abstract class BaseEntity
{
	protected int id;

	protected int version;

	protected BaseEntity()
	{
		// Intended blank
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public int getVersion()
	{
		return version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}
}
