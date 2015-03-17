package de.osthus.ambeth.ioc.link;

import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.ioc.config.BeanConfiguration;
import de.osthus.ambeth.proxy.IProxyFactory;

public class LinkConfiguration<D> extends BeanConfiguration implements ILinkConfiguration, ILinkRegistryNeededConfiguration<D>
{
	public LinkConfiguration(Class<?> beanType, IProxyFactory proxyFactory, IProperties props)
	{
		super(beanType, null, proxyFactory, props);
	}

	@Override
	public ILinkConfiguration with(Object... arguments)
	{
		propertyValue(AbstractLinkContainer.PROPERTY_ARGUMENTS, arguments);
		return this;
	}

	@Override
	public ILinkConfiguration optional()
	{
		propertyValue(AbstractLinkContainer.PROPERTY_OPTIONAL, true);
		return this;
	}

	@Override
	public ILinkConfiguration to(String registryBeanName, Class<?> registryClass)
	{
		propertyValue(AbstractLinkContainer.PROPERTY_REGISTRY_NAME, registryBeanName);
		propertyValue(AbstractLinkContainer.PROPERTY_REGISTRY_TYPE, registryClass);
		return this;
	}

	@Override
	public ILinkConfiguration to(String registryBeanName, String propertyName)
	{
		propertyValue(AbstractLinkContainer.PROPERTY_REGISTRY_NAME, registryBeanName);
		propertyValue(AbstractLinkContainer.PROPERTY_REGISTRY_PROPERTY_NAME, propertyName);
		return this;
	}

	@Override
	public ILinkConfiguration to(String registryBeanName, IEventDelegate<D> eventDelegate)
	{
		return to(registryBeanName, eventDelegate.getEventName());
	}

	@Override
	public ILinkConfiguration to(Class<?> autowiredRegistryClass)
	{
		propertyValue(AbstractLinkContainer.PROPERTY_REGISTRY_TYPE, autowiredRegistryClass);
		return this;
	}

	@Override
	public ILinkConfiguration to(Object registry, IEventDelegate<D> eventDelegate)
	{
		return to(registry, eventDelegate.getEventName());
	}

	@Override
	public ILinkConfiguration to(Object registry, Class<?> registryClass)
	{
		propertyValue(AbstractLinkContainer.PROPERTY_REGISTRY, registry);
		propertyValue(AbstractLinkContainer.PROPERTY_REGISTRY_TYPE, registryClass);
		return this;
	}

	@Override
	public ILinkConfiguration to(Object registry, String propertyName)
	{
		propertyValue(AbstractLinkContainer.PROPERTY_REGISTRY, registry);
		propertyValue(AbstractLinkContainer.PROPERTY_REGISTRY_PROPERTY_NAME, propertyName);
		return this;
	}
}
