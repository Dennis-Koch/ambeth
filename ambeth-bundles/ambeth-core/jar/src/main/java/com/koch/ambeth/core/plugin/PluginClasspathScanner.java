package com.koch.ambeth.core.plugin;

/*-
 * #%L
 * jambeth-core
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

import java.net.URL;
import java.net.URLClassLoader;

import com.koch.ambeth.core.start.CoreClasspathScanner;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.util.collections.IList;

import javassist.ClassPool;

public class PluginClasspathScanner extends CoreClasspathScanner {
	@Autowired
	protected IJarURLProvider jarURLProvider;

	protected URLClassLoader urlClassLoader;

	@Override
	public void afterPropertiesSet() throws Throwable {
		super.afterPropertiesSet();

		URL[] urls = jarURLProvider.getJarURLs().toArray(URL.class);
		urlClassLoader = new URLClassLoader(urls, getClass().getClassLoader());
	}

	@Override
	public ClassLoader getClassLoader() {
		return urlClassLoader;
	}

	@Override
	protected IList<URL> getJarURLs() {
		return jarURLProvider.getJarURLs();
	}

	@Override
	protected ClassPool getClassPool() {
		if (classPool == null) {
			classPool = new ClassPool();
			initializeClassPool(classPool);
		}
		return classPool;
	}
}
