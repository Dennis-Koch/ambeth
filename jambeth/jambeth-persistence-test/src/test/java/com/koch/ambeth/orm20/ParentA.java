package com.koch.ambeth.orm20;

import com.koch.ambeth.model.AbstractEntity;

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
