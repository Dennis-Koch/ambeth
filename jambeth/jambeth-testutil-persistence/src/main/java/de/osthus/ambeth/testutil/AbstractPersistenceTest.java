package de.osthus.ambeth.testutil;

import org.junit.Assert;
import org.junit.runner.RunWith;

import de.osthus.ambeth.config.IocConfigurationConstants;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.database.ITransaction;
import de.osthus.ambeth.filter.ioc.FilterPersistenceModule;
import de.osthus.ambeth.ioc.BytecodeModule;
import de.osthus.ambeth.ioc.CacheBytecodeModule;
import de.osthus.ambeth.ioc.CacheDataChangeModule;
import de.osthus.ambeth.ioc.CacheModule;
import de.osthus.ambeth.ioc.CacheServerModule;
import de.osthus.ambeth.ioc.EventDataChangeModule;
import de.osthus.ambeth.ioc.EventModule;
import de.osthus.ambeth.ioc.EventServerModule;
import de.osthus.ambeth.ioc.ExprModule;
import de.osthus.ambeth.ioc.MappingModule;
import de.osthus.ambeth.ioc.MergeBytecodeModule;
import de.osthus.ambeth.ioc.MergeModule;
import de.osthus.ambeth.ioc.MergeServerModule;
import de.osthus.ambeth.ioc.ObjectCopierModule;
import de.osthus.ambeth.ioc.PersistenceJdbcModule;
import de.osthus.ambeth.ioc.PersistenceModule;
import de.osthus.ambeth.ioc.PrivilegeModule;
import de.osthus.ambeth.ioc.PrivilegeServerModule;
import de.osthus.ambeth.ioc.SecurityBytecodeModule;
import de.osthus.ambeth.ioc.SecurityModule;
import de.osthus.ambeth.ioc.SecurityQueryModule;
import de.osthus.ambeth.ioc.SecurityServerModule;
import de.osthus.ambeth.ioc.ServiceModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.persistence.jdbc.IConnectionFactory;
import de.osthus.ambeth.proxy.IEntityEquals;
import de.osthus.ambeth.query.IQueryBuilderFactory;
import de.osthus.ambeth.query.ioc.SQLQueryModule;
import de.osthus.ambeth.testutil.AbstractPersistenceTest.PersistencePropertiesProvider;
import de.osthus.ambeth.util.IConversionHelper;

@TestFrameworkModule({ BytecodeModule.class, CacheBytecodeModule.class, MergeBytecodeModule.class, ServiceModule.class, MergeModule.class, MappingModule.class,
		MergeServerModule.class, CacheModule.class, CacheServerModule.class, CacheDataChangeModule.class, EventModule.class, EventServerModule.class,
		EventDataChangeModule.class, ExprModule.class, ObjectCopierModule.class, PersistenceModule.class, PersistenceJdbcModule.class, PrivilegeModule.class,
		PrivilegeServerModule.class, SecurityModule.class, SecurityBytecodeModule.class, SecurityServerModule.class, SecurityQueryModule.class,
		SQLQueryModule.class, FilterPersistenceModule.class })
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
			props.put(ServiceConfigurationConstants.NetworkClientMode, "false");
			props.put(ServiceConfigurationConstants.SlaveMode, "false");
			props.put(ServiceConfigurationConstants.LogShortNames, "true");

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
