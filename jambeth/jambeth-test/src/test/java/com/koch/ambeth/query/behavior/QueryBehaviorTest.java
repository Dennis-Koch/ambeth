package com.koch.ambeth.query.behavior;

/*-
 * #%L
 * jambeth-test
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.koch.ambeth.cache.service.ICacheService;
import com.koch.ambeth.informationbus.persistence.setup.SQLData;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
import com.koch.ambeth.query.behavior.QueryBehaviorTest.QueryBehaviorTestModule;
import com.koch.ambeth.service.SyncToAsyncUtil;
import com.koch.ambeth.service.cache.model.IServiceResult;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.transfer.ServiceDescription;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.util.ParamChecker;

@SQLData("/com/koch/ambeth/persistence/jdbc/Example_data.sql")
@SQLStructure("/com/koch/ambeth/persistence/jdbc/JDBCDatabase_structure.sql")
@TestModule(QueryBehaviorTestModule.class)
@TestPropertiesList({
		@TestProperties(name = PersistenceConfigurationConstants.DatabaseTablePrefix, value = "D_"),
		@TestProperties(name = PersistenceConfigurationConstants.DatabaseFieldPrefix, value = "F_"),
		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "orm.xml")})
@Ignore
public class QueryBehaviorTest extends AbstractInformationBusWithPersistenceTest {
	public static class QueryBehaviorTestModule implements IInitializingModule {
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
			beanContextFactory.registerBean("queryBehaviorService", QueryBehaviorService.class)
					.autowireable(IQueryBehaviorService.class);
		}
	}

	protected ICacheService cacheService;

	protected IQueryBehaviorService queryBehaviorService;

	@Override
	public void afterPropertiesSet() throws Throwable {
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(cacheService, "cacheService");
		ParamChecker.assertNotNull(queryBehaviorService, "queryBehaviorService");
	}

	public void setCacheService(ICacheService cacheService) {
		this.cacheService = cacheService;
	}

	public void setQueryBehaviorService(IQueryBehaviorService queryBehaviorService) {
		this.queryBehaviorService = queryBehaviorService;
	}

	protected IServiceResult callThroughCacheService(String methodName) throws Exception {
		ServiceDescription serviceDescription = SyncToAsyncUtil.createServiceDescription(
				"QueryBehaviorService", IQueryBehaviorService.class.getMethod(methodName, String.class),
				new Object[] {"test 3"});
		return cacheService.getORIsForServiceRequest(serviceDescription);
	}

	@Test
	public void getORIsForServiceRequestNormal() throws Exception {
		IServiceResult serviceResult = callThroughCacheService("getMaterialByName");
		Assert.assertNull(serviceResult.getAdditionalInformation());
		Assert.assertNotNull(serviceResult.getObjRefs());
		Assert.assertEquals(1, serviceResult.getObjRefs().size());
		Assert.assertNotNull(serviceResult.getObjRefs().get(0));
	}

	@Test
	public void getORIsForServiceRequestDefaultMode() throws Exception {
		IServiceResult serviceResult = callThroughCacheService("getMaterialByNameDefaultMode");
		Assert.assertNull(serviceResult.getAdditionalInformation());
		Assert.assertNotNull(serviceResult.getObjRefs());
		Assert.assertEquals(1, serviceResult.getObjRefs().size());
		Assert.assertNotNull(serviceResult.getObjRefs().get(0));
	}

	@Test
	public void getORIsForServiceRequestObjRefMode() throws Exception {
		IServiceResult serviceResult = callThroughCacheService("getMaterialByNameObjRefMode");
		Assert.assertNull(serviceResult.getAdditionalInformation());
		Assert.assertNotNull(serviceResult.getObjRefs());
		Assert.assertEquals(1, serviceResult.getObjRefs().size());
		Assert.assertNotNull(serviceResult.getObjRefs().get(0));
	}
}
