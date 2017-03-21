package com.koch.ambeth.security.bytecode.ioc;

/*-
 * #%L
 * jambeth-security-bytecode
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

import com.koch.ambeth.bytecode.ioc.BytecodeModule;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.security.bytecode.behavior.EntityPrivilegeBehavior;
import com.koch.ambeth.security.bytecode.behavior.EntityTypePrivilegeBehavior;

@FrameworkModule
public class SecurityBytecodeModule implements IInitializingModule {
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, EntityPrivilegeBehavior.class);
		BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory,
				EntityTypePrivilegeBehavior.class);

		// beanContextFactory.registerAnonymousBean(ValueHolderContainerTemplate.class).autowireable(ValueHolderContainerTemplate.class);
	}
}
