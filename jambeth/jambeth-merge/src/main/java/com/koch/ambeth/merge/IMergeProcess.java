package com.koch.ambeth.merge;

public interface IMergeProcess
{
	void process(Object objectToMerge, Object objectToDelete, ProceedWithMergeHook proceedHook, MergeFinishedCallback mergeFinishedCallback);

	void process(Object objectToMerge, Object objectToDelete, ProceedWithMergeHook proceedHook, MergeFinishedCallback mergeFinishedCallback,
			boolean addNewEntitiesToCache);
}
