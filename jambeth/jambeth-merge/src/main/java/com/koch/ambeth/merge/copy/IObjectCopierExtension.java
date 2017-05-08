package com.koch.ambeth.merge.copy;

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

/**
 * Implement this interface to encapsulate copy logic which extends the default ObjectCopier
 * behavior
 */
public interface IObjectCopierExtension {
	/**
	 * Implement this interface to encapsulate copy logic which extends the default ObjectCopier
	 * behavior
	 *
	 * @param original The object instance to copy
	 * @param objectCopierState Encapsulates the current copy state. It may be called in cascaded
	 *        custom/default copy behaviors
	 * @return The copied object representing a deep clone of the source object
	 */
	Object deepClone(Object original, IObjectCopierState objectCopierState);
}
