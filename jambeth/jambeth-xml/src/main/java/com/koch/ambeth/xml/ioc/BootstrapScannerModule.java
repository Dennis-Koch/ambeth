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

import javax.servlet.ServletContext;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.util.IClasspathScanner;
import com.koch.ambeth.xml.util.ClasspathScanner;
import com.koch.ambeth.xml.util.ModuleScanner;

public class BootstrapScannerModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		IBeanConfiguration classpathScannerBC = beanContextFactory.registerBean(ClasspathScanner.class).autowireable(IClasspathScanner.class);

		beanContextFactory.registerBean(ModuleScanner.class);

		ServletContext servletContext = getServletContext();
		if (servletContext != null)
		{
			classpathScannerBC.propertyValue("ServletContext", servletContext);
		}
	}

	protected ServletContext getServletContext()
	{
		return null;
	}
}
