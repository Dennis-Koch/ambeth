package com.koch.ambeth.informationbus.testutil.contextstore;

import com.koch.ambeth.ioc.IServiceContext;

public interface IServiceContextStore
{
	IServiceContext getContext(String contextName);

	IInjectNeedBean injectFrom(String contextName);

	IInjectNeedTargetContext injectBean(IBeanGetter beanGetter);

	IInjectNeedTargetContext inject(Object bean);
}
