package com.koch.ambeth.merge.incremental;

public interface IMergeProcessFinishListenerExtendable {

	void registerMergeProcessFinishListener(IMergePipelineFinishListener extension);

	void unregisterMergeProcessFinishListener(IMergePipelineFinishListener extension);
}
