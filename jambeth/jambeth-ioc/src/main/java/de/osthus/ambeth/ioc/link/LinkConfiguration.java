package de.osthus.ambeth.ioc.link;

import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.ioc.config.BeanConfiguration;
import de.osthus.ambeth.proxy.IProxyFactory;

public class LinkConfiguration<D> extends BeanConfiguration implements ILinkRegistryNeededConfiguration<D>, ILinkConfigWithOptional, ILinkConfigOptional
{
	public LinkConfiguration(Class<?> beanType, IProxyFactory proxyFactory, IProperties props)
	{
		super(beanType, null, proxyFactory, props);
	}

	@Override
	public ILinkConfigOptional with(Object... arguments)
	{
		propertyValue(AbstractLinkContainer.PROPERTY_ARGUMENTS, arguments);
		return this;
	}

	@Override
	public void optional()
	{
		propertyValue(AbstractLinkContainer.PROPERTY_OPTIONAL, true);
	}

	@Override
	public ILinkConfigWithOptional to(Class<?> autowiredRegistryClass)
	{
		propertyValue(AbstractLinkContainer.PROPERTY_REGISTRY_TYPE, autowiredRegistryClass);
		return this;
	}

	@Override
	public ILinkConfigWithOptional to(Object registry, IEventDelegate<D> eventDelegate)
	{
		return to(registry, eventDelegate.getEventName());
	}

	@Override
	public ILinkConfigWithOptional to(Object registry, Class<?> registryClass)
	{
		propertyValue(AbstractLinkContainer.PROPERTY_REGISTRY, registry);
		propertyValue(AbstractLinkContainer.PROPERTY_REGISTRY_TYPE, registryClass);
		return this;
	}

	@Override
	public ILinkConfigWithOptional to(Object registry, String propertyName)
	{
		propertyValue(AbstractLinkContainer.PROPERTY_REGISTRY, registry);
		propertyValue(AbstractLinkContainer.PROPERTY_REGISTRY_PROPERTY_NAME, propertyName);
		return this;
	}
}
