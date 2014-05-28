package de.osthus.ambeth.merge;

public interface IMergeProcess
{
	void process(Object objectToMerge, Object objectToDelete, ProceedWithMergeHook proceedHook, MergeFinishedCallback mergeFinishedCallback);
}
