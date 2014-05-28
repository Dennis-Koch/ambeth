package de.osthus.ambeth.util;

import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

public final class DedicatedConverterUtil
{
	private DedicatedConverterUtil()
	{
		// Intended blank
	}

	public static void biLink(IBeanContextFactory beanContextFactory, String listenerBeanName, Class<?> fromType, Class<?> toType)
	{
		beanContextFactory.link(listenerBeanName).to(IDedicatedConverterExtendable.class).with(fromType, toType);
		beanContextFactory.link(listenerBeanName).to(IDedicatedConverterExtendable.class).with(toType, fromType);
	}
}
