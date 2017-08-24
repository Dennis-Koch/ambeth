package com.koch.ambeth.ioc.link;

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
 * Last step of the fluent interface for a link configuration at runtime.
 *
 * @see ILinkRuntimeExtendable
 */
public interface ILinkRuntimeFinish {
	/**
	 * Finishes the link configuration and executes it. Note that it is NOT necessary to call
	 * {@link ILinkContainer#link()} on the result.
	 *
	 * @return The already executed (linked) handle. Allows to unlink() it before the IoC context
	 *         would unlink it during its end of life
	 */
	ILinkContainer finishLink();
}
