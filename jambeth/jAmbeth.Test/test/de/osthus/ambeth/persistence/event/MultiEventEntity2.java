package de.osthus.ambeth.persistence.event;

import de.osthus.ambeth.model.AbstractEntity;

/**
 * Test for multiple save actions in one persistence context
 */
public class MultiEventEntity2 extends AbstractEntity
{
	private String name;

	protected MultiEventEntity2()
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
