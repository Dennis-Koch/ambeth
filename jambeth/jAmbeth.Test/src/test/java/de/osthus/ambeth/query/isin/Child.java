package de.osthus.ambeth.query.isin;

import de.osthus.ambeth.model.AbstractEntity;

public class Child extends AbstractEntity
{
	protected Parent parent;

	protected Child()
	{
		// Intended blank
	}

	public Parent getParent()
	{
		return parent;
	}

	public void setParent(Parent parent)
	{
		this.parent = parent;
	}
}
