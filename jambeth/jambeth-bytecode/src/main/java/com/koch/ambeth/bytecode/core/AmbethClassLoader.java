package com.koch.ambeth.bytecode.core;

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

import com.koch.ambeth.util.collections.WeakHashMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class AmbethClassLoader extends ClassLoader {
	protected final WeakHashMap<Class<?>, byte[]> classToContentMap =
			new WeakHashMap<>();

	public AmbethClassLoader(ClassLoader parent) {
		super(parent);
	}

	public Class<?> defineClass(String name, byte[] b) {
		try {
			Class<?> type = defineClass(name, b, 0, b.length);
			classToContentMap.put(type, b);
			return type;
		}
		catch (NoClassDefFoundError e) {
			throw RuntimeExceptionUtil.mask(e, "Error occurred while creating '" + name
					+ "' in an Ambeth ClassLoader derived from '" + getParent() + "'");
		}
	}

	public byte[] getContent(Class<?> type) {
		return classToContentMap.get(type);
	}
}
