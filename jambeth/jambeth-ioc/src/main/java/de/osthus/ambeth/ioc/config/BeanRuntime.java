package de.osthus.ambeth.ioc.config;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.ioc.IBeanRuntime;
import de.osthus.ambeth.ioc.ServiceContext;
import de.osthus.ambeth.ioc.factory.BeanContextFactory;
import de.osthus.ambeth.ioc.factory.IBeanContextInitializer;
import de.osthus.ambeth.proxy.IProxyFactory;

public class BeanRuntime<V> implements IBeanRuntime<V>
{
	protected ServiceContext serviceContext;

	protected BeanConfiguration beanConfiguration;

	protected boolean joinLifecycle;

	protected V beanInstance;

	protected Class<? extends V> beanType;

	public BeanRuntime(ServiceContext serviceContext, Class<? extends V> beanType, boolean joinLifecycle)
	{
		this.serviceContext = serviceContext;
		this.beanType = beanType;
		this.joinLifecycle = joinLifecycle;
		beanConfiguration = createBeanConfiguration(beanType);
	}

	public BeanRuntime(ServiceContext serviceContext, V beanInstance, boolean joinLifecycle)
	{
		this.serviceContext = serviceContext;
		this.beanInstance = beanInstance;
		this.joinLifecycle = joinLifecycle;
		beanConfiguration = createBeanConfiguration(beanInstance.getClass());
	}

	protected BeanConfiguration createBeanConfiguration(Class<?> beanType)
	{
		IProxyFactory proxyFactory = serviceContext.getService(IProxyFactory.class, false);
		IProperties props = serviceContext.getService(IProperties.class, true);
		return new BeanConfiguration(beanType, null, proxyFactory, props);
	}

	@SuppressWarnings("unchecked")
	@Override
	public V finish()
	{
		BeanContextFactory beanContextFactory = serviceContext.getBeanContextFactory();
		IBeanContextInitializer beanContextInitializer = beanContextFactory.getBeanContextInitializer();
		IList<IBeanConfiguration> beanConfHierarchy = beanContextInitializer.fillParentHierarchyIfValid(serviceContext, beanContextFactory, beanConfiguration);

		V bean = beanInstance;
		if (bean == null)
		{
			Class<?> beanType = this.beanType;
			if (beanType == null)
			{
				beanType = beanContextInitializer.resolveTypeInHierarchy(beanConfHierarchy);
			}
			bean = (V) beanContextInitializer.instantiateBean(serviceContext, beanContextFactory, beanConfiguration, beanType, beanConfHierarchy);
		}
		bean = (V) beanContextInitializer.initializeBean(serviceContext, beanContextFactory, beanConfiguration, bean, beanConfHierarchy, joinLifecycle);
		return bean;
	}

	@Override
	public IBeanRuntime<V> parent(String parentBeanTemplateName)
	{
		beanConfiguration.parent(parentBeanTemplateName);
		return this;
	}

	@Override
	public IBeanRuntime<V> propertyRef(String propertyName, String beanName)
	{
		beanConfiguration.propertyRef(propertyName, beanName);
		return this;
	}

	@Override
	public IBeanRuntime<V> propertyRef(String propertyName, IBeanConfiguration bean)
	{
		beanConfiguration.propertyRef(propertyName, bean);
		return this;
	}

	@Override
	public IBeanRuntime<V> propertyRefs(String beanName)
	{
		beanConfiguration.propertyRefs(beanName);
		return this;
	}

	@Override
	public IBeanRuntime<V> propertyRefs(String... beanNames)
	{
		beanConfiguration.propertyRefs(beanNames);
		return this;
	}

	@Override
	public IBeanRuntime<V> propertyRef(IBeanConfiguration bean)
	{
		beanConfiguration.propertyRef(bean);
		return this;
	}

	@Override
	public IBeanRuntime<V> propertyValue(String propertyName, Object value)
	{
		beanConfiguration.propertyValue(propertyName, value);
		return this;
	}

	@Override
	public IBeanRuntime<V> ignoreProperties(String propertyName)
	{
		beanConfiguration.ignoreProperties(propertyName);
		return this;
	}

	@Override
	public IBeanRuntime<V> ignoreProperties(String... propertyNames)
	{
		beanConfiguration.ignoreProperties(propertyNames);
		return this;
	}
}
