package de.osthus.ambeth.merge;

public interface IEntityInstantiationExtensionExtendable
{
	void registerEntityInstantiationExtension(IEntityInstantiationExtension entityInstantiationExtension, Class<?> type);

	void unregisterEntityInstantiationExtension(IEntityInstantiationExtension entityInstantiationExtension, Class<?> type);
}
