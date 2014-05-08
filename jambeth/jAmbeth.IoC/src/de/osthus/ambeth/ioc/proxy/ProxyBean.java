package de.osthus.ambeth.ioc.proxy;

import net.sf.cglib.proxy.MethodInterceptor;
import de.osthus.ambeth.ioc.IFactoryBean;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.IProxyFactory;
import de.osthus.ambeth.util.ParamChecker;

public class ProxyBean implements IInitializingBean, IFactoryBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IProxyFactory proxyFactory;

	protected Class<?> type;

	protected Class<?>[] additionalTypes;

	protected MethodInterceptor interceptor;

	protected Object proxy;

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertNotNull(proxyFactory, "ProxyFactory");
		ParamChecker.assertNotNull(type, "Type");

		getProxy();
	}

	public void setAdditionalTypes(Class<?>[] additionalTypes)
	{
		this.additionalTypes = additionalTypes;
	}

	public void setInterceptor(MethodInterceptor interceptor)
	{
		this.interceptor = interceptor;
	}

	public void setProxyFactory(IProxyFactory proxyFactory)
	{
		this.proxyFactory = proxyFactory;
	}

	public void setType(Class<?> type)
	{
		this.type = type;
	}

	protected Object getProxy()
	{
		if (proxy == null)
		{
			if (interceptor != null)
			{
				if (additionalTypes != null)
				{
					proxy = proxyFactory.createProxy(type, additionalTypes, interceptor);
				}
				else
				{
					proxy = proxyFactory.createProxy(type, interceptor);
				}
			}
			else if (additionalTypes != null)
			{
				proxy = proxyFactory.createProxy(type, additionalTypes);
			}
			else
			{
				proxy = proxyFactory.createProxy(type);
			}
		}
		return proxy;
	}

	@Override
	public Object getObject()
	{
		return getProxy();
	}
}
