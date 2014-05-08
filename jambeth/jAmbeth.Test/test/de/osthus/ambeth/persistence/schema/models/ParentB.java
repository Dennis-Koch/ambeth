package de.osthus.ambeth.persistence.schema.models;

import de.osthus.ambeth.model.AbstractEntity;

public class ParentB extends AbstractEntity
{
	protected ChildB child;

	protected ParentB()
	{
		// Intended blank
	}

	public ChildB getChild()
	{
		return child;
	}

	public void setChild(ChildB child)
	{
		this.child = child;
	}
}
