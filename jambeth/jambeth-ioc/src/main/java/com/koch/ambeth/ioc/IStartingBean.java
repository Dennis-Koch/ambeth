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

/**
 * Interface for beans that need to initialize or check things after all beans have been injected with their dependencies and have been initialized.
 */
public interface IStartingBean
{
	/**
	 * Implement initializations and checks in this method to be run after all beans have been prepared and initialized by the IoC container.
	 * 
	 * @throws Throwable
	 */
	void afterStarted() throws Throwable;
}
