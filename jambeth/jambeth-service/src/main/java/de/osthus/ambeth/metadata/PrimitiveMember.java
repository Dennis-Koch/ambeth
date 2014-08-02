package de.osthus.ambeth.metadata;

import de.osthus.ambeth.typeinfo.IPropertyInfo;

public abstract class PrimitiveMember extends Member
{
	protected PrimitiveMember(Class<?> type, IPropertyInfo property)
	{
		super(type, property);
	}

	public abstract boolean isTechnicalMember();
}
