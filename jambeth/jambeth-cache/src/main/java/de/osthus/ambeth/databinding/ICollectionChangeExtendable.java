package de.osthus.ambeth.databinding;

public interface ICollectionChangeExtendable
{
	void registerCollectionChangeExtension(ICollectionChangeExtension collectionChangeExtension, Class<?> entityType);

	void unregisterCollectionChangeExtension(ICollectionChangeExtension collectionChangeExtension, Class<?> entityType);
}
