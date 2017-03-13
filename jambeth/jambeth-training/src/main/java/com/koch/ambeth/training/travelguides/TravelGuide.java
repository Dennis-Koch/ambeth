package com.koch.ambeth.training.travelguides;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.IocModule;
import com.koch.ambeth.ioc.factory.BeanContextFactory;
import com.koch.ambeth.training.travelguides.ioc.HelloWorldModule;

public class TravelGuide
{
	public static void main(String[] args)
	{
		IServiceContext rootContext = BeanContextFactory.createBootstrap();
		IServiceContext beanContext = rootContext.createService("helloWorld", HelloWorldModule.class, IocModule.class);

		TravelGuideService service = (TravelGuideService) beanContext.getService("helloWorldService");
		service.speak();

		beanContext.getRoot().dispose();
	}
}
