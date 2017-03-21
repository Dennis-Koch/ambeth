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
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.koch.ambeth.util.collections.AbstractHashSet;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.ObservableArrayList;
import com.koch.ambeth.util.collections.ObservableHashSet;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.objectcollector.IObjectCollector;

public final class ListUtil {
	protected static final Class<?> listType = ArrayList.class;

	protected static final Class<?> setType = HashSet.class;

	protected static final Class<?> obsListType = ObservableArrayList.class;

	protected static final Class<?> obsSetType = ObservableHashSet.class;

	protected static final Constructor<?> listTypeC, setTypeC, obsListTypeC, obsSetTypeC,
			sizeListTypeC, sizeSetTypeC, sizeObsListTypeC, sizeObsSetTypeC;

	static {
		try {
			listTypeC = listType.getConstructor();
			setTypeC = setType.getConstructor();
			obsListTypeC = obsListType.getConstructor();
			obsSetTypeC = obsSetType.getConstructor();
			sizeListTypeC = listType.getConstructor(Integer.TYPE);
			sizeSetTypeC = setType.getConstructor(Integer.TYPE, Float.TYPE);
			sizeObsListTypeC = obsListType.getConstructor(Integer.TYPE);
			sizeObsSetTypeC = obsSetType.getConstructor(Integer.TYPE, Float.TYPE);
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	private ListUtil() {
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(Class<T> type, Collection<?> list) {
		if (list == null) {
			return null;
		}
		Object array = Array.newInstance(type, list.size());
		int index = 0;
		Iterator<?> iter = list.iterator();
		while (iter.hasNext()) {
			Array.set(array, index++, iter.next());
		}
		return (T[]) array;
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> toList(Enumeration<T> enumerable) {
		if (enumerable == null) {
			return null;
		}
		if (enumerable instanceof List<?>) {
			return (List<T>) enumerable;
		}
		ArrayList<T> list = new ArrayList<>();
		while (enumerable.hasMoreElements()) {
			list.add(enumerable.nextElement());
		}

		return list;
	}

	public static <T> List<T> toList(Iterable<T> enumerable) {
		if (enumerable == null) {
			return null;
		}
		if (enumerable instanceof List<?>) {
			return (List<T>) enumerable;
		}
		Iterator<T> iter = enumerable.iterator();
		ArrayList<T> list = new ArrayList<>();
		while (iter.hasNext()) {
			list.add(iter.next());
		}

		return list;
	}

	@SuppressWarnings("unchecked")
	public static List<Object> toList(Object obj) {
		if (obj instanceof List<?>) {
			return (List<Object>) obj;
		}
		else if (obj instanceof Iterable<?>) {
			return toList((Iterable<Object>) obj);
		}
		else if (obj instanceof Enumeration<?>) {
			return toList((Enumeration<Object>) obj);
		}
		ArrayList<Object> list = new ArrayList<>(1);
		list.add(obj);
		return list;
	}

	@SuppressWarnings("unchecked")
	public static <T> IList<T> anyToList(IObjectCollector objectCollector, Object obj) {
		// Always build a new list
		ArrayList<T> list = new ArrayList<>();
		if (obj == null) {
			return list;
		}
		else if (obj.getClass().isArray()) {
			int length = Array.getLength(obj);
			for (int a = 0; a < length; a++) {
				list.add((T) Array.get(obj, a));
			}
		}
		else if (obj instanceof List) {
			List<?> objList = (List<?>) obj;
			for (int a = 0, size = objList.size(); a < size; a++) {
				list.add((T) objList.get(a));
			}
		}
		else if (obj instanceof Iterable) {
			for (Object item : (Iterable<?>) obj) {
				list.add((T) item);
			}
		}
		else if (obj != null) {
			list.add((T) obj);
		}
		return list;
	}

	public static Collection<Object> createCollectionOfType(Class<?> expectedCollectionType) {
		return createCollectionOfType(expectedCollectionType, -1);
	}

	public static Collection<Object> createObservableCollectionOfType(
			Class<?> expectedCollectionType) {
		return createObservableCollectionOfType(expectedCollectionType, -1);
	}

	public static Collection<Object> createCollectionOfType(Class<?> expectedCollectionType,
			int size) {
		return createCollectionOfType(expectedCollectionType, size, setTypeC, sizeSetTypeC, listTypeC,
				sizeListTypeC);
	}

	public static Collection<Object> createObservableCollectionOfType(Class<?> expectedCollectionType,
			int size) {
		return createCollectionOfType(expectedCollectionType, size, obsSetTypeC, sizeObsSetTypeC,
				obsListTypeC, sizeObsListTypeC);
	}

	public static Collection<Object> createCollectionOfType(Class<?> expectedCollectionType,
			Constructor<?> setTypeC, Constructor<?> sizeSetTypeC, Constructor<?> listTypeC,
			Constructor<?> sizeListTypeC) {
		return createCollectionOfType(expectedCollectionType, -1, setTypeC, sizeSetTypeC, listTypeC,
				sizeListTypeC);
	}

	@SuppressWarnings("unchecked")
	public static Collection<Object> createCollectionOfType(Class<?> expectedCollectionType, int size,
			Constructor<?> setTypeC, Constructor<?> sizeSetTypeC, Constructor<?> listTypeC,
			Constructor<?> sizeListTypeC) {
		try {
			if (!expectedCollectionType.isInterface()) {
				return (Collection<Object>) expectedCollectionType.newInstance();
			}
			if (Set.class.isAssignableFrom(expectedCollectionType)) {
				if (size != -1 && sizeSetTypeC != null) {
					return (Collection<Object>) sizeSetTypeC.newInstance(
							(int) (size / AbstractHashSet.DEFAULT_LOAD_FACTOR) + 1,
							AbstractHashSet.DEFAULT_LOAD_FACTOR);
				}
				return (Collection<Object>) setTypeC.newInstance();
			}
			if (size != -1 && sizeListTypeC != null) {
				return (Collection<Object>) sizeListTypeC.newInstance(size);
			}
			return (Collection<Object>) listTypeC.newInstance();
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static List<Object> anyToList(Object obj) {
		List<Object> list;

		if (obj == null) {
			list = Collections.<Object>emptyList();
		}
		else if (obj.getClass().isArray()) {
			int length = Array.getLength(obj);
			if (length > 0) {
				list = new ArrayList<>(length);
				for (int i = 0; i < length; i++) {
					list.add(Array.get(obj, i));
				}
			}
			else {
				list = Collections.<Object>emptyList();
			}
		}
		else if (obj instanceof List) {
			list = (List<Object>) obj;
		}
		else if (obj instanceof Iterable) {
			list = anyToList(((Iterable<Object>) obj).iterator());
		}
		else if (obj instanceof Iterator) {
			Iterator<Object> iter = (Iterator<Object>) obj;
			if (iter.hasNext()) {
				list = new ArrayList<>();
				while (iter.hasNext()) {
					list.add(iter.next());
				}
			}
			else {
				list = Collections.<Object>emptyList();
			}
		}
		else {
			list = new ArrayList<>(1);
			list.add(obj);
		}

		return list;
	}

	@SuppressWarnings("unchecked")
	public static Set<Object> anyToSet(Object obj) {
		Set<Object> set;

		if (obj instanceof Set) {
			set = (Set<Object>) obj;
		}
		else {
			List<Object> list = anyToList(obj);
			set = new HashSet<>((int) (list.size() / 0.75f) + 1, 0.75f);
			set.addAll(list);
		}
		return set;
	}

	public static Object[] anyToArray(Object obj) {
		return anyToArray(obj, Object.class);
	}

	@SuppressWarnings("unchecked")
	public static <V> V[] anyToArray(Object obj, Class<V> targetElementType) {
		if (obj == null) {
			return null;
		}
		Class<?> objType = obj.getClass();
		if (objType.isArray()) {
			if (targetElementType.equals(objType.getComponentType())) {
				return (V[]) obj;
			}
			// We decide to go on and try to convert the elements (maybe we have a object-array which
			// contains only <someclass> elements)
			int length = Array.getLength(obj);
			Object resultArray = Array.newInstance(targetElementType, length);
			try {
				for (int i = length; i-- > 0;) {
					Object source = Array.get(obj, i);
					Array.set(resultArray, i, source);
				}
			}
			catch (Exception e) {
				throw new RuntimeException("Element Types of this this array are not compatible \""
						+ objType.getComponentType().getName() + "\" -> \"" + targetElementType.getName()
						+ "\".", e);
			}
			return (V[]) resultArray;
		}
		List<Object> list = anyToList(obj);
		int size = list.size();
		Object resultArray = Array.newInstance(targetElementType, size);
		for (int a = size; a-- > 0;) {
			Array.set(resultArray, a, list.get(a));
		}
		return (V[]) resultArray;
	}

	public static void clearList(Object obj) {
		((Collection<?>) obj).clear();
	}

	public static void clearAndFillList(Object obj, Iterable<?> items) {
		clearList(obj);
		fillList(obj, items);
	}

	@SuppressWarnings("unchecked")
	public static void fillList(Object obj, Iterable<?> items) {
		if (items == null) {
			return;
		}
		Collection<Object> coll = (Collection<Object>) obj;
		for (Object item : items) {
			coll.add(item);
		}

	}
}
