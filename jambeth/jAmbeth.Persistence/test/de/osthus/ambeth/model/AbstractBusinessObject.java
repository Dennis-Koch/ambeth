package de.osthus.ambeth.model;

import de.osthus.ambeth.annotation.EntityEqualsAspect;

@EntityEqualsAspect
public abstract class AbstractBusinessObject
{
	protected AbstractBusinessObject()
	{
		// Intended blank
	}

	public abstract int getId();
}
