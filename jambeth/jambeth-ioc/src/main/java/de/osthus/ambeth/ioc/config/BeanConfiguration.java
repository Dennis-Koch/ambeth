package de.osthus.ambeth.ioc.config;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.exception.BeanContextDeclarationException;
import de.osthus.ambeth.ioc.proxy.EmptyInterceptor;
import de.osthus.ambeth.proxy.IProxyFactory;

public class BeanConfiguration extends AbstractBeanConfiguration
{
	protected final Class<?> beanType;

	protected Object createdInstance;

	protected boolean isAbstract;

	protected final IProxyFactory proxyFactory;

	protected final MethodFilter abstractMethodFilter = new MethodFilter()
	{
		@Override
		public boolean isHandled(Method m)
		{
			return Modifier.isAbstract(m.getModifiers());
		}
	};
	protected final MethodHandler abstractMethodHandler = new MethodHandler()
	{
		@Override
		public Object invoke(Object arg0, Method method, Method arg2, Object[] arg3) throws Throwable
		{
			// just want throw the exception, because if(true) there will be warning, so the condition just want a true
			if (method != null)
			{
				throw new UnsupportedOperationException("Should never be called, Because this method[" + method + "] should be intercept by some Intercepter");
			}
			return null;
		}
	};

	public BeanConfiguration(Class<?> beanType, String beanName, IProxyFactory proxyFactory, IProperties props)
	{
		super(beanName, props);
		this.beanType = beanType;
		this.proxyFactory = proxyFactory;
	}

	@Override
	public IBeanConfiguration template()
	{
		isAbstract = true;
		return this;
	}

	@Override
	public Class<?> getBeanType()
	{
		return beanType;
	}

	@Override
	public boolean isAbstract()
	{
		return isAbstract;
	}

	@Override
	public Object getInstance(Class<?> instanceType)
	{
		if (createdInstance == null)
		{
			try
			{
				if (instanceType.isInterface())
				{
					createdInstance = proxyFactory.createProxy(instanceType, EmptyInterceptor.INSTANCE);
				}
				else if (Modifier.isAbstract(instanceType.getModifiers()))
				{
					createdInstance = buildAbstractClassInstance(instanceType);
				}
				else
				{
					createdInstance = instanceType.newInstance();
					if (declarationStackTrace != null && createdInstance instanceof IDeclarationStackTraceAware)
					{
						((IDeclarationStackTraceAware) createdInstance).setDeclarationStackTrace(declarationStackTrace);
					}
				}
			}
			catch (Throwable e)
			{
				if (declarationStackTrace != null)
				{
					throw new BeanContextDeclarationException(declarationStackTrace, e);
				}
				else
				{
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		}
		return createdInstance;
	}

	private Object buildAbstractClassInstance(Class<?> type)
	{
		Class<?>[] interfaces = type.getInterfaces();
		ProxyFactory factory = new ProxyFactory();

		factory.setInterfaces(interfaces);
		factory.setSuperclass(type);
		factory.setUseCache(true);

		factory.setFilter(abstractMethodFilter);

		Proxy proxy;
		try
		{
			proxy = (Proxy) factory.createClass().newInstance();
		}
		catch (InstantiationException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		catch (IllegalAccessException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		proxy.setHandler(abstractMethodHandler);
		return proxy;
	}
}
