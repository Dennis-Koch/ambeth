package com.koch.ambeth.merge.changecontroller;

import com.koch.ambeth.merge.incremental.IIncrementalMergeState;

/*-
 * #%L
 * jambeth-merge
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

/**
 * Marker interface for rules that want to work in a batch mode and want to get notified for a
 * complete chain of steps of the complete lifecycle of a single incremental merge pipeline. Just
 * implement this interface on any custom {@link IChangeControllerExtension}.
 */
public interface IMergePipelineListener {
	/**
	 * Callback to notify the extension that a new batch sequence is started
	 */
	void queuePipeline(IIncrementalMergeState incrementalMergeState);

	/**
	 * Callback to notify the extension that the previous incremental merge pipeline called with
	 * "queue" is now finished.
	 */
	void flushPipeline(IIncrementalMergeState incrementalMergeState);

	/**
	 * Callback to notify the extension that the previous incremental merge pipeline called with
	 * "queue" is reverted (mostly due to an exception).
	 */
	void rollbackPipeline(IIncrementalMergeState incrementalMergeState);
}
