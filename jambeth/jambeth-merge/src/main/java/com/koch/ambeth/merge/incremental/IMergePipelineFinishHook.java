package com.koch.ambeth.merge.incremental;

@FunctionalInterface
public interface IMergePipelineFinishHook {
	void mergePipelineFinished(boolean success, IIncrementalMergeState incrementalMergeState);
}
