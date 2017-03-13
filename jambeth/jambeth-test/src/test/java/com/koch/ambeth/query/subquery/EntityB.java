package com.koch.ambeth.query.subquery;

import com.koch.ambeth.model.AbstractEntity;

public class EntityB extends AbstractEntity
{
	protected String buid;

	protected EntityB()
	{
		// Intended blank
	}

	public String getBuid()
	{
		return buid;
	}

	public void setBuid(String buid)
	{
		this.buid = buid;
	}
}
