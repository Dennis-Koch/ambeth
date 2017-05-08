package com.koch.ambeth.cache.mock;

/*-
 * #%L
 * jambeth-merge-test
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

import com.koch.ambeth.merge.IRevertChangesHelper;
import com.koch.ambeth.merge.IRevertChangesSavepoint;
import com.koch.ambeth.merge.RevertChangesFinishedCallback;

/**
 * Support for unit tests that do not include jAmbeth.Cache
 */
public class RevertChangesHelperMock implements IRevertChangesHelper {
	@Override
	public IRevertChangesSavepoint createSavepoint(Object source) {
		return null;
	}

	@Override
	public IRevertChangesSavepoint createSavepoint(Object... sources) {
		return null;
	}

	@Override
	public void revertChanges(Object objectsToRevert) {
	}

	@Override
	public void revertChanges(Object objectsToRevert, boolean recursive) {
	}

	@Override
	public void revertChanges(Object objectsToRevert,
			RevertChangesFinishedCallback revertChangesFinishedCallback) {
	}

	@Override
	public void revertChanges(Object objectsToRevert,
			RevertChangesFinishedCallback revertChangesFinishedCallback, boolean recursive) {
	}

	@Override
	public void revertChangesGlobally(Object objectsToRevert) {
	}

	@Override
	public void revertChangesGlobally(Object objectsToRevert, boolean recursive) {
	}

	@Override
	public void revertChangesGlobally(Object objectsToRevert,
			RevertChangesFinishedCallback revertChangesFinishedCallback) {
	}

	@Override
	public void revertChangesGlobally(Object objectsToRevert,
			RevertChangesFinishedCallback revertChangesFinishedCallback, boolean recursive) {
	}
}
