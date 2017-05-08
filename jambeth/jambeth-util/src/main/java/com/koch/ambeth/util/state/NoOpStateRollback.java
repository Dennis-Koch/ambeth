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


public final class NoOpStateRollback implements IStateRollback {
	public static final IStateRollback instance = new NoOpStateRollback();

	public static IStateRollback createNoOpRollback(IStateRollback[] rollbacks) {
		if (rollbacks == null || rollbacks.length == 0) {
			return instance;
		}
		if (rollbacks.length == 1) {
			return rollbacks[0];
		}
		return new AbstractStateRollback(rollbacks) {
			@Override
			protected void rollbackIntern() throws Throwable {
				// intended blank
			}
		};
	}

	private NoOpStateRollback() {
		// Intended blank
	}

	@Override
	public void rollback() {
		// intended blank
	}
}
