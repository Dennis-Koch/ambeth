package com.koch.ambeth.informationbus.testutil.contextstore;

public class DirectBeanGetter implements IBeanGetter
{
	private Object bean;

	public void setBean(Object bean)
	{
		this.bean = bean;
	}

	@Override
	public Object getBean(IServiceContextStore contextStore)
	{
		return bean;
	}
}
