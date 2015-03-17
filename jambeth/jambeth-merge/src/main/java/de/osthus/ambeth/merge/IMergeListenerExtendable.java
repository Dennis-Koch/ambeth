package de.osthus.ambeth.merge;


public interface IMergeListenerExtendable
{
	void registerMergeListener(IMergeListener mergeListener);

	void unregisterMergeListener(IMergeListener mergeListener);
}
