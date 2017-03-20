package com.koch.ambeth.bytecode.core;

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
