package com.koch.ambeth.ioc.postprocessor;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.ProcessorOrder;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

public class BeanPostProcessorTestModule implements IInitializingModule
{
	public static final String NumberOfPostProcessors = "numberOfPostProcessors";

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property(name = NumberOfPostProcessors)
	protected int number;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		ProcessorOrder[] orders = ProcessorOrder.values();
		for (int a = number; a-- > 0;)
		{
			ProcessorOrder order = orders[(int) (Math.random() * orders.length)];
			beanContextFactory.registerBean(OrderedPostProcessor.class).propertyValue("Order", order);
		}
	}
}
