package com.koch.ambeth.merge.incremental;

@FunctionalInterface
public interface IMergePipelineFinishListener {
	void mergePipelineFinished(boolean success, IIncrementalMergeState incrementalMergeState);
}
