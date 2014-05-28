package de.osthus.ambeth.cache;

import de.osthus.ambeth.typeinfo.ITypeInfoItem;

public interface IParentEntityAware
{
	void setParentEntity(Object parentEntity, ITypeInfoItem member);
}
