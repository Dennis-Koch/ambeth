package de.osthus.ambeth.merge;

import de.osthus.ambeth.merge.incremental.IIncrementalMergeState;
import de.osthus.ambeth.merge.model.ICUDResult;

public interface ICUDResultPrinter
{
	CharSequence printCUDResult(ICUDResult cudResult, IIncrementalMergeState state);
}