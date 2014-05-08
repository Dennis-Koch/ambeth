package de.osthus.ambeth.cache.bytecode;

import java.util.List;

import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.model.AbstractEntity;

public abstract class TestEntity extends AbstractEntity
{
	protected List<TestEntity> f_children;

	protected List<TestEntity> childrenWithProtectedField;

	protected List<TestEntity> childrenWithPrivateField;

	protected TestEntity(IEntityFactory entityFactory)
	{
		super();
	}

	public List<TestEntity> getChildrenNoField()
	{
		return f_children;
	}

	public void setChildrenNoField(List<TestEntity> children)
	{
		this.f_children = children;
	}

	public List<TestEntity> getChildrenWithProtectedField()
	{
		return childrenWithProtectedField;
	}

	public void setChildrenWithProtectedField(List<TestEntity> childrenWithProtectedField)
	{
		this.childrenWithProtectedField = childrenWithProtectedField;
	}

	public List<TestEntity> getChildrenWithPrivateField()
	{
		return childrenWithPrivateField;
	}

	public void setChildrenWithPrivateField(List<TestEntity> childrenWithPrivateField)
	{
		this.childrenWithPrivateField = childrenWithPrivateField;
	}
}
