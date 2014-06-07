package de.osthus.ambeth.merge;

public interface IMergeServiceExtensionExtendable
{
	void registerMergeServiceExtension(IMergeServiceExtension mergeServiceExtension, Class<?> entityType);

	void unregisterMergeServiceExtension(IMergeServiceExtension mergeServiceExtension, Class<?> entityType);
}
