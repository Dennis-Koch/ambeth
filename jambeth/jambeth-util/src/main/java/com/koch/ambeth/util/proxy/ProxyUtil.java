package com.koch.ambeth.util.proxy;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Factory;

public class ProxyUtil
{
	private ProxyUtil()
	{
		// Intended blank
	}

	public static Object getProxiedBean(Object proxy)
	{
		Object bean = proxy;

		if (bean instanceof Factory)
		{
			Factory factory = (Factory) bean;
			Callback callback = factory.getCallback(0);
			bean = callback;
			while (bean instanceof ICascadedInterceptor)
			{
				ICascadedInterceptor cascadedInterceptor = (ICascadedInterceptor) bean;
				bean = cascadedInterceptor.getTarget();
			}
		}

		return bean;
	}
}
