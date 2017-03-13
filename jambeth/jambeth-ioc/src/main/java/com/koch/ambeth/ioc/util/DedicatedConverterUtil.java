package com.koch.ambeth.ioc.util;

import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.util.IDedicatedConverterExtendable;

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

	public static void biLink(IBeanContextFactory beanContextFactory, IBeanConfiguration listenerBC, Class<?> fromType, Class<?> toType)
	{
		beanContextFactory.link(listenerBC).to(IDedicatedConverterExtendable.class).with(fromType, toType);
		beanContextFactory.link(listenerBC).to(IDedicatedConverterExtendable.class).with(toType, fromType);
	}

	public static void link(IBeanContextFactory beanContextFactory, String listenerBeanName, Class<?> fromType, Class<?> toType)
	{
		beanContextFactory.link(listenerBeanName).to(IDedicatedConverterExtendable.class).with(fromType, toType);
	}

	public static void link(IBeanContextFactory beanContextFactory, IBeanConfiguration listenerBC, Class<?> fromType, Class<?> toType)
	{
		beanContextFactory.link(listenerBC).to(IDedicatedConverterExtendable.class).with(fromType, toType);
	}
}
