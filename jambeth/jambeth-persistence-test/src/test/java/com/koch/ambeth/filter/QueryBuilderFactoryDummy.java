package com.koch.ambeth.filter;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.IQueryBuilderFactory;

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
