package com.koch.ambeth.ioc.bytecode;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.util.IClassLoaderProvider;

public class SimpleClassLoaderProvider implements IClassLoaderProvider, IInitializingBean {

	public static final String CLASS_LOADER_PROP_NAME = "ClassLoader";

	protected ClassLoader classLoader;

	@Override
	public void afterPropertiesSet() throws Throwable {
		if (classLoader == null) {
			classLoader = Thread.currentThread().getContextClassLoader();
		}
	}

	@Override
	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}
}
