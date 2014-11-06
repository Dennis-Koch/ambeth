package de.osthus.ambeth.remote;

import net.sf.cglib.proxy.MethodInterceptor;
import de.osthus.ambeth.ioc.IFactoryBean;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.proxy.IProxyFactory;
import de.osthus.ambeth.service.IClientServiceFactory;
import de.osthus.ambeth.util.ParamChecker;

public class ClientServiceBean implements IFactoryBean, IInitializingBean
{
	@Autowired
	protected IClientServiceFactory clientServiceFactory;

	@Autowired
	protected IClientServiceInterceptorBuilder clientServiceInterceptorBuilder;

	@Autowired
	protected IProxyFactory proxyFactory;

	@Autowired
	protected IServiceContext beanContext;

	protected Class<?> syncLocalInterface;

	protected Class<?> syncRemoteInterface;

	protected Class<?> asyncRemoteInterface;

	protected Object proxy;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(clientServiceFactory, "ClientServiceFactory");
		ParamChecker.assertNotNull(clientServiceInterceptorBuilder, "ClientServiceInterceptorBuilder");
		ParamChecker.assertNotNull(proxyFactory, "ProxyFactory");
		ParamChecker.assertNotNull(beanContext, "BeanContext");
		ParamChecker.assertNotNull(syncLocalInterface, "SyncLocalInterface");

		init();
	}

	protected void init()
	{
		MethodInterceptor interceptor = clientServiceInterceptorBuilder.createInterceptor(beanContext, syncLocalInterface, syncRemoteInterface,
				asyncRemoteInterface);
		proxy = proxyFactory.createProxy(syncLocalInterface, interceptor);
	}

	@Override
	public Object getObject() throws Throwable
	{
		if (proxy == null)
		{
			ParamChecker.assertNotNull(clientServiceInterceptorBuilder, "ClientServiceInterceptorBuilder");
			ParamChecker.assertNotNull(proxyFactory, "ProxyFactory");
			ParamChecker.assertNotNull(beanContext, "BeanContext");
			ParamChecker.assertNotNull(syncLocalInterface, "Interface");

			init();
		}
		return proxy;
	}

	public Class<?> getSyncLocalInterface()
	{
		return syncLocalInterface;
	}

	public void setSyncLocalInterface(Class<?> syncLocalInterface)
	{
		this.syncLocalInterface = syncLocalInterface;
	}

	public Class<?> getSyncRemoteInterface()
	{
		return syncRemoteInterface;
	}

	public void setSyncRemoteInterface(Class<?> syncRemoteInterface)
	{
		this.syncRemoteInterface = syncRemoteInterface;
	}

	public Class<?> getAsyncRemoteInterface()
	{
		return asyncRemoteInterface;
	}

	public void setAsyncRemoteInterface(Class<?> asyncRemoteInterface)
	{
		this.asyncRemoteInterface = asyncRemoteInterface;
	}

}
