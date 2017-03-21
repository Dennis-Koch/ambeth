package com.koch.ambeth.example.helloworld;

/*-
 * #%L
 * jambeth-examples
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

import com.koch.ambeth.example.bytecode.ExampleEntity;
import com.koch.ambeth.example.validation.ExampleValidation;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.ioc.ChangeControllerModule;
import com.koch.ambeth.util.IDedicatedConverterExtendable;

public class HelloWorldModule implements IInitializingModule {
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		beanContextFactory.registerBean("helloWorldService", HelloWorldService.class);

		beanContextFactory.registerBean("helloWorldConverter", HelloWorldConverter.class);
		beanContextFactory.link("helloWorldConverter").to(IDedicatedConverterExtendable.class)
				.with(HelloWorldToken.class, String.class);

		ChangeControllerModule.registerRule(beanContextFactory, ExampleValidation.class,
				ExampleEntity.class);
	}
}
