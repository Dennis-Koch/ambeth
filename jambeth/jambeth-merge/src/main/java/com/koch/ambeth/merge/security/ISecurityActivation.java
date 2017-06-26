package com.koch.ambeth.merge.security;

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

import java.util.Set;

import com.koch.ambeth.util.state.IStateRollback;
import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

public interface ISecurityActivation {
	boolean isSecured();

	boolean isServiceSecurityEnabled();

	boolean isFilterActivated();

	void executeWithSecurityDirective(Set<SecurityDirective> securityDirective,
			IBackgroundWorkerDelegate runnable) throws Exception;

	<R> R executeWithSecurityDirective(Set<SecurityDirective> securityDirective,
			IResultingBackgroundWorkerDelegate<R> runnable) throws Exception;

	void executeWithoutSecurity(IBackgroundWorkerDelegate pausedSecurityRunnable) throws Exception;

	<R> R executeWithoutSecurity(IResultingBackgroundWorkerDelegate<R> pausedSecurityRunnable)
			throws Exception;

	void executeWithoutFiltering(IBackgroundWorkerDelegate noFilterRunnable) throws Exception;

	<R> R executeWithoutFiltering(IResultingBackgroundWorkerDelegate<R> noFilterRunnable)
			throws Exception;

	IStateRollback pushWithSecurityDirective(Set<SecurityDirective> securityDirective,
			IStateRollback... rollbacks);

	IStateRollback pushWithoutSecurity(IStateRollback... rollbacks);

	IStateRollback pushWithoutFiltering(IStateRollback... rollbacks);
}
