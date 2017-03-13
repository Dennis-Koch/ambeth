package com.koch.ambeth.example.helloworld;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.IocModule;
import com.koch.ambeth.ioc.factory.BeanContextFactory;
import com.koch.ambeth.util.IConversionHelper;

public class HelloWorld {
	public static void main(String[] args) {
		IServiceContext rootContext = BeanContextFactory.createBootstrap();
		IServiceContext beanContext = rootContext.createService("helloWorld", HelloWorldModule.class, IocModule.class);

		HelloWorldService service = (HelloWorldService) beanContext.getService("helloWorldService");
		service.speak();

		IConversionHelper conversionHelper = beanContext.getService(IConversionHelper.class);
		String helloWorldString = conversionHelper.convertValueToType(String.class, new HelloWorldToken());
		System.out.println(helloWorldString);

		beanContext.getRoot().dispose();
	}
}
