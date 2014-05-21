package de.osthus.ambeth.testutil;

import org.junit.Assert;
import org.junit.runner.RunWith;

import de.osthus.ambeth.config.IocConfigurationConstants;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.database.ITransaction;
import de.osthus.ambeth.filter.ioc.FilterPersistenceModule;
import de.osthus.ambeth.ioc.BytecodeModule;
import de.osthus.ambeth.ioc.CacheBytecodeModule;
import de.osthus.ambeth.ioc.CacheDataChangeModule;
import de.osthus.ambeth.ioc.CacheModule;
import de.osthus.ambeth.ioc.CacheServerModule;
import de.osthus.ambeth.ioc.CompositeIdModule;
import de.osthus.ambeth.ioc.EventDataChangeModule;
import de.osthus.ambeth.ioc.EventModule;
import de.osthus.ambeth.ioc.EventServerModule;
import de.osthus.ambeth.ioc.MappingModule;
import de.osthus.ambeth.ioc.MergeModule;
import de.osthus.ambeth.ioc.MergeServerModule;
import de.osthus.ambeth.ioc.ObjectCopierModule;
import de.osthus.ambeth.ioc.PersistenceJdbcModule;
import de.osthus.ambeth.ioc.PersistenceModule;
import de.osthus.ambeth.ioc.PrivilegeServerModule;
import de.osthus.ambeth.ioc.SecurityModule;
import de.osthus.ambeth.ioc.SecurityServerModule;
import de.osthus.ambeth.ioc.ServiceModule;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.oracle.Oracle10gModule;
import de.osthus.ambeth.persistence.jdbc.IConnectionFactory;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.proxy.IEntityEquals;
import de.osthus.ambeth.query.IQueryBuilderFactory;
import de.osthus.ambeth.query.ioc.SQLQueryModule;
import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.testutil.AbstractPersistenceTest.PersistencePropertiesProvider;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.ParamChecker;

@TestFrameworkModule({ BytecodeModule.class, CacheBytecodeModule.class, CompositeIdModule.class, ServiceModule.class, MergeModule.class, MappingModule.class,
		MergeServerModule.class, CacheModule.class, CacheServerModule.class, CacheDataChangeModule.class, EventModule.class, EventServerModule.class,
		EventDataChangeModule.class, ObjectCopierModule.class, PersistenceModule.class, PersistenceJdbcModule.class, PrivilegeServerModule.class,
		SecurityModule.class, SecurityServerModule.class, Oracle10gModule.class, SQLQueryModule.class, FilterPersistenceModule.class })
@TestProperties(type = PersistencePropertiesProvider.class)
@RunWith(AmbethPersistenceRunner.class)
public abstract class AbstractPersistenceTest extends AbstractIocTest
{
	public static class PersistencePropertiesProvider implements IPropertiesProvider
	{
		@Override
		public void fillProperties(Properties props)
		{
			// PersistenceJdbcModule
			props.put(PersistenceJdbcConfigurationConstants.DatabaseConnection, "${" + PersistenceJdbcConfigurationConstants.DatabaseProtocol + "}:@" + "${"
					+ PersistenceJdbcConfigurationConstants.DatabaseHost + "}" + ":" + "${" + PersistenceJdbcConfigurationConstants.DatabasePort + "}" + ":"
					+ "${" + PersistenceJdbcConfigurationConstants.DatabaseName + "}");

			props.put(ConfigurationConstants.NetworkClientMode, "false");
			props.put(ConfigurationConstants.SlaveMode, "false");
			props.put(ConfigurationConstants.LogShortNames, "true");

			props.put(PersistenceJdbcConfigurationConstants.AdditionalConnectionInterfaces, "oracle.jdbc.OracleConnection");
			props.put(PersistenceJdbcConfigurationConstants.AdditionalConnectionModules, "de.osthus.ambeth.oracle.Oracle10gConnectionModule");

			// IocModule
			props.put(IocConfigurationConstants.UseObjectCollector, "false");
		}
	}

	protected IConnectionFactory connectionFactory;

	protected IConversionHelper conversionHelper;

	protected IEntityFactory entityFactory;

	protected IEntityMetaDataProvider entityMetaDataProvider;

	protected IMeasurement measurement;

	protected IQueryBuilderFactory queryBuilderFactory;

	protected ITransaction transaction;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();
		ParamChecker.assertNotNull(connectionFactory, "ConnectionFactory");
		ParamChecker.assertNotNull(conversionHelper, "ConversionHelper");
		ParamChecker.assertNotNull(entityFactory, "EntityFactory");
		ParamChecker.assertNotNull(entityMetaDataProvider, "EntityMetaDataProvider");
		ParamChecker.assertNotNull(queryBuilderFactory, "QueryBuilderFactory");
		ParamChecker.assertNotNull(transaction, "Transaction");
	}

	public void setConnectionFactory(IConnectionFactory connectionFactory)
	{
		this.connectionFactory = connectionFactory;
	}

	public void setConversionHelper(IConversionHelper conversionHelper)
	{
		this.conversionHelper = conversionHelper;
	}

	public void setEntityFactory(IEntityFactory entityFactory)
	{
		this.entityFactory = entityFactory;
	}

	public void setEntityMetaDataProvider(IEntityMetaDataProvider entityMetaDataProvider)
	{
		this.entityMetaDataProvider = entityMetaDataProvider;
	}

	public void setMeasurement(IMeasurement measurement)
	{
		this.measurement = measurement;
	}

	public void setQueryBuilderFactory(IQueryBuilderFactory queryBuilderFactory)
	{
		this.queryBuilderFactory = queryBuilderFactory;
	}

	public void setTransaction(ITransaction transaction)
	{
		this.transaction = transaction;
	}

	public void assertProxyEquals(Object expected, Object actual)
	{
		assertProxyEquals("", expected, actual);
	}

	public void assertProxyEquals(String message, Object expected, Object actual)
	{
		if (expected == actual)
		{
			// Nothing to do
			return;
		}
		if (expected == null)
		{
			if (actual == null)
			{
				return;
			}
			else
			{
				Assert.fail("expected:<" + expected + "> but was:<" + actual + ">");
			}
		}
		else if (actual == null)
		{
			Assert.fail("expected:<" + expected + "> but was:<" + actual + ">");
		}
		if (expected instanceof IEntityEquals)
		{
			if (expected.equals(actual))
			{
				return;
			}
			IEntityEquals expectedEE = (IEntityEquals) expected;
			Assert.fail("expected:<" + expectedEE.toString() + "> but was:<" + actual + ">");
		}
		IEntityMetaData expectedMetaData = entityMetaDataProvider.getMetaData(expected.getClass());
		IEntityMetaData actualMetaData = entityMetaDataProvider.getMetaData(actual.getClass());
		Class<?> expectedType = expectedMetaData.getEntityType();
		Class<?> actualType = actualMetaData.getEntityType();
		if (!expectedType.equals(actualType))
		{
			Assert.fail("expected:<" + expected + "> but was:<" + actual + ">");
		}
		Object expectedId = expectedMetaData.getIdMember().getValue(expected, false);
		Object actualId = actualMetaData.getIdMember().getValue(actual, false);
		Assert.assertEquals(expectedId, actualId);
	}
}
