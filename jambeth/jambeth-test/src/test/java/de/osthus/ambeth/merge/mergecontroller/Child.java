package de.osthus.ambeth.merge.mergecontroller;

import de.osthus.ambeth.model.AbstractEntity;

public abstract class Child extends AbstractEntity
{
	protected Child()
	{
		// Intended blank
	}

	public abstract String getName();

	public abstract void setName(String name);
}
