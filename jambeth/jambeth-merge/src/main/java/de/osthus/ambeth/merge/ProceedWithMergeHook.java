package de.osthus.ambeth.merge;

import de.osthus.ambeth.merge.model.ICUDResult;

public interface ProceedWithMergeHook
{
	boolean checkToProceed(ICUDResult result);
}
