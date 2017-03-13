package com.koch.ambeth.testutil;

import org.junit.Assert;
import org.junit.runner.RunWith;

import com.koch.ambeth.cache.proxy.IEntityEquals;
import com.koch.ambeth.cache.server.ioc.CacheServerModule;
import com.koch.ambeth.event.datachange.ioc.EventDataChangeModule;
import com.koch.ambeth.event.server.ioc.EventServerModule;
import com.koch.ambeth.expr.ioc.ExprModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.IocConfigurationConstants;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.merge.server.ioc.MergeServerModule;
import com.koch.ambeth.persistence.api.database.ITransaction;
import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
import com.koch.ambeth.persistence.filter.ioc.FilterPersistenceModule;
import com.koch.ambeth.persistence.ioc.PersistenceModule;
import com.koch.ambeth.persistence.jdbc.IConnectionFactory;
import com.koch.ambeth.persistence.jdbc.ioc.PersistenceJdbcModule;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.query.jdbc.ioc.SQLQueryModule;
import com.koch.ambeth.security.ioc.PrivilegeModule;
import com.koch.ambeth.security.persistence.ioc.SecurityQueryModule;
import com.koch.ambeth.security.server.ioc.PrivilegeServerModule;
import com.koch.ambeth.security.server.ioc.SecurityServerModule;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.testutil.AbstractInformationBusTest;
import com.koch.ambeth.testutil.IMeasurement;
import com.koch.ambeth.testutil.IPropertiesProvider;
import com.koch.ambeth.testutil.TestFrameworkModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest.PersistencePropertiesProvider;
import com.koch.ambeth.util.IConversionHelper;

@TestFrameworkModule({ MergeServerModule.class, CacheServerModule.class, EventServerModule.class, EventDataChangeModule.class, ExprModule.class,
		PersistenceModule.class, PersistenceJdbcModule.class, PrivilegeModule.class, PrivilegeServerModule.class, SecurityServerModule.class,
		SecurityQueryModule.class, SQLQueryModule.class, FilterPersistenceModule.class, PreparedStatementParamLoggerModule.class })
@TestProperties(type = PersistencePropertiesProvider.class)
@RunWith(AmbethInformationBusWithPersistenceRunner.class)
public abstract class AbstractInformationBusWithPersistenceTest extends AbstractInformationBusTest
{
	public static class PersistencePropertiesProvider implements IPropertiesProvider
	{
		@Override
		public void fillProperties(Properties props)
		{
			// PersistenceJdbcModule
			props.put(ServiceConfigurationConstants.NetworkClientMode, "false");
			props.put(ServiceConfigurationConstants.SlaveMode, "false");
			props.put(ServiceConfigurationConstants.LogShortNames, "true");
			props.put(PersistenceConfigurationConstants.AutoIndexForeignKeys, "true");

			// IocModule
			props.put(IocConfigurationConstants.UseObjectCollector, "false");
		}
	}

	@Autowired
	protected IConnectionFactory connectionFactory;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IEntityFactory entityFactory;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IMeasurement measurement;

	@Autowired
	protected IQueryBuilderFactory queryBuilderFactory;

	@Autowired
	protected ITransaction transaction;

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
