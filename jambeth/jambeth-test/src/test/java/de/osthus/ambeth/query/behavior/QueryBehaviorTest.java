package de.osthus.ambeth.query.behavior;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import de.osthus.ambeth.cache.model.IServiceResult;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.query.behavior.QueryBehaviorTest.QueryBehaviorTestModule;
import de.osthus.ambeth.service.ICacheService;
import de.osthus.ambeth.service.SyncToAsyncUtil;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.transfer.ServiceDescription;
import de.osthus.ambeth.util.ParamChecker;

@SQLData("/de/osthus/ambeth/persistence/jdbc/Example_data.sql")
@SQLStructure("/de/osthus/ambeth/persistence/jdbc/JDBCDatabase_structure.sql")
@TestModule(QueryBehaviorTestModule.class)
@TestPropertiesList({ @TestProperties(name = PersistenceConfigurationConstants.DatabaseTablePrefix, value = "D_"),
		@TestProperties(name = PersistenceConfigurationConstants.DatabaseFieldPrefix, value = "F_"),
		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "orm.xml") })
@Ignore
public class QueryBehaviorTest extends AbstractPersistenceTest
{
	public static class QueryBehaviorTestModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerBean("queryBehaviorService", QueryBehaviorService.class).autowireable(IQueryBehaviorService.class);
		}
	}

	protected ICacheService cacheService;

	protected IQueryBehaviorService queryBehaviorService;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(cacheService, "cacheService");
		ParamChecker.assertNotNull(queryBehaviorService, "queryBehaviorService");
	}

	public void setCacheService(ICacheService cacheService)
	{
		this.cacheService = cacheService;
	}

	public void setQueryBehaviorService(IQueryBehaviorService queryBehaviorService)
	{
		this.queryBehaviorService = queryBehaviorService;
	}

	protected IServiceResult callThroughCacheService(String methodName) throws Exception
	{
		ServiceDescription serviceDescription = SyncToAsyncUtil.createServiceDescription("QueryBehaviorService",
				IQueryBehaviorService.class.getMethod(methodName, String.class), new Object[] { "test 3" });
		return cacheService.getORIsForServiceRequest(serviceDescription);
	}

	@Test
	public void getORIsForServiceRequestNormal() throws Exception
	{
		IServiceResult serviceResult = callThroughCacheService("getMaterialByName");
		Assert.assertNull(serviceResult.getAdditionalInformation());
		Assert.assertNotNull(serviceResult.getObjRefs());
		Assert.assertEquals(1, serviceResult.getObjRefs().size());
		Assert.assertNotNull(serviceResult.getObjRefs().get(0));
	}

	@Test
	public void getORIsForServiceRequestDefaultMode() throws Exception
	{
		IServiceResult serviceResult = callThroughCacheService("getMaterialByNameDefaultMode");
		Assert.assertNull(serviceResult.getAdditionalInformation());
		Assert.assertNotNull(serviceResult.getObjRefs());
		Assert.assertEquals(1, serviceResult.getObjRefs().size());
		Assert.assertNotNull(serviceResult.getObjRefs().get(0));
	}

	@Test
	public void getORIsForServiceRequestObjRefMode() throws Exception
	{
		IServiceResult serviceResult = callThroughCacheService("getMaterialByNameObjRefMode");
		Assert.assertNull(serviceResult.getAdditionalInformation());
		Assert.assertNotNull(serviceResult.getObjRefs());
		Assert.assertEquals(1, serviceResult.getObjRefs().size());
		Assert.assertNotNull(serviceResult.getObjRefs().get(0));
	}
}
