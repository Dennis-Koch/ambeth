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

import com.koch.ambeth.util.collections.HashMap;

public final class WrapperTypeSet {
	private static final HashMap<Class<?>, Class<?>> wrapperTypesMap =
			new HashMap<>(0.5f);

	static {
		wrapperTypesMap.put(Integer.class, Integer.TYPE);
		wrapperTypesMap.put(Long.class, Long.TYPE);
		wrapperTypesMap.put(Double.class, Double.TYPE);
		wrapperTypesMap.put(Float.class, Float.TYPE);
		wrapperTypesMap.put(Short.class, Short.TYPE);
		wrapperTypesMap.put(Character.class, Character.TYPE);
		wrapperTypesMap.put(Byte.class, Byte.TYPE);
		wrapperTypesMap.put(Boolean.class, Boolean.TYPE);
	}

	private WrapperTypeSet() {
		// Intended blank
	}

	public static Class<?> getUnwrappedType(Class<?> wrapperType) {
		return wrapperTypesMap.get(wrapperType);
	}
}
