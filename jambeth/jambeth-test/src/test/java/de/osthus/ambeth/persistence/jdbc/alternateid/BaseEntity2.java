package de.osthus.ambeth.persistence.jdbc.alternateid;

import de.osthus.ambeth.model.AbstractEntity;

public class BaseEntity2 extends AbstractEntity
{
	protected String name;

	protected AlternateIdEntity alternateIdEntity;

	protected BaseEntity2()
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

	public AlternateIdEntity getAlternateIdEntity()
	{
		return alternateIdEntity;
	}

	public void setAlternateIdEntity(AlternateIdEntity alternateIdEntity)
	{
		this.alternateIdEntity = alternateIdEntity;
	}
}
