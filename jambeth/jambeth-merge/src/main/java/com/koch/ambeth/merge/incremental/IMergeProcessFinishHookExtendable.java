package com.koch.ambeth.merge.incremental;

public interface IMergeProcessFinishHookExtendable {

	void registerMergePipelineFinishHook(IMergePipelineFinishHook hook);

	void unregisterMergePipelineFinishHook(IMergePipelineFinishHook hook);
}
