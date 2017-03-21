package com.koch.ambeth.platform;

/*-
 * #%L
 * jambeth-platform
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
import com.koch.ambeth.util.config.IProperties;

public interface IPlatformContextConfiguration
{
	IPlatformContextConfiguration addProperties(IProperties... properties);

	IPlatformContextConfiguration addProperties(java.util.Properties... properties);

	IPlatformContextConfiguration addProviderModule(Class<?>... providerModuleTypes);

	IPlatformContextConfiguration addFrameworkModule(Class<?>... frameworkModuleTypes);

	IPlatformContextConfiguration addBootstrapModule(Class<?>... bootstrapModuleTypes);

	IPlatformContextConfiguration addProviderModule(IInitializingModule... providerModules);

	IPlatformContextConfiguration addFrameworkModule(IInitializingModule... frameworkModules);

	IPlatformContextConfiguration addBootstrapModule(IInitializingModule... bootstrapModules);

	IAmbethPlatformContext createPlatformContext();
}
