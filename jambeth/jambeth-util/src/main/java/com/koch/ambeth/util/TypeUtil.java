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

import org.objectweb.asm.Type;

public final class TypeUtil {
	public static final Class<?>[] EMPTY_TYPES = new Class[0];

	public static final Type[] getClassesToTypes(Class<?>[] classes) {
		Type[] types = new Type[classes.length];
		for (int a = classes.length; a-- > 0;) {
			Class<?> clazz = classes[a];
			if (clazz == null) {
				continue;
			}
			types[a] = Type.getType(clazz);
		}
		return types;
	}

	private TypeUtil() {
		// Intended blank
	}
}
