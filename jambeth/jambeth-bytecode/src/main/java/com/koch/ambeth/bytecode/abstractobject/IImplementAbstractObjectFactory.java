package com.koch.ambeth.bytecode.abstractobject;

/*-
 * #%L
 * jambeth-bytecode
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
 * IImplementAbstractObjectFactory implements objects based on interfaces. Optionally the implementations can inherit
 * from an (abstract) base type
 */
public interface IImplementAbstractObjectFactory
{
	/**
	 * Returns true if this type is registered for this factory
	 * 
	 * @param keyType
	 *            The type to be implemented
	 * @return true if this type is registered for this factory
	 */
	boolean isRegistered(Class<?> keyType);

	/**
	 * Returns the base type registered for this type. The base type as a(n abstract) class that is extended when
	 * creating the types implementation.
	 * 
	 * @param keyType
	 *            The type to be implemented
	 * @return BaseType or Object.class if the type is not registered with an base type
	 * @throws IllegalArgumentException
	 *             when the type is not registered
	 */
	Class<?> getBaseType(Class<?> keyType);

	/**
	 * Returns interfaces types to be implemented for this type
	 * 
	 * @param keyType
	 *            The type to be implemented
	 * @return InterfaceTypes interface types to be implemented
	 * @throws IllegalArgumentException
	 *             when the type is not registered
	 */
	Class<?>[] getInterfaceTypes(Class<?> keyType);

	/**
	 * Creates the implementation of the type. Optionally the implementation can inherit from an (abstract) base base
	 * type
	 * 
	 * @param keyType
	 *            The type to be implemented
	 * @return The type implementing keyType
	 */
	<T> Class<? extends T> getImplementingType(Class<T> keyType);
}
