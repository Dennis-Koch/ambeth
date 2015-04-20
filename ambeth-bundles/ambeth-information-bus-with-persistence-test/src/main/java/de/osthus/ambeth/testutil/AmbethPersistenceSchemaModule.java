package de.osthus.ambeth.testutil;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.IocModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.orm.IOrmPatternMatcher;
import de.osthus.ambeth.orm.OrmPatternMatcher;
import de.osthus.ambeth.persistence.IPersistenceHelper;
import de.osthus.ambeth.persistence.PersistenceHelper;
import de.osthus.ambeth.persistence.jdbc.IConnectionFactory;
import de.osthus.ambeth.persistence.jdbc.connection.ConnectionFactory;
import de.osthus.ambeth.sql.ISqlBuilder;
import de.osthus.ambeth.sql.ISqlKeywordRegistry;
import de.osthus.ambeth.sql.SqlBuilder;
import de.osthus.ambeth.util.IPersistenceExceptionUtil;
import de.osthus.ambeth.util.PersistenceExceptionUtil;

public class AmbethPersistenceSchemaModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(final IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(ConnectionFactory.class).autowireable(IConnectionFactory.class);
		beanContextFactory.registerBean(PersistenceExceptionUtil.class).autowireable(IPersistenceExceptionUtil.class);
		beanContextFactory.registerBean(OrmPatternMatcher.class).autowireable(IOrmPatternMatcher.class);
		beanContextFactory.registerBean(SqlBuilder.class).autowireable(ISqlBuilder.class, ISqlKeywordRegistry.class);
		beanContextFactory.registerBean(PersistenceHelper.class).autowireable(IPersistenceHelper.class);

		beanContextFactory.registerBean(IocModule.class);
		beanContextFactory.registerBean(DialectSelectorSchemaModule.class);
	}
}
