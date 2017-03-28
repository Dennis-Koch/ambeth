package com.koch.ambeth.merge;

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
 * Gets called by the Merge Process as soon as the (potentially asynchronous) merge operation is
 * finished.
 */
public interface MergeFinishedCallback {
	/**
	 *
	 * @param success true if the merge operation finished without errors and the transition to any
	 *        local entities as been processed
	 */
	void invoke(boolean success);
}
