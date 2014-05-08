package de.osthus.ambeth.ioc.link;

import de.osthus.ambeth.ioc.ServiceContext;
import de.osthus.ambeth.ioc.config.BeanConfiguration;
import de.osthus.ambeth.ioc.config.BeanRuntime;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.proxy.IProxyFactory;

public class LinkRuntime<D> extends BeanRuntime<ILinkContainer> implements ILinkRegistryNeededRuntime<D>, ILinkRuntime
{
	public LinkRuntime(ServiceContext serviceContext, Class<? extends ILinkContainer> beanType)
	{
		super(serviceContext, beanType, true);
	}

	@Override
	protected BeanConfiguration createBeanConfiguration(Class<?> beanType)
	{
		IProxyFactory proxyFactory = serviceContext.getService(IProxyFactory.class, false);
		return new LinkConfiguration<Object>(beanType, proxyFactory, null);
	}

	@Override
	public ILinkRuntime optional()
	{
		propertyValue(AbstractLinkContainer.PROPERTY_OPTIONAL, "true");
		return this;
	}

	@Override
	public ILinkRuntime with(Object... arguments)
	{
		propertyValue(AbstractLinkContainer.PROPERTY_ARGUMENTS, arguments);
		return this;
	}

	@Override
	public void finishLink()
	{
		finish();
	}

	public ILinkRegistryNeededRuntime<?> listener(IBeanConfiguration listenerBean)
	{
		propertyValue(AbstractLinkContainer.PROPERTY_LISTENER_BEAN, listenerBean);
		return this;
	}

	public ILinkRegistryNeededRuntime<?> listener(String listenerBeanName)
	{
		propertyValue(AbstractLinkContainer.PROPERTY_LISTENER_NAME, listenerBeanName);
		return this;
	}

	public ILinkRegistryNeededRuntime<?> listener(Object listener)
	{
		propertyValue(AbstractLinkContainer.PROPERTY_LISTENER, listener);
		return this;
	}

	public ILinkRegistryNeededRuntime<?> listenerMethod(String methodName)
	{
		propertyValue(AbstractLinkContainer.PROPERTY_LISTENER_METHOD_NAME, methodName);
		return this;
	}

	@Override
	public ILinkRuntime to(String registryBeanName, Class<?> registryClass)
	{
		propertyValue(AbstractLinkContainer.PROPERTY_REGISTRY_NAME, registryBeanName);
		propertyValue(AbstractLinkContainer.PROPERTY_REGISTRY_TYPE, registryClass);
		return this;
	}

	@Override
	public ILinkRuntime to(String registryBeanName, String propertyName)
	{
		propertyValue(AbstractLinkContainer.PROPERTY_REGISTRY_NAME, registryBeanName);
		propertyValue(AbstractLinkContainer.PROPERTY_REGISTRY_PROPERTY_NAME, propertyName);
		return this;
	}

	@Override
	public ILinkRuntime to(String registryBeanName, IEventDelegate<D> eventDelegate)
	{
		return to(registryBeanName, eventDelegate.getEventName());
	}

	@Override
	public ILinkRuntime to(Class<?> autowiredRegistryClass)
	{
		propertyValue(AbstractLinkContainer.PROPERTY_REGISTRY_TYPE, autowiredRegistryClass);
		return this;
	}

	@Override
	public ILinkRuntime to(Object registry, Class<?> registryClass)
	{
		propertyValue(AbstractLinkContainer.PROPERTY_REGISTRY, registry);
		propertyValue(AbstractLinkContainer.PROPERTY_REGISTRY_TYPE, registryClass);
		return this;
	}

	@Override
	public ILinkRuntime to(Object registry, String propertyName)
	{
		propertyValue(AbstractLinkContainer.PROPERTY_REGISTRY, registry);
		propertyValue(AbstractLinkContainer.PROPERTY_REGISTRY_PROPERTY_NAME, propertyName);
		return this;
	}

	@Override
	public ILinkRuntime to(Object registry, IEventDelegate<D> eventDelegate)
	{
		return to(registry, eventDelegate.getEventName());
	}

}
