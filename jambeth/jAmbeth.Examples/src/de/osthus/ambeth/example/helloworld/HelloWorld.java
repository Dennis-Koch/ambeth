package de.osthus.ambeth.example.helloworld;

import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.IocBootstrapModule;
import de.osthus.ambeth.ioc.factory.BeanContextFactory;
import de.osthus.ambeth.util.IConversionHelper;

public class HelloWorld
{
	public static void main(String[] args)
	{
		IServiceContext rootContext = BeanContextFactory.createBootstrap();
		IServiceContext beanContext = rootContext.createService("helloWorld", HelloWorldModule.class, IocBootstrapModule.class);

		HelloWorldService service = (HelloWorldService) beanContext.getService("helloWorldService");
		service.speak();

		IConversionHelper conversionHelper = beanContext.getService(IConversionHelper.class);
		String helloWorldString = conversionHelper.convertValueToType(String.class, new HelloWorldToken());
		System.out.println(helloWorldString);

		beanContext.dispose();
	}
}
