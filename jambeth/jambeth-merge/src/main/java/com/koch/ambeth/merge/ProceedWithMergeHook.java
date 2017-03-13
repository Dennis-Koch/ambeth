package com.koch.ambeth.merge;

import com.koch.ambeth.merge.model.ICUDResult;

public interface ProceedWithMergeHook
{
	boolean checkToProceed(ICUDResult result);
}
