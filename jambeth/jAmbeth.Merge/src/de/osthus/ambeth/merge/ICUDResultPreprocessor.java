package de.osthus.ambeth.merge;

public interface ICUDResultPreprocessor
{
	ProceedWithMergeHook getProceedWithMergeHook();

	void cleanUp(ProceedWithMergeHook proceedWithMergeHook);

	// Returns null, if preprocessing has not yet finished
	Boolean getPreprocessSuccess(ProceedWithMergeHook proceedWithMergeHook);
}
