package com.koch.ambeth.ioc.injection;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;

public class InjectionTestModule implements IInitializingModule
{
	public static final int BEAN_COUNT = 10000;

	public static final String NAME = "injectionTestBean-";

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		for (int i = 0; i < BEAN_COUNT; i++)
		{
			int previousNumber = (BEAN_COUNT + i) % BEAN_COUNT;
			int counterpartNumber = ((int) (BEAN_COUNT * 1.5) + i) % BEAN_COUNT;
			String serviceName = NAME + i;
			String previousName = NAME + previousNumber;
			String counterpartName = NAME + counterpartNumber;
			beanContextFactory.registerBean(serviceName, InjectionTestBean.class).propertyValue("Name", serviceName).propertyRef("Previous", previousName)
					.propertyRef("Counterpart", counterpartName);
		}
	}
}
