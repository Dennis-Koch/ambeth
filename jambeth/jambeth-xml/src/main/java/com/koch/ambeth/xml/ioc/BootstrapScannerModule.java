package com.koch.ambeth.xml.ioc;

/*-
 * #%L
 * jambeth-xml
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

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.util.IClasspathScanner;
import com.koch.ambeth.xml.util.ClasspathScanner;
import com.koch.ambeth.xml.util.ClasspathScannerServletContext;
import com.koch.ambeth.xml.util.ModuleScanner;

public class BootstrapScannerModule implements IInitializingModule {

	private static Class<?> classpathScannerServletContextType;
	static {
		try {
			classpathScannerServletContextType = ClasspathScannerServletContext.class;
		}
		catch (Throwable e) {
			// intended blank
		}
	}

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		IBeanConfiguration classpathScannerBC = beanContextFactory.registerBean(ClasspathScanner.class)
				.autowireable(IClasspathScanner.class);

		Object servletContext = getServletContext();
		if (classpathScannerServletContextType != null && servletContext != null) {
			IBeanConfiguration classpathScannerServletContext =
					beanContextFactory.registerBean(classpathScannerServletContextType)
							.propertyValue("ServletContext", servletContext);
			classpathScannerBC.propertyRef("ClasspathScannerServletContext",
					classpathScannerServletContext);
		}
		beanContextFactory.registerBean(ModuleScanner.class);
	}

	protected Object getServletContext() {
		return null;
	}
}
