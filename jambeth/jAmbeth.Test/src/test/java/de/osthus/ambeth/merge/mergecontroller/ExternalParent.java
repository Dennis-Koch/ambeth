package de.osthus.ambeth.merge.mergecontroller;

import java.util.List;

import de.osthus.ambeth.annotation.PropertyChangeAspect;
import de.osthus.ambeth.model.AbstractEntity;

@PropertyChangeAspect
public abstract class ExternalParent extends AbstractEntity
{
	protected String name;

	protected ExternalParent()
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

	public abstract List<ExternalChild> getChildren();

	public abstract ExternalParent setChildren(List<ExternalChild> children);
}
