package de.osthus.ambeth.merge.model;

import de.osthus.ambeth.typeinfo.IPrimitiveValueProvider;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;

public interface IPrimitiveMember extends ITypeInfoItem
{
	void setPrimitiveValue(Object obj, IPrimitiveValueProvider primitiveValueProvider, int index);
}
