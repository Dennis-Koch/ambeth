package de.osthus.ambeth.ioc.postprocessor;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.ProcessorOrder;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

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
