package de.osthus.ambeth.databinding;

public interface ICollectionChangeExtensionExtendable
{
	void registerCollectionChangeExtension(ICollectionChangeExtension collectionChangeExtension, Class<?> entityType);

	void unregisterCollectionChangeExtension(ICollectionChangeExtension collectionChangeExtension, Class<?> entityType);
}
