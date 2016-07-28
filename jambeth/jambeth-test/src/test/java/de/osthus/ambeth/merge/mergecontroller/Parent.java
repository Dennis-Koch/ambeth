package de.osthus.ambeth.merge.mergecontroller;

import java.util.List;
import java.util.Set;

import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.model.AbstractEntity;

public abstract class Parent extends AbstractEntity
{
	protected Child child;

	protected String name;

	protected IEntityFactory abc;

	protected Parent(IEntityFactory entityFactory)
	{
		abc = entityFactory;
		// Intended blank
	}

	public Child getChild()
	{
		return child;
	}

	public void setChild(Child child)
	{
		this.child = child;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public abstract List<Child> getOtherChildren();

	public abstract Parent setOtherChildren(List<Child> children);

	public abstract Set<Child> getOtherChildren2();

	public abstract Parent setOtherChildren2(Set<Child> children2);
}
