package com.koch.ambeth.persistence.find;

import com.koch.ambeth.model.AbstractEntity;

public class Entity extends AbstractEntity
{
	public static final String ALTERNATE_ID = "AlternateId";

	private String alternateId;

	public String getAlternateId()
	{
		return alternateId;
	}

	public void setAlternateId(String alternateId)
	{
		this.alternateId = alternateId;
	}
}
