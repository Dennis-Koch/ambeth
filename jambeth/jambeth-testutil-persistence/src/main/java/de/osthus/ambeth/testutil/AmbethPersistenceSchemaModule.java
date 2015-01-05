package de.osthus.ambeth.testutil;

import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.IPropertyLoadingBean;
import de.osthus.ambeth.ioc.IocBootstrapModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.orm.IOrmPatternMatcher;
import de.osthus.ambeth.orm.OrmPatternMatcher;
import de.osthus.ambeth.persistence.IPersistenceHelper;
import de.osthus.ambeth.persistence.PersistenceHelper;
import de.osthus.ambeth.persistence.jdbc.IConnectionFactory;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.connection.ConnectionFactory;
import de.osthus.ambeth.sql.ISqlBuilder;
import de.osthus.ambeth.sql.ISqlKeywordRegistry;
import de.osthus.ambeth.sql.SqlBuilder;
import de.osthus.ambeth.util.IPersistenceExceptionUtil;
import de.osthus.ambeth.util.PersistenceExceptionUtil;

public class AmbethPersistenceSchemaModule implements IInitializingModule, IPropertyLoadingBean
{
	@Override
	public void applyProperties(Properties contextProperties)
	{
		String databaseConnection = contextProperties.getString(PersistenceJdbcConfigurationConstants.DatabaseConnection);
		if (databaseConnection == null)
		{
			contextProperties.put(PersistenceJdbcConfigurationConstants.DatabaseConnection, "${" + PersistenceJdbcConfigurationConstants.DatabaseProtocol
					+ "}:@" + "${" + PersistenceJdbcConfigurationConstants.DatabaseHost + "}" + ":" + "${" + PersistenceJdbcConfigurationConstants.DatabasePort
					+ "}" + ":" + "${" + PersistenceJdbcConfigurationConstants.DatabaseName + "}");
		}
		// contextProperties.put("ambeth.log.level.de.osthus.ambeth.persistence.jdbc.connection.LogStatementInterceptor", "INFO");
		// contextProperties.put("ambeth.log.level.de.osthus.ambeth.persistence.jdbc.JDBCDatabaseWrapper", "INFO");
	}

	@Override
	public void afterPropertiesSet(final IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean(IocBootstrapModule.class);
		beanContextFactory.registerBean(DialectSelectorSchemaModule.class);
		beanContextFactory.registerBean(ConnectionFactory.class).autowireable(IConnectionFactory.class);
		beanContextFactory.registerBean(PersistenceExceptionUtil.class).autowireable(IPersistenceExceptionUtil.class);
		beanContextFactory.registerBean(OrmPatternMatcher.class).autowireable(IOrmPatternMatcher.class);
		beanContextFactory.registerBean(SqlBuilder.class).autowireable(ISqlBuilder.class, ISqlKeywordRegistry.class);
		beanContextFactory.registerBean(PersistenceHelper.class).autowireable(IPersistenceHelper.class);
	}
}
