package com.koch.ambeth.core.start;

/*-
 * #%L
 * jambeth-core
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

import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;

public interface IAmbethConfigurationIntern extends IAmbethConfiguration {
	/**
	 * Adds the provided module classes to the list of modules to start with the framework context.
	 *
	 * @param modules Ambeth modules
	 * @return This configuration object
	 */
	@SuppressWarnings("unchecked")
	IAmbethConfigurationIntern withAmbethModules(
			IBackgroundWorkerParamDelegate<IBeanContextFactory>... moduleDelegates);

	/**
	 * Adds the provided module classes to the list of modules to start with the application context.
	 *
	 * @param modules Application modules
	 * @return This configuration object
	 */
	@SuppressWarnings("unchecked")
	IAmbethConfigurationIntern withApplicationModules(
			IBackgroundWorkerParamDelegate<IBeanContextFactory>... moduleDelegates);
}
