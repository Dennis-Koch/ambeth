package com.koch.ambeth.informationbus.testutil.contextstore;

public interface IInjectNeedTargetContext
{
	IInjectNeedTargetBean in(String contextName);

	IInjectNeedTargetProperty intoBean(IBeanGetter beanGetter);

	IInjectNeedTargetProperty into(Object bean);
}
