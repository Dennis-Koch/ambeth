package de.osthus.ambeth.merge;

public interface IEntityInstantiationExtensionExtendable
{
	void registerEntityFactoryExtension(IEntityInstantiationExtension entityFactoryExtension, Class<?> type);

	void unregisterEntityFactoryExtension(IEntityInstantiationExtension entityFactoryExtension, Class<?> type);
}
