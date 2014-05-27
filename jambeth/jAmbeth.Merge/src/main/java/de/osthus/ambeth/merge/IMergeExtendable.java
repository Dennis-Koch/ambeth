package de.osthus.ambeth.merge;

public interface IMergeExtendable
{

	void registerMergeExtension(IMergeExtension mergeExtension);

	void unregisterMergeExtension(IMergeExtension mergeExtension);

}
