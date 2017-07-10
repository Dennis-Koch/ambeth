package com.koch.ambeth.util.state;

import java.util.List;

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
	public static void executeRollbacksReverse(IStateRollback rollbacks) {
		if (rollbacks != null) {
			rollbacks.rollback();
		}
	}

	public static void executeRollbacksReverse(IStateRollback[] rollbacks) {
		if (rollbacks == null) {
			return;
		}
		for (int a = rollbacks.length; a-- > 0;) {
			IStateRollback rollback = rollbacks[a];
			if (rollback == null) {
				continue;
			}
			rollback.rollback();
		}
	}

	public static void executeRollbacksReverse(List<? extends IStateRollback> rollbacks) {
		if (rollbacks == null) {
			return;
		}
		for (int a = rollbacks.size(); a-- > 0;) {
			IStateRollback rollback = rollbacks.get(a);
			if (rollback == null) {
				continue;
			}
			rollback.rollback();
		}
	}

	private final IStateRollback[] rollbacks;

	private final IStateRollback rollback;

	private boolean rollbackCalled;

	public AbstractStateRollback(IStateRollback rollback) {
		rollbacks = null;
		this.rollback = rollback;
	}

	public AbstractStateRollback(IStateRollback... rollbacks) {
		IStateRollback rollback = null;
		if (rollbacks == null || rollbacks.length == 0) {
			rollbacks = null;
		}
		else if (rollbacks.length == 1) {
			rollback = rollbacks[0];
			rollbacks = null;
		}
		this.rollbacks = rollbacks;
		this.rollback = rollback;
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
			if (rollback != null) {
				rollback.rollback();
			}
			else {
				executeRollbacksReverse(rollbacks);
			}
		}
	}

	protected abstract void rollbackIntern() throws Exception;
}
