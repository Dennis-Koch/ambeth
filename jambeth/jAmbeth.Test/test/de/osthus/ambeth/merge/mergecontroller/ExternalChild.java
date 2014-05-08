package de.osthus.ambeth.merge.mergecontroller;

import de.osthus.ambeth.annotation.PropertyChangeAspect;
import de.osthus.ambeth.model.AbstractEntity;

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
