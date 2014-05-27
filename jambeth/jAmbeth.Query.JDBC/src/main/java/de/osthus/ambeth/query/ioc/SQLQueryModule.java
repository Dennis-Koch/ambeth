package de.osthus.ambeth.query.ioc;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.query.IQueryBuilderFactory;
import de.osthus.ambeth.query.sql.ITableAliasProvider;
import de.osthus.ambeth.query.sql.ListToSqlUtil;
import de.osthus.ambeth.query.sql.SqlQueryBuilderFactory;
import de.osthus.ambeth.query.sql.TableAliasProviderFactory;

@FrameworkModule
public class SQLQueryModule implements IInitializingModule
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("sqlQueryBuilderFactory", SqlQueryBuilderFactory.class).autowireable(IQueryBuilderFactory.class);
		beanContextFactory.registerBean("listToSqlUtil", ListToSqlUtil.class).autowireable(ListToSqlUtil.class);
		beanContextFactory.registerBean("tableAliasProviderFactory", TableAliasProviderFactory.class).autowireable(ITableAliasProvider.class);
	}
}
