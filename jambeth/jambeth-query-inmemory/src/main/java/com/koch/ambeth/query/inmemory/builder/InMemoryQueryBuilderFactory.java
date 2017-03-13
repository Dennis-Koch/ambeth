package com.koch.ambeth.query.inmemory.builder;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.proxy.IProxyFactory;

public class InMemoryQueryBuilderFactory implements IQueryBuilderFactory, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IServiceContext beanContext;

	protected IProxyFactory proxyFactory;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(beanContext, "beanContext");
		ParamChecker.assertNotNull(proxyFactory, "proxyFactory");
	}

	public void setBeanContext(IServiceContext beanContext)
	{
		this.beanContext = beanContext;
	}

	public void setProxyFactory(IProxyFactory proxyFactory)
	{
		this.proxyFactory = proxyFactory;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> IQueryBuilder<T> create(Class<T> entityType)
	{
		return beanContext.registerBean(InMemoryQueryBuilder.class).propertyValue("EntityType", entityType).finish();
	}
}
