package com.koch.ambeth.persistence.validation;

import com.koch.ambeth.model.AbstractEntity;

public class Entity extends AbstractEntity
{
	String name;

	String noDb;

	protected Entity()
	{
		// Intended blank
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getNoDb()
	{
		return noDb;
	}

	public void setNoDb(String noDb)
	{
		this.noDb = noDb;
	}
}
