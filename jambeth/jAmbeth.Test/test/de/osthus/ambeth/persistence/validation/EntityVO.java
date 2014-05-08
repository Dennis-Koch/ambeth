package de.osthus.ambeth.persistence.validation;

import de.osthus.ambeth.model.AbstractEntity;

public class EntityVO extends AbstractEntity
{
	String name;

	String noDb;

	protected EntityVO()
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
