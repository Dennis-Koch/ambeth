package com.koch.ambeth.util.exception;

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

public interface IExceptionHandler
{
	/**
	 * This method must not throw an exception of any type by itself. The calling convention expects a robust handler in this case. Any inconsistent state will
	 * be handled outside of the scope of the exception handler (before or after the call).
	 * 
	 * @param e
	 *            Throwable to handle robustly
	 */
	void handleException(Throwable e);
}
