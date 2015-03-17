package de.osthus.ambeth.testutil.contextstore;

import de.osthus.ambeth.ioc.IServiceContext;

public class BeanByNameGetter implements IBeanGetter
{
	private String contextName;

	private String beanName;

	public void setContextName(String contextName)
	{
		this.contextName = contextName;
	}

	public void setBeanName(String beanName)
	{
		this.beanName = beanName;
	}

	@Override
	public Object getBean(IServiceContextStore contextStore)
	{
		IServiceContext context = contextStore.getContext(contextName);
		if (context == null)
		{
			throw new IllegalStateException("Service context '" + contextName + "' not found");
		}
		Object bean = context.getService(beanName, true);
		return bean;
	}
}
