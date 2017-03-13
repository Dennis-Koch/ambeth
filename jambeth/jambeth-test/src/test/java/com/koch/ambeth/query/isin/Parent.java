package com.koch.ambeth.query.isin;

import java.util.List;

import com.koch.ambeth.model.AbstractEntity;

public class Parent extends AbstractEntity
{
	protected List<Child> children;

	protected Parent()
	{
		// Intended blank
	}

	public List<Child> getChildren()
	{
		return children;
	}

	public void setChildren(List<Child> children)
	{
		this.children = children;
	}
}
