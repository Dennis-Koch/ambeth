package com.koch.ambeth.util;

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


/**
 * Marker interface for objects which claim to be immutable - that means they have no
 * properties/fields which can be modified directly or indirectly and therefore change the state of
 * the immutable object. Its used e.g. in the internals of the ObjectCopier component to speedup the
 * cloning procedure of dehydrated entities in the 2nd level cache and to greatly reduce the overall
 * memory footprint.<br>
 *
 * @see com.koch.ambeth.merge.copy.IObjectCopier
 */
public interface IImmutableType {
	// Intended blank
}
