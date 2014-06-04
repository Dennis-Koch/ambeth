package de.osthus.ambeth.testutil.contextstore;

import de.osthus.ambeth.ioc.IServiceContext;

public interface IServiceContextStore
{
	IServiceContext getContext(String contextName);

	IInjectNeedBean injectFrom(String contextName);

	IInjectNeedTargetContext injectBean(IBeanGetter beanGetter);

	IInjectNeedTargetContext inject(Object bean);
}