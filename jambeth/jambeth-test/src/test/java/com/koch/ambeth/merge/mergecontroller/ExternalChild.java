package com.koch.ambeth.merge.mergecontroller;

import com.koch.ambeth.model.AbstractEntity;
import com.koch.ambeth.util.annotation.PropertyChangeAspect;

@PropertyChangeAspect
public abstract class ExternalChild extends AbstractEntity
{
	protected ExternalChild()
	{
		// Intended blank
	}

	public abstract String getName();

	public abstract void setName(String name);
}
