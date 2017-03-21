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

import com.koch.ambeth.core.Ambeth;

/**
 * <p>
 * <b>If you implement this interface really read this text!</b>
 * </p>
 * <p>
 * An implementing class is hooked directly in the fluent API of the Ambeth startup configuration process. So it has to conform to the design of this API and
 * the following points.
 * </p>
 * <ul>
 * <li>Store the set {@link Ambeth} instance in a field.</li>
 * <li>Every method has to have {@link IAmbethConfiguration} or the extension type as the return type.</li>
 * <li>If it returns {@link IAmbethConfiguration} the Ambeth instance has to be returned to continue the normal API.</li>
 * <li>If it returns the extension type the extension instance (this) has to be returned to continue the extended API.</li>
 * <li>The extension API has to end returning the Ambeth instance.</li>
 * <li>The instance is hooked deeply into the startup process - be aware of what you do!</li>
 * </ul>
 */
public interface IAmbethConfigurationExtension
{
	void setAmbethConfiguration(Ambeth ambethConfiguration);
}
