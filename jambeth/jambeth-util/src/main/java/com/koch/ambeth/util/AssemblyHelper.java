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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.threading.SensitiveThreadLocal;

public class AssemblyHelper {
	public static final Pattern assemblyNameRegEx = Pattern.compile("([^\\,]+)\\,.+");

	protected AssemblyHelper() {
	}

	protected static final ThreadLocal<Map<String, Reference<Class<?>>>> nameToTypeDictTL =
			new SensitiveThreadLocal<Map<String, Reference<Class<?>>>>() {
				@Override
				protected Map<String, Reference<Class<?>>> initialValue() {
					// Intentionally use java.util.HashMap to prevent ClassLoader memory leaking due to EE
					// redeployments
					return new java.util.HashMap<>();
				}
			};

	protected static final Map<String, Class<?>> nameToTypeDict = new HashMap<>();

	public static Class<?> getTypeFromCurrentDomain(String typeName) {
		Map<String, Reference<Class<?>>> nameToTypeLocal = nameToTypeDictTL.get();
		Reference<Class<?>> typeR = nameToTypeLocal.get(typeName);
		Class<?> type = null;
		if (typeR != null) {
			type = typeR.get();
		}
		if (type != null) {
			return type;
		}
		synchronized (nameToTypeDict) {
			type = nameToTypeDict.get(typeName);
			if (type == null) {
				try {
					type = Thread.currentThread().getContextClassLoader().loadClass(typeName);
				}
				catch (ClassNotFoundException e) {
					throw RuntimeExceptionUtil.mask(e);
				}
				nameToTypeDict.put(typeName, type);
			}
		}
		nameToTypeLocal.put(typeName, new WeakReference<Class<?>>(type));
		return type;
	}

	// public static <T> void handleTypesFromCurrentDomain(Class<T> type,
	// TypeHandleDelegate typeHandleDelegate)
	// {
	// handleTypesFromCurrentDomain(type, typeHandleDelegate, false);
	// }
	//
	// public static <T> void handleTypesFromCurrentDomain(Class<T> type,
	// TypeHandleDelegate typeHandleDelegate, boolean includeInterfaces)
	// {
	// Class<?> lookForType = typeof(T);
	// for (Class<?> type : typesFromCurrentDomain)
	// {
	// if (!lookForType.isAssignableFrom(type))
	// {
	// continue;
	// }
	// if (!includeInterfaces && type.IsInterface)
	// {
	// continue;
	// }
	// typeHandleDelegate(type);
	// }
	// }

	// public static <T> void
	// handleAttributedTypesFromCurrentDomain(AttributedTypeHandleDelegate
	// typeHandleDelegate) where T : Attribute
	// {
	// Class<?> lookForType = typeof(T);
	// for (Class<?> type : typesFromCurrentDomain)
	// {
	// Object[] attributes = type.GetCustomAttributes(lookForType, true);
	// if (attributes == null || attributes.length == 0)
	// {
	// continue;
	// }
	// typeHandleDelegate(type, attributes);
	// }
	// }
}

// public void typeHandleDelegate(Class<?> type);
//
// public void attributedTypeHandleDelegate(Class<?> type, Object[] attributes);

