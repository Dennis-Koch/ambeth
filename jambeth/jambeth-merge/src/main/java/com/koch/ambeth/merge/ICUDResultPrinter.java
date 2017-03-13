package com.koch.ambeth.merge;

import com.koch.ambeth.merge.incremental.IIncrementalMergeState;
import com.koch.ambeth.merge.model.ICUDResult;

public interface ICUDResultPrinter
{
	CharSequence printCUDResult(ICUDResult cudResult, IIncrementalMergeState state);
}