package com.koch.ambeth.merge;


public interface IMergeListenerExtendable
{
	void registerMergeListener(IMergeListener mergeListener);

	void unregisterMergeListener(IMergeListener mergeListener);
}
