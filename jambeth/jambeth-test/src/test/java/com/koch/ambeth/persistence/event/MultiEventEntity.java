package com.koch.ambeth.persistence.event;

import com.koch.ambeth.model.AbstractEntity;

/**
 * Test for multiple save actions in one persistence context
 */
public class MultiEventEntity extends AbstractEntity
{
	private String name;

	protected MultiEventEntity()
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
}
