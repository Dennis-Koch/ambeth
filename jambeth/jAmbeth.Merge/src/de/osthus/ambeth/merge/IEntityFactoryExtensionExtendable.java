package de.osthus.ambeth.merge;

public interface IEntityFactoryExtensionExtendable
{
	void registerEntityFactoryExtension(IEntityFactoryExtension entityFactoryExtension, Class<?> type);

	void unregisterEntityFactoryExtension(IEntityFactoryExtension entityFactoryExtension, Class<?> type);
}
