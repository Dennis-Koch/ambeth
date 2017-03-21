package com.koch.ambeth.util.typeinfo;

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

public final class NullEquivalentValueUtil
{
	private static final HashMap<Class<?>, Object> nullEquivalentValues = new HashMap<Class<?>, Object>(0.5f);

	static
	{
		nullEquivalentValues.put(Boolean.TYPE, Boolean.valueOf(false));
		nullEquivalentValues.put(Double.TYPE, Double.valueOf(0));
		nullEquivalentValues.put(Long.TYPE, Long.valueOf(0));
		nullEquivalentValues.put(Float.TYPE, Float.valueOf(0));
		nullEquivalentValues.put(Integer.TYPE, Integer.valueOf(0));
		nullEquivalentValues.put(Short.TYPE, Short.valueOf((short) 0));
		nullEquivalentValues.put(Byte.TYPE, Byte.valueOf((byte) 0));
		nullEquivalentValues.put(Character.TYPE, Character.valueOf('\0'));
		nullEquivalentValues.put(Boolean.class, Boolean.valueOf(false));
		nullEquivalentValues.put(Double.class, Double.valueOf(0));
		nullEquivalentValues.put(Long.class, Long.valueOf(0));
		nullEquivalentValues.put(Float.class, Float.valueOf(0));
		nullEquivalentValues.put(Integer.class, Integer.valueOf(0));
		nullEquivalentValues.put(Short.class, Short.valueOf((short) 0));
		nullEquivalentValues.put(Byte.class, Byte.valueOf((byte) 0));
		nullEquivalentValues.put(Character.class, Character.valueOf('\0'));
	}

	public static Object getNullEquivalentValue(Class<?> type)
	{
		return nullEquivalentValues.get(type);
	}

	private NullEquivalentValueUtil()
	{
		// intended blank
	}
}
