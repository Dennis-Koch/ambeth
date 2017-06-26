package com.koch.ambeth.util.state;

/*-
 * #%L
 * jambeth-util
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

import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public abstract class AbstractStateRollback implements IStateRollback {
	private final IStateRollback[] rollbacks;

	private boolean rollbackCalled;

	public AbstractStateRollback(IStateRollback[] rollbacks) {
		this.rollbacks = rollbacks;
	}

	@Override
	public final void rollback() {
		if (rollbackCalled) {
			throw new IllegalStateException("rollback() has already been called");
		}
		rollbackCalled = true;
		try {
			rollbackIntern();
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		finally {
			for (int a = rollbacks.length; a-- > 0;) {
				IStateRollback rollback = rollbacks[a];
				if (rollback == null) {
					continue;
				}
				rollback.rollback();
			}
		}
	}

	protected abstract void rollbackIntern() throws Exception;
}
