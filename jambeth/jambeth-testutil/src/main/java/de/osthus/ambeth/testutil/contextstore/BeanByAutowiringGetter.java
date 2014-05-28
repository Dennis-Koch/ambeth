package de.osthus.ambeth.testutil.contextstore;

import de.osthus.ambeth.ioc.IServiceContext;

public class BeanByAutowiringGetter implements IBeanGetter
{
	private String contextName;

	private Class<?> beanType;

	public void setContextName(String contextName)
	{
		this.contextName = contextName;
	}

	public void setBeanType(Class<?> beanType)
	{
		this.beanType = beanType;
	}

	@Override
	public Object getBean(IServiceContextStore contextStore)
	{
		IServiceContext context = contextStore.getContext(contextName);
		if (context == null)
		{
			throw new IllegalStateException("Service context '" + contextName + "' not found");
		}
		Object bean = context.getService(beanType, true);
		return bean;
	}
}
