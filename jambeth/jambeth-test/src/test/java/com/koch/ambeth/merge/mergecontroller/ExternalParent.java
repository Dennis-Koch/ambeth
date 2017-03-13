package com.koch.ambeth.merge.mergecontroller;

import java.util.List;

import com.koch.ambeth.model.AbstractEntity;
import com.koch.ambeth.util.annotation.PropertyChangeAspect;

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
