package com.koch.ambeth.bytecode;

/*-
 * #%L
 * jambeth-test
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

import com.koch.ambeth.bytecode.abstractobject.IImplementAbstractObjectFactory;
import com.koch.ambeth.bytecode.abstractobject.IImplementAbstractObjectFactoryExtendable;
import com.koch.ambeth.bytecode.behavior.ImplementAbstractObjectBehavior;
import com.koch.ambeth.bytecode.ioc.BytecodeModule;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.bytecode.abstractobject.ImplementAbstractObjectFactory;

public class PublicConstructorVisitorTestModule implements IInitializingModule {
	private static final String IMPLEMENT_ABSTRACT_OBJECT_FACTORY = "implementAbstractObjectFactory";

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		// creates objects that implement the interfaces
		beanContextFactory
				.registerBean(IMPLEMENT_ABSTRACT_OBJECT_FACTORY, ImplementAbstractObjectFactory.class)
				.autowireable(IImplementAbstractObjectFactory.class,
						IImplementAbstractObjectFactoryExtendable.class);

		BytecodeModule
				.addDefaultBytecodeBehavior(beanContextFactory, ImplementAbstractObjectBehavior.class)
				.propertyRef("ImplementAbstractObjectFactory", IMPLEMENT_ABSTRACT_OBJECT_FACTORY);
	}
};
