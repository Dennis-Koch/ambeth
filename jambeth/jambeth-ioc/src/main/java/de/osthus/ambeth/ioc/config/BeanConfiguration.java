package de.osthus.ambeth.ioc.config;

import java.lang.reflect.Modifier;

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
				if (instanceType.isInterface() || Modifier.isAbstract(instanceType.getModifiers()))
				{
					createdInstance = proxyFactory.createProxy(instanceType, EmptyInterceptor.INSTANCE);
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
}
