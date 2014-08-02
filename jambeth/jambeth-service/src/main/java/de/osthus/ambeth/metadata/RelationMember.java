package de.osthus.ambeth.metadata;

import de.osthus.ambeth.annotation.CascadeLoadMode;
import de.osthus.ambeth.typeinfo.IPropertyInfo;

public abstract class RelationMember extends Member
{
	protected RelationMember(Class<?> type, IPropertyInfo property)
	{
		super(type, property);
	}

	@Override
	public Object getNullEquivalentValue()
	{
		return null;
	}

	public abstract CascadeLoadMode getCascadeLoadMode();

	public abstract boolean isToMany();

	public abstract boolean isManyTo();
}
