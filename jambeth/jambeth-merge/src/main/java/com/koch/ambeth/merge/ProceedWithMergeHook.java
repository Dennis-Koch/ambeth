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

import com.koch.ambeth.merge.model.ICUDResult;
import com.koch.ambeth.merge.service.IMergeService;

/**
 * Allows to intercept the Merge Process after the initial transition of "old state" to "new state"
 * has been resolved in the entity graph.
 */
public interface ProceedWithMergeHook {
	/**
	 *
	 * @param result The object describing the transition from "old state" to "new state" in the
	 *        entity graph.
	 * @return true if the Merge Process is allowed to proceed (means: call the {@link IMergeService}
	 *         layer).
	 */
	boolean checkToProceed(ICUDResult result);
}
