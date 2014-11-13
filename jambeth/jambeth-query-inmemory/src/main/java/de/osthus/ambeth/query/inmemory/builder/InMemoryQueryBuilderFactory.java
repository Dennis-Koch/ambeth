package de.osthus.ambeth.query.inmemory.builder;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.IProxyFactory;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.query.IQueryBuilderFactory;
import de.osthus.ambeth.util.ParamChecker;

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
