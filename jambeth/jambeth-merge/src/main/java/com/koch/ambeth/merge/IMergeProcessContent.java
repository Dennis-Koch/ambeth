package com.koch.ambeth.merge;

public interface IMergeProcessContent extends IMergeProcessStarted {
	/**
	 * Executes the previously configured merge process instance on the stack
	 */
	void finish();

	@Override
	IMergeProcessContent onLocalDiff(ProceedWithMergeHook hook);

	@Override
	IMergeProcessContent onSuccess(MergeFinishedCallback callback);

	@Override
	IMergeProcessContent suppressNewEntitiesAddedToCache();

	@Override
	IMergeProcessContent shallow();
}
