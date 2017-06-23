package com.koch.ambeth.util.proxy;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class ClassLoaderAwareClassWriter extends ClassWriter {
	private final ClassLoader classLoader;

	public ClassLoaderAwareClassWriter(int flags, ClassLoader classLoader) {
		super(flags);
		this.classLoader = classLoader;
	}

	public ClassLoaderAwareClassWriter(ClassReader classReader, int flags, ClassLoader classLoader) {
		super(classReader, flags);
		this.classLoader = classLoader;
	}

	@Override
	protected String getCommonSuperClass(String type1, String type2) {
		Class<?> c, d;
		ClassLoader classLoader = this.classLoader != null ? this.classLoader
				: getClass().getClassLoader();
		try {
			c = Class.forName(type1.replace('/', '.'), false, classLoader);
			d = Class.forName(type2.replace('/', '.'), false, classLoader);
		}
		catch (Exception e) {
			throw new RuntimeException(e.toString());
		}
		if (c.isAssignableFrom(d)) {
			return type1;
		}
		if (d.isAssignableFrom(c)) {
			return type2;
		}
		if (c.isInterface() || d.isInterface()) {
			return "java/lang/Object";
		}
		else {
			do {
				c = c.getSuperclass();
			}
			while (!c.isAssignableFrom(d));
			return c.getName().replace('.', '/');
		}
	}

}
