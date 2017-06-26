package com.koch.ambeth.security.threading;

/*-
 * #%L
 * jambeth-security
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

import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

/**
 * This execution is used in order to activate all proxies (especially the SecurityProxy)
 */
public interface IBackgroundAuthenticatingExecution {
	void execute(IBackgroundWorkerDelegate runnable) throws Exception;

	<T> T execute(IResultingBackgroundWorkerDelegate<T> runnable) throws Exception;
}
