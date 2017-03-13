package com.koch.ambeth.model;

import com.koch.ambeth.util.annotation.EntityEqualsAspect;

@EntityEqualsAspect
public abstract class AbstractBusinessObject
{
	protected AbstractBusinessObject()
	{
		// Intended blank
	}

	public abstract int getId();
}
