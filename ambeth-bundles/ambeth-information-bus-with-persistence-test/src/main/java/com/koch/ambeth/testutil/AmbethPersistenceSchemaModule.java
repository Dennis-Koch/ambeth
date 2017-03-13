package com.koch.ambeth.testutil;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.IocModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.persistence.IPersistenceHelper;
import com.koch.ambeth.persistence.PersistenceHelper;
import com.koch.ambeth.persistence.api.sql.ISqlBuilder;
import com.koch.ambeth.persistence.jdbc.IConnectionFactory;
import com.koch.ambeth.persistence.jdbc.connection.ConnectionFactory;
import com.koch.ambeth.persistence.jdbc.testconnector.DialectSelectorSchemaModule;
import com.koch.ambeth.persistence.orm.IOrmPatternMatcher;
import com.koch.ambeth.persistence.orm.OrmPatternMatcher;
import com.koch.ambeth.persistence.sql.ISqlKeywordRegistry;
import com.koch.ambeth.persistence.sql.SqlBuilder;
import com.koch.ambeth.persistence.util.IPersistenceExceptionUtil;
import com.koch.ambeth.persistence.util.PersistenceExceptionUtil;

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
