package com.koch.ambeth.persistence.xml;

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

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.koch.ambeth.audit.Signature;
import com.koch.ambeth.cache.interceptor.CacheInterceptor;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.ITechnicalEntityTypeExtendable;
import com.koch.ambeth.merge.config.MergeConfigurationConstants;
import com.koch.ambeth.merge.ioc.ChangeControllerModule;
import com.koch.ambeth.merge.util.IPrefetchHelper;
import com.koch.ambeth.merge.util.setup.IDatasetBuilderExtendable;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.xml.Relations20WithSecurityTest.Relations20WithSecurityTestModule;
import com.koch.ambeth.persistence.xml.model.Employee;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.security.SecurityTest;
import com.koch.ambeth.security.TestAuthentication;
import com.koch.ambeth.security.SecurityTest.SecurityTestFrameworkModule;
import com.koch.ambeth.security.model.ISignature;
import com.koch.ambeth.security.server.config.SecurityServerConfigurationConstants;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.SQLStructureList;
import com.koch.ambeth.testutil.TestFrameworkModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;

@SQLStructureList({ @SQLStructure("Relations_structure_with_security.sql"), @SQLStructure("com/koch/ambeth/audit/security-structure.sql") })
@TestFrameworkModule({ Relations20WithSecurityTestModule.class, ChangeControllerModule.class })
@TestPropertiesList({
		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/persistence/xml/orm20.xml;com/koch/ambeth/audit/security-orm.xml"),
		@TestProperties(name = MergeConfigurationConstants.SecurityActive, value = "true"),
		@TestProperties(name = SecurityServerConfigurationConstants.LoginPasswordAutoRehashActive, value = "false") })
@TestAuthentication(name = SecurityTest.userName1, password = SecurityTest.userPass1)
public class Relations20WithSecurityTest extends Relations20Test
{
	public static class Relations20WithSecurityTestModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerBean(SecurityTestFrameworkModule.class);

			beanContextFactory.link(ISignature.class).to(ITechnicalEntityTypeExtendable.class).with(Signature.class);

			IBeanConfiguration dataSetBuilder = beanContextFactory.registerBean(Relations20WithSecurityTestDataSetBuilder.class);
			beanContextFactory.link(dataSetBuilder).to(IDatasetBuilderExtendable.class);
		}
	}

	@Autowired
	protected Connection conn;

	@Autowired
	protected IDatabase database;

	@Override
	@Test
	public void testCascadedRetrieve() throws Throwable
	{
		List<String> names = Arrays.asList(new String[] { "Steve Smith", "Oscar Meyer" });

		CacheInterceptor.pauseCache.set(Boolean.TRUE);
		try
		{
			IQueryBuilder<Employee> qb = queryBuilderFactory.create(Employee.class);
			IQuery<Employee> query = qb.build(qb.isIn(qb.property("AllProjects.Employees.Name"), qb.value(names)));
			List<Employee> actual = query.retrieve();
			assertEquals(1, actual.size());

			beanContext.getService(IPrefetchHelper.class).createPrefetch()//
					.add(Employee.class, "AllProjects")//
					.add(Employee.class, "Boat")//
					.add(Employee.class, "PrimaryAddress")//
					.add(Employee.class, "Supervisor")//
					.add(Employee.class, "PrimaryProject")//
					.add(Employee.class, "SecondaryProject")//

					.build().prefetch(actual);
		}
		finally
		{
			CacheInterceptor.pauseCache.remove();
		}
	}
}
