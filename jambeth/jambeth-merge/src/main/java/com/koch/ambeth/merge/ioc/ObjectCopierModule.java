package com.koch.ambeth.merge.ioc;

/*-
 * #%L
 * jambeth-merge
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
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.copy.IObjectCopier;
import com.koch.ambeth.merge.copy.IObjectCopierExtendable;
import com.koch.ambeth.merge.copy.ObjectCopier;
import com.koch.ambeth.merge.copy.StringBuilderOCE;

/**
 * Registers an ObjectCopier as well as default extensions to copy objects Include this module in an
 * IOC container to gain access to <code>IObjectCopier</code> & <code>IObjectCopierExtendable</code>
 * functionality
 */
@FrameworkModule
public class ObjectCopierModule implements IInitializingModule {
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		// Default ObjectCopier implementation
		beanContextFactory.registerBean(ObjectCopier.class).autowireable(IObjectCopier.class,
				IObjectCopierExtendable.class);

		// Default ObjectCopier extensions
		IBeanConfiguration stringBuilderOCE = beanContextFactory.registerBean(StringBuilderOCE.class);
		beanContextFactory.link(stringBuilderOCE).to(IObjectCopierExtendable.class)
				.with(StringBuilder.class);
	}
}
