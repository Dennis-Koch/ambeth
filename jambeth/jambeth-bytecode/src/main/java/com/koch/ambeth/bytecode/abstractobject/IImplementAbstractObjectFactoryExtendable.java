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
 * IImplementAbstractObjectFactoryExtendable provides configuration for {@link IImplementAbstractObjectFactory} to implement objects based on interfaces.
 * Optionally the implementations can inherit from an (abstract) base type
 */
public interface IImplementAbstractObjectFactoryExtendable
{
	/**
	 * Register a type to be implemented by this extension
	 * 
	 * @param keyType
	 *            The type to be implemented
	 */
	void register(Class<?> keyType);

	/**
	 * Register a type to be implemented by this extension. The implementation will extend baseType
	 * 
	 * @param baseType
	 *            The (abstract) base type to be extended
	 * @param keyType
	 *            The type to be implemented
	 */
	void registerBaseType(Class<?> baseType, Class<?> keyType);

	/**
	 * Registers a type to be implemented by this extension. The implementation will implement interfaceTypes
	 * 
	 * @param interfaceTypes
	 *            The interface types to be implemented
	 * @param keyType
	 *            The type to be implemented
	 */
	void registerInterfaceTypes(Class<?>[] interfaceTypes, Class<?> keyType);

	/**
	 * Unregister a type to be implemented by this extension.
	 * 
	 * @param keyType
	 *            The type to be unregistered
	 */
	void unregister(Class<?> keyType);

	/**
	 * Unregister a type to be implemented by this extension.
	 * 
	 * @param baseType
	 *            The (abstract) base type to be extended
	 * @param keyType
	 *            The type to be unregistered
	 */
	void unregisterBaseType(Class<?> baseType, Class<?> keyType);

	/**
	 * Unregister a type to be implemented by this extension.
	 * 
	 * @param interfaceTypes
	 *            The interface types to be implemented
	 * @param keyType
	 *            The type to be unregistered
	 */
	void unregisterInterfaceTypes(Class<?>[] interfaceTypes, Class<?> keyType);
}
