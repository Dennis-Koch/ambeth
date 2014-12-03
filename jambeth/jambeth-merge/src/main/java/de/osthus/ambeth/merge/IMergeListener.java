package de.osthus.ambeth.merge;

import de.osthus.ambeth.merge.model.ICUDResult;

public interface IMergeListener
{
	ICUDResult preMerge(ICUDResult cudResult);

	void postMerge(ICUDResult cudResult);
}
