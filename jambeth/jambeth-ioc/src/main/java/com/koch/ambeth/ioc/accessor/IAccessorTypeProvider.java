package com.koch.ambeth.ioc.accessor;

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

import com.koch.ambeth.util.typeinfo.IPropertyInfo;

/**
 * Allows the creation of runtime-generated classes and instances of it to have the best-performing
 * implementation for highly used tasks - like creating many instances of an unknown class handle
 * without the need to use the comparatively slow reflection API.
 */
public interface IAccessorTypeProvider {
	/**
	 * Creates an accessor to get/set values of a property but without depending an reflection API.
	 * This allows for very high runtime performance with the only drawback of some "generated-once"
	 * classes on-demand to instantiate from.
	 *
	 * @param type The concrete class to look for the specified property. This class is also used to
	 *        resolve its classloader in order to derive a sub-classloader from to put in the
	 *        generated sub-class of the returned {@link AbstractAccessor}.
	 * @param property The property to "compile" into the generated {@link AbstractAccessor}
	 * @return The "compiled" property allowing generic access to the property with native performance
	 *         - means: without using reflection API
	 */
	AbstractAccessor getAccessorType(Class<?> type, IPropertyInfo property);

	/**
	 * Creates an instance of a runtime-generated class which is a sub-class of the specified
	 * <code>delegateType</code> and maps all methods of the delegate type to the applicable
	 * constructor signature of the specified <code>targetType</code>. So the returned objects allows
	 * you to efficiently instantiate classes for the <code>targetType</code> but with native
	 * performance and without the overhead of reflection or any dynamic invocations. The
	 * <code>delegateType</code> is in most cases an interface but may even be an abstract class (to
	 * further reduce the overhead of interface invocations on the backing bytecode).
	 *
	 * @param delegateType The type to subclass from and to return an instance of it
	 * @param targetType The type to instantiate when calling the delegate and to map the constructors
	 *        signatures from
	 * @return The delegate handle to be used in order to create instances of <code>targetType</code>
	 */
	<V> V getConstructorType(Class<V> delegateType, Class<?> targetType);
}
