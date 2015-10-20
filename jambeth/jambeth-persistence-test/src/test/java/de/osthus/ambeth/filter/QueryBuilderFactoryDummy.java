package de.osthus.ambeth.filter;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.query.IQueryBuilderFactory;

public class QueryBuilderFactoryDummy implements IInitializingBean, IQueryBuilderFactory
{
	@Override
	public void afterPropertiesSet() throws Throwable
	{
	}

	@Override
	public <T> IQueryBuilder<T> create(Class<T> entityType)
	{
		return new QueryBuilderDummy<T>();
	}
}
