package com.koch.ambeth.cache.datachange;

/*-
 * #%L
 * jambeth-cache-datachange
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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.merge.IRevertChangesHelper;
import com.koch.ambeth.merge.IRevertChangesSavepoint;
import com.koch.ambeth.merge.RevertChangesFinishedCallback;

public class RevertChangesHelper implements IRevertChangesHelper, IInitializingBean {
	@Override
	public void afterPropertiesSet() throws Throwable {
	}

	@Override
	public IRevertChangesSavepoint createSavepoint(Object source) {
		// Not yet implemented. Ignoring operation intentionally
		return null;
	}

	@Override
	public void revertChanges(Object objectsToRevert) {
		// Not yet implemented. Ignoring operation intentionally
	}

	@Override
	public void revertChanges(Object objectsToRevert, boolean recursive) {
		// Not yet implemented. Ignoring operation intentionally
	}

	@Override
	public void revertChanges(Object objectsToRevert,
			RevertChangesFinishedCallback revertChangesFinishedCallback) {
		// Not yet implemented. Ignoring operation intentionally
	}

	@Override
	public void revertChanges(Object objectsToRevert,
			RevertChangesFinishedCallback revertChangesFinishedCallback, boolean recursive) {
		// Not yet implemented. Ignoring operation intentionally
	}

	@Override
	public void revertChangesGlobally(Object objectsToRevert) {
		// Not yet implemented. Ignoring operation intentionally
	}

	@Override
	public void revertChangesGlobally(Object objectsToRevert, boolean recursive) {
		// Not yet implemented. Ignoring operation intentionally
	}

	@Override
	public void revertChangesGlobally(Object objectsToRevert,
			RevertChangesFinishedCallback revertChangesFinishedCallback) {
		// Not yet implemented. Ignoring operation intentionally
	}

	@Override
	public void revertChangesGlobally(Object objectsToRevert,
			RevertChangesFinishedCallback revertChangesFinishedCallback, boolean recursive) {
		// Not yet implemented. Ignoring operation intentionally
	}
}
