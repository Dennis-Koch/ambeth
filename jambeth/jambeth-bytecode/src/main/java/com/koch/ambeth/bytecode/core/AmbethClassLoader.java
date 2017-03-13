package com.koch.ambeth.bytecode.core;

import com.koch.ambeth.util.collections.WeakHashMap;

public class AmbethClassLoader extends ClassLoader
{
	protected final WeakHashMap<Class<?>, byte[]> classToContentMap = new WeakHashMap<Class<?>, byte[]>();

	public AmbethClassLoader(ClassLoader parent)
	{
		super(parent);
	}

	public Class<?> defineClass(String name, byte[] b)
	{
		Class<?> type = defineClass(name, b, 0, b.length);
		classToContentMap.put(type, b);
		return type;
	}

	public byte[] getContent(Class<?> type)
	{
		return classToContentMap.get(type);
	}
}
