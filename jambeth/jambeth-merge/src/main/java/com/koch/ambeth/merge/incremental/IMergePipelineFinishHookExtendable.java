package com.koch.ambeth.merge.incremental;

public interface IMergePipelineFinishHookExtendable {

	void registerMergePipelineFinishHook(IMergePipelineFinishHook hook);

	void unregisterMergePipelineFinishHook(IMergePipelineFinishHook hook);
}
