package com.koch.ambeth.ioc;

/*-
 * #%L
 * jambeth-ioc
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

/**
 * Interface for initializing modules that define and configure the content of the IoC container.
 *
 * @see {@wiki Ambeth Verwendung}
 */
public interface IInitializingModule {
	/**
	 * Called by the starting IoC container after the module has been instantiated and injected with
	 * required beans and properties. Implement this method to register and link beans during
	 * application startup.
	 *
	 * @param beanContextFactory Starting context.
	 * @throws Throwable
	 */
	void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable;
}
