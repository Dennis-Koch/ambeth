package de.osthus.ambeth.orm20;

import de.osthus.ambeth.model.AbstractEntity;

public class ParentA extends AbstractEntity
{
	protected ChildA child;

	protected ParentA()
	{
		// Intended blank
	}

	public ChildA getChild()
	{
		return child;
	}

	public void setChild(ChildA child)
	{
		this.child = child;
	}
}
