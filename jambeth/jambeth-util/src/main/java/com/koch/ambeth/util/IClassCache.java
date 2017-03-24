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
 * Provides a high-performance implementation to cache calls to either
 * {@link ClassLoader#loadClass(String)} or {@link Class#forName(String, boolean, ClassLoader).<br>
 * <br>
 *
 * The implementation holds only weak references to the cached classloader results to be safe
 * against memory leaks of unloaded class loaders.
 */
public interface IClassCache {
	Class<?> loadClass(String name, ClassLoader classLoader) throws ClassNotFoundException;

	Class<?> loadClass(String name) throws ClassNotFoundException;

	Class<?> forName(String name, ClassLoader classLoader) throws ClassNotFoundException;

	Class<?> forName(String name) throws ClassNotFoundException;

	void invalidate(ClassLoader classLoader);
}
