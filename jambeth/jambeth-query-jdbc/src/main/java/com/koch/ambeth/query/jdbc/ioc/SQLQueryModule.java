package com.koch.ambeth.query.jdbc.ioc;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.query.IQueryBuilderExtensionExtendable;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.query.jdbc.sql.ITableAliasProvider;
import com.koch.ambeth.query.jdbc.sql.ListToSqlUtil;
import com.koch.ambeth.query.jdbc.sql.SqlQueryBuilderFactory;
import com.koch.ambeth.query.jdbc.sql.TableAliasProviderFactory;

@FrameworkModule
public class SQLQueryModule implements IInitializingModule
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(SqlQueryBuilderFactory.class).autowireable(IQueryBuilderFactory.class, IQueryBuilderExtensionExtendable.class);
		beanContextFactory.registerBean(ListToSqlUtil.class).autowireable(ListToSqlUtil.class);
		beanContextFactory.registerBean(TableAliasProviderFactory.class).autowireable(ITableAliasProvider.class);
	}
}
