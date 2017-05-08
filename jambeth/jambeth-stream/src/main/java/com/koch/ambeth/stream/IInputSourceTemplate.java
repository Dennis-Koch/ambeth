package com.koch.ambeth.stream;

import com.koch.ambeth.stream.float64.IDoubleInputSource;

/*-
 * #%L
 * jambeth-stream
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
 * Technically this is just a marker interface: If you implement a custom {@link ICacheRetriever} or
 * {@link IPrimitiveRetriever} you can assign just an instance of this interface to give the cache
 * hierarchies the hint to prepare the corresponding property at runtime for the full streaming
 * capability. For convenience there is already an enum value ({@link InputSourceTemplate#INSTANCE})
 * ready to use as the primitive property value. As soon as the cache needs to hydrate the entity in
 * any 1st level cache the corresponding dedicated converter is called to instantiate the type-safe
 * data source (e.g. an {@link IDoubleInputSource}). The usage of the enum can look like this:<br>
 * <br>
 * <code>
 * Object[] getPrimitives(List&lt;IObjRelation&gt; objPropertyKeys) {<br>
 * IObjRelation objPropertyKey = objPropertyKeys.get(0);<br>
 * return new Object[] { InputSourceTemplate.INSTANCE };<br>
 * }</code>
 */
public interface IInputSourceTemplate extends Cloneable {
	// Intended blank
}
