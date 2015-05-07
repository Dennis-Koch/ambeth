package de.osthus.ambeth.typeinfo;

public interface INoEntityTypeExtendable
{
	void registerNoEntityType(Class<?> noEntityType);

	void unregisterNoEntityType(Class<?> noEntityType);
}
