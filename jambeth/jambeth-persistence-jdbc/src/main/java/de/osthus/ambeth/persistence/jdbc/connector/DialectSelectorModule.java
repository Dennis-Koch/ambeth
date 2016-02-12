package de.osthus.ambeth.persistence.jdbc.connector;

import java.sql.Connection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.IPropertyLoadingBean;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.util.ParamChecker;

@FrameworkModule
public class DialectSelectorModule implements IInitializingModule, IPropertyLoadingBean
{
	public static void fillProperties(Properties props)
	{
		String databaseProtocol = props.getString(PersistenceJdbcConfigurationConstants.DatabaseProtocol);
		if (databaseProtocol == null)
		{
			return;
		}
		IConnector connector = loadConnector(databaseProtocol);
		connector.handleProperties(props, databaseProtocol);
	}

	protected static IConnector loadConnector(String databaseProtocol)
	{
		String connectorName = databaseProtocol.toUpperCase().replace(':', '_');
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		String fqConnectorName = DialectSelectorModule.class.getPackage().getName() + "." + connectorName;
		try
		{
			Class<?> connectorType = classLoader.loadClass(fqConnectorName);
			return (IConnector) connectorType.newInstance();
		}
		catch (Throwable e)
		{
			throw new IllegalStateException("Protocol not supported: '" + databaseProtocol + "'", e);
		}
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property(name = PersistenceJdbcConfigurationConstants.DatabaseProtocol, mandatory = false)
	protected String databaseProtocol;

	@Property(name = PersistenceJdbcConfigurationConstants.DataSourceName, mandatory = false)
	protected String dataSourceName;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		if (databaseProtocol == null)
		{
			// At this point databaseProtocol MUST be initialized
			ParamChecker.assertNotNull(databaseProtocol, "databaseProtocol");
		}
		IConnector connector = loadConnector(databaseProtocol);
		connector.handleProd(beanContextFactory, databaseProtocol);
	}

	@Override
	public void applyProperties(Properties contextProperties)
	{
		if (contextProperties.get(PersistenceJdbcConfigurationConstants.DatabaseProtocol) != null)
		{
			return;
		}

		// If we don't use the integrated connection factory,
		try
		{
			InitialContext ic = new InitialContext();
			DataSource datasource = (DataSource) ic.lookup(dataSourceName);
			Connection connection = datasource.getConnection();
			String connectionUrl = connection.getMetaData().getURL();
			if (contextProperties.get(PersistenceJdbcConfigurationConstants.DatabaseConnection) == null)
			{
				contextProperties.putString(PersistenceJdbcConfigurationConstants.DatabaseConnection, connectionUrl);
			}
			Matcher urlMatcher;
			if (connectionUrl.contains(":@"))
			{
				// Oracle
				// jdbc:oracle:driver:username/password@host:port:database
				urlMatcher = Pattern.compile("^(jdbc:[^:]+:[^:]+)(?::[^:/]+/[^:]+)?:@.*").matcher(connectionUrl);
				// Ignore ([^:]+)(?::(\\d++))?(?::([^:]+))?$ => host:post/database?params
			}
			else
			{
				// Use everything from jdbc to the second :
				// Postgresql, MySql, SqlServer
				// jdbc:driver://host:port/database?user=...
				// jdbc:h2:tcp://localhost/~/test;AUTO_RECONNECT=TRUE
				// Derby, DB2, Sybase, H2 non-urls
				// jdbc:driver:...
				urlMatcher = Pattern.compile("^(jdbc:[^:]+)(:.*)?").matcher(connectionUrl);
			}
			if (urlMatcher.matches())
			{
				String protocol = urlMatcher.group(1);
				contextProperties.putString(PersistenceJdbcConfigurationConstants.DatabaseProtocol, protocol);
				databaseProtocol = protocol;
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e, "The " + getClass().getSimpleName() + " was not able to get the database protocol from the dataSource");
			// Do nothing and hope that the connection is configured elsewhere
		}
	}
}
