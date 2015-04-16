package de.osthus.ambeth.training.travelguides;

import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.IocModule;
import de.osthus.ambeth.ioc.factory.BeanContextFactory;
import de.osthus.ambeth.training.travelguides.ioc.HelloWorldModule;

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
