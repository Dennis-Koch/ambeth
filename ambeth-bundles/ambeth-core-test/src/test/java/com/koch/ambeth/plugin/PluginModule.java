package com.koch.ambeth.plugin;

/*-
 * #%L
 * jambeth-core-test
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

import com.koch.ambeth.core.plugin.IJarURLProvider;
import com.koch.ambeth.core.plugin.JarURLProvider;
import com.koch.ambeth.core.plugin.PluginClasspathScanner;
import com.koch.ambeth.core.start.IClasspathInfo;
import com.koch.ambeth.core.start.SystemClasspathInfo;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;

public class PluginModule implements IInitializingModule {
	@Override
	public void afterPropertiesSet(IBeanContextFactory bcf) throws Throwable {
		bcf.registerBean(SystemClasspathInfo.class).autowireable(IClasspathInfo.class);

		bcf.registerBean(JarURLProvider.class).autowireable(IJarURLProvider.class);

		bcf.registerBean(PluginClasspathScanner.class).autowireable(PluginClasspathScanner.class);
	}
}
