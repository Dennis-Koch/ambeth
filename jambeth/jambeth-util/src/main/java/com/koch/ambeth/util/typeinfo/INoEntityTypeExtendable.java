package com.koch.ambeth.util.typeinfo;

public interface INoEntityTypeExtendable
{
	void registerNoEntityType(Class<?> noEntityType);

	void unregisterNoEntityType(Class<?> noEntityType);
}
