package com.koch.ambeth.persistence.jdbc.alternateid;

import com.koch.ambeth.model.AbstractEntity;

public class BaseEntity extends AbstractEntity
{
	protected String name;

	protected AlternateIdEntity alternateIdEntity;

	protected BaseEntity()
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
