package de.osthus.ambeth.merge;

import de.osthus.ambeth.merge.model.ICUDResult;

public interface IMergeListener
{
	void preMerge(ICUDResult cudResult);

	void postMerge(ICUDResult cudResult);
}
