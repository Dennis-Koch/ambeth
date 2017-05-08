package com.koch.ambeth.ioc.extendable;

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

import java.util.Collection;

/**
 * Interface for listener or extension container for unique instances.
 *
 * @param <V> Type of the content
 */
public interface IExtendableContainer<V> {
	/**
	 * Registers an instance of an extension.
	 *
	 * @param extension Instance to be registered
	 */
	void register(V extension);

	/**
	 * Unregisters an instance of an extension.
	 *
	 * @param extension Instance to be unregistered
	 */
	void unregister(V extension);

	/**
	 * Returns an typed array of all registered extensions.
	 *
	 * @return Array of extension instances
	 */
	V[] getExtensions();

	/**
	 * Fills the collection with all registered extensions.
	 *
	 * @param targetExtensionList Collection to be filled
	 */
	void getExtensions(Collection<V> targetExtensionList);
}
