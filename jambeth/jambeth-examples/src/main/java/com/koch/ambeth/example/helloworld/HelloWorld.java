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

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.IocModule;
import com.koch.ambeth.ioc.factory.BeanContextFactory;
import com.koch.ambeth.util.IConversionHelper;

public class HelloWorld {
	public static void main(String[] args) {
		IServiceContext rootContext = BeanContextFactory.createBootstrap();
		IServiceContext beanContext =
				rootContext.createService("helloWorld", HelloWorldModule.class, IocModule.class);

		HelloWorldService service = (HelloWorldService) beanContext.getService("helloWorldService");
		service.speak();

		IConversionHelper conversionHelper = beanContext.getService(IConversionHelper.class);
		String helloWorldString =
				conversionHelper.convertValueToType(String.class, new HelloWorldToken());
		System.out.println(helloWorldString);

		beanContext.getRoot().dispose();
	}
}
