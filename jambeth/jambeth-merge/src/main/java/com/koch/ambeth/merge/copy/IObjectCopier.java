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
 * Performs a deep copy of the object. The ObjectCopier can clone any object tree in a flexible and
 * extendable way. Use IObjectCopierExtendable to provide own extensions to the default ObjectCopier
 * behavior if necessary. In addition the ObjectCopier recognizes native data copy scenarios as well
 * as cyclic paths in the object tree.
 */
public interface IObjectCopier {
	/**
	 * Performs a deep copy of the object
	 *
	 * @param <T> The type of object being copied
	 * @param source The object instance to copy
	 * @return The copied object representing a deep clone of the source object
	 */
	<T> T clone(T source);
}
