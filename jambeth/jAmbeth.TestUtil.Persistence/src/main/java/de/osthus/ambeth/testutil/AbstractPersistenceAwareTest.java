package de.osthus.ambeth.testutil;

import org.junit.Assert;

import de.osthus.ambeth.filter.ioc.FilterPersistenceModule;
import de.osthus.ambeth.ioc.CacheDataChangeModule;
import de.osthus.ambeth.ioc.CacheModule;
import de.osthus.ambeth.ioc.CacheServerModule;
import de.osthus.ambeth.ioc.EventDataChangeModule;
import de.osthus.ambeth.ioc.EventModule;
import de.osthus.ambeth.ioc.EventServerModule;
import de.osthus.ambeth.ioc.IocBootstrapModule;
import de.osthus.ambeth.ioc.MappingModule;
import de.osthus.ambeth.ioc.MergeModule;
import de.osthus.ambeth.ioc.MergeServerModule;
import de.osthus.ambeth.ioc.ObjectCopierModule;
import de.osthus.ambeth.ioc.PersistenceJdbcModule;
import de.osthus.ambeth.ioc.PersistenceModule;
import de.osthus.ambeth.ioc.PrivilegeServerModule;
import de.osthus.ambeth.ioc.SecurityModule;
import de.osthus.ambeth.ioc.SecurityServerModule;
import de.osthus.ambeth.ioc.SensorModule;
import de.osthus.ambeth.ioc.ServiceModule;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.oracle.Oracle10gModule;
import de.osthus.ambeth.query.ioc.SQLQueryModule;

/**
 * Deprecated: Please use <src>de.osthus.ambeth.testutil.AbstractPersistenceTest</src> instead.
 */
@Deprecated
public class AbstractPersistenceAwareTest extends AbstractIocAwareTest
{
	private static Class<?>[] persistenceModules = new Class<?>[] { ServiceModule.class, MergeModule.class, MappingModule.class, MergeServerModule.class,
			CacheModule.class, CacheServerModule.class, CacheDataChangeModule.class, EventModule.class, EventServerModule.class, EventDataChangeModule.class,
			IocBootstrapModule.class, ObjectCopierModule.class, PersistenceModule.class, PersistenceJdbcModule.class, PrivilegeServerModule.class,
			SecurityModule.class, SecurityServerModule.class, SensorModule.class, Oracle10gModule.class, SQLQueryModule.class, FilterPersistenceModule.class };

	private static Class<?>[] mergeModules(Class<?>[] extendingModules)
	{
		return mergeModules(persistenceModules, extendingModules);
	}

	private static Class<?>[] mergeChildModules(Class<?>[] extendingModules)
	{
		return mergeModules(null, extendingModules);
	}

	protected static void setUpBeforeClassPersistence(Class<?>[] moduleTypes) throws Exception
	{
		setUpBeforeClassPersistence(moduleTypes, null);
	}

	protected static void setUpBeforeClassPersistence(Class<?>[] moduleTypes, Class<?>[] childModuleTypes) throws Exception
	{
		setUpBeforeClassIoc(mergeModules(moduleTypes), mergeChildModules(childModuleTypes));
	}

	protected static void tearDownAfterClassPersistence() throws Exception
	{
		AbstractIocAwareTest.tearDownAfterClassIoc();
	}

	public static void assertProxyEquals(Object expected, Object actual)
	{
		assertProxyEquals("", expected, actual);
	}

	public static void assertProxyEquals(String message, Object expected, Object actual)
	{
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

		IEntityMetaDataProvider entityMetaDataProvider = beanContext.getService(IEntityMetaDataProvider.class);
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
