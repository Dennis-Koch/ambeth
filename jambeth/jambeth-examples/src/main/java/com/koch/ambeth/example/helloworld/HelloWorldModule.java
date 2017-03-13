package com.koch.ambeth.example.helloworld;

import com.koch.ambeth.example.bytecode.ExampleEntity;
import com.koch.ambeth.example.validation.ExampleValidation;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.ioc.ChangeControllerModule;
import com.koch.ambeth.util.IDedicatedConverterExtendable;

public class HelloWorldModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("helloWorldService", HelloWorldService.class);

		beanContextFactory.registerBean("helloWorldConverter", HelloWorldConverter.class);
		beanContextFactory.link("helloWorldConverter").to(IDedicatedConverterExtendable.class).with(HelloWorldToken.class, String.class);

		ChangeControllerModule.registerRule(beanContextFactory, ExampleValidation.class, ExampleEntity.class);
	}
}
