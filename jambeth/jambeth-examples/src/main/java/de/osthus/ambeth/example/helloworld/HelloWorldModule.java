package de.osthus.ambeth.example.helloworld;

import de.osthus.ambeth.example.bytecode.ExampleEntity;
import de.osthus.ambeth.example.validation.ExampleValidation;
import de.osthus.ambeth.ioc.ChangeControllerModule;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.util.IDedicatedConverterExtendable;

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
