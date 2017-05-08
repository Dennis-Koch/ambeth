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

import java.lang.reflect.Array;

import com.koch.ambeth.util.objectcollector.IObjectCollector;

public final class Arrays {
	private Arrays() {
		// Intended blank
	}

	@SuppressWarnings("unchecked")
	public static final <T> T[] removeElementAt(T[] array, int index) {
		Object arr = Array.newInstance(array.getClass().getComponentType(), array.length - 1);
		System.arraycopy(array, 0, arr, 0, index);
		System.arraycopy(array, index + 1, arr, index, array.length - index - 1);
		return (T[]) arr;
	}

	public static final String toString(IObjectCollector objectCollector, Object[] array) {
		if (array == null) {
			return "null";
		}
		int iMax = array.length - 1;
		if (iMax == -1) {
			return "[]";
		}
		StringBuilder sb = objectCollector.create(StringBuilder.class);
		try {
			sb.append('[');
			for (int i = 0;; i++) {
				Object item = array[i];
				StringBuilderUtil.appendPrintable(sb, item);
				if (i == iMax) {
					return sb.append(']').toString();
				}
				sb.append(", ");
			}
		}
		finally {
			objectCollector.dispose(sb);
		}
	}

	public static final void toString(StringBuilder sb, Object[] array) {
		if (array == null) {
			sb.append("null");
			return;
		}
		int iMax = array.length - 1;
		if (iMax == -1) {
			sb.append("[]");
			return;
		}
		sb.append('[');
		for (int i = 0;; i++) {
			Object item = array[i];
			StringBuilderUtil.appendPrintable(sb, item);
			if (i == iMax) {
				sb.append(']');
				return;
			}
			sb.append(", ");
		}
	}
}
