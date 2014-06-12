package de.osthus.ambeth.query.sql;

import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.IProxyFactory;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.query.IQueryBuilderFactory;

public class SqlQueryBuilderFactory implements IQueryBuilderFactory
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected IProxyFactory proxyFactory;

	@SuppressWarnings("unchecked")
	@Override
	public <T> IQueryBuilder<T> create(final Class<T> entityType)
	{
		return beanContext.registerAnonymousBean(SqlQueryBuilder.class).propertyValue("EntityType", entityType)
				.propertyValue("DisposeContextOnDispose", Boolean.FALSE).finish();
	}
}
