package com.koch.ambeth.ioc.link;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.ServiceContext;
import com.koch.ambeth.ioc.config.BeanConfiguration;
import com.koch.ambeth.ioc.config.BeanRuntime;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.util.proxy.IProxyFactory;

public class LinkRuntime<D> extends BeanRuntime<ILinkContainer> implements ILinkRegistryNeededRuntime<D>, ILinkRuntimeWithOptional, ILinkRuntimeOptional,
		ILinkRuntimeFinish
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
	public ILinkRuntimeOptional with(Object... arguments)
	{
		propertyValue(AbstractLinkContainer.PROPERTY_ARGUMENTS, arguments);
		return this;
	}

	@Override
	public ILinkRuntimeFinish optional()
	{
		propertyValue(AbstractLinkContainer.PROPERTY_OPTIONAL, "true");
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
	public ILinkRuntimeWithOptional to(Class<?> autowiredRegistryClass)
	{
		propertyValue(AbstractLinkContainer.PROPERTY_REGISTRY_TYPE, autowiredRegistryClass);
		return this;
	}

	@Override
	public ILinkRuntimeWithOptional to(Object registry, Class<?> registryClass)
	{
		propertyValue(AbstractLinkContainer.PROPERTY_REGISTRY, registry);
		propertyValue(AbstractLinkContainer.PROPERTY_REGISTRY_TYPE, registryClass);
		return this;
	}

	@Override
	public ILinkRuntimeWithOptional to(Object registry, String propertyName)
	{
		propertyValue(AbstractLinkContainer.PROPERTY_REGISTRY, registry);
		propertyValue(AbstractLinkContainer.PROPERTY_REGISTRY_PROPERTY_NAME, propertyName);
		return this;
	}

	@Override
	public ILinkRuntimeWithOptional to(Object registry, IEventDelegate<D> eventDelegate)
	{
		return to(registry, eventDelegate.getEventName());
	}

	@Override
	public ILinkRegistryNeededRuntime<D> toContext(IServiceContext beanContext)
	{
		propertyValue(AbstractLinkContainer.PROPERTY_FOREIGN_BEAN_CONTEXT, beanContext);
		return this;
	}

	@Override
	public ILinkRegistryNeededRuntime<D> toContext(String nameOfBeanContext)
	{
		propertyValue(AbstractLinkContainer.PROPERTY_FOREIGN_BEAN_CONTEXT_NAME, nameOfBeanContext);
		return this;
	}
}
