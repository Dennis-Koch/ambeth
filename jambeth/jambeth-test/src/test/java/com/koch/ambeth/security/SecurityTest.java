package com.koch.ambeth.security;

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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.audit.Password;
import com.koch.ambeth.audit.User;
import com.koch.ambeth.audit.UserIdentifierProvider;
import com.koch.ambeth.cache.IRootCache;
import com.koch.ambeth.cache.ISecondLevelCacheManager;
import com.koch.ambeth.cache.config.CacheConfigurationConstants;
import com.koch.ambeth.cache.imc.IInMemoryConfig;
import com.koch.ambeth.cache.imc.InMemoryCacheRetriever;
import com.koch.ambeth.cache.service.ICacheRetrieverExtendable;
import com.koch.ambeth.informationbus.persistence.setup.SQLData;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.ITechnicalEntityTypeExtendable;
import com.koch.ambeth.merge.cache.HandleContentDelegate;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.config.MergeConfigurationConstants;
import com.koch.ambeth.merge.security.ISecurityActivation;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.database.ResultingDatabaseCallback;
import com.koch.ambeth.persistence.xml.TestServicesModule;
import com.koch.ambeth.persistence.xml.model.Employee;
import com.koch.ambeth.persistence.xml.model.IBusinessService;
import com.koch.ambeth.persistence.xml.model.IEmployeeService;
import com.koch.ambeth.security.SecurityTest.SecurityTestFrameworkModule;
import com.koch.ambeth.security.SecurityTest.SecurityTestModule;
import com.koch.ambeth.security.model.IPassword;
import com.koch.ambeth.security.model.IUser;
import com.koch.ambeth.security.server.IUserIdentifierProvider;
import com.koch.ambeth.security.server.IUserResolver;
import com.koch.ambeth.security.server.Passwords;
import com.koch.ambeth.security.server.config.SecurityServerConfigurationConstants;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestFrameworkModule;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.util.codec.Base64;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

@SQLData("/com/koch/ambeth/security/Relations_data.sql")
@SQLStructure("/com/koch/ambeth/security/Relations_structure.sql")
@TestProperties(name = MergeConfigurationConstants.SecurityActive, value = "true")
@TestPropertiesList({
		@TestProperties(name = ServiceConfigurationConstants.mappingFile,
				value = "com/koch/ambeth/persistence/xml/orm.xml;com/koch/ambeth/security/orm.xml"),
		@TestProperties(name = CacheConfigurationConstants.ServiceResultCacheActive, value = "false"),
		@TestProperties(name = SecurityServerConfigurationConstants.LoginPasswordAutoRehashActive,
				value = "false")})
@TestModule(TestServicesModule.class)
@TestFrameworkModule({SecurityTestFrameworkModule.class, SecurityTestModule.class})
public class SecurityTest extends AbstractInformationBusWithPersistenceTest {
	public static final String IN_MEMORY_CACHE_RETRIEVER = "inMemoryCacheRetriever";

	public static class SecurityTestFrameworkModule implements IInitializingModule {
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
			beanContextFactory.registerBean(TestAuthorizationManager.class)
					.autowireable(IAuthorizationManager.class);
			beanContextFactory.registerBean(UserIdentifierProvider.class)
					.autowireable(IUserIdentifierProvider.class);
			beanContextFactory.registerBean(TestUserResolver.class).autowireable(IUserResolver.class);

			beanContextFactory.link(IUser.class).to(ITechnicalEntityTypeExtendable.class)
					.with(User.class);
			beanContextFactory.link(IPassword.class).to(ITechnicalEntityTypeExtendable.class)
					.with(Password.class);
		}
	}

	public static class SecurityTestModule implements IInitializingModule {
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
			IBeanConfiguration inMemoryCacheRetriever =
					beanContextFactory.registerBean(IN_MEMORY_CACHE_RETRIEVER, InMemoryCacheRetriever.class);
			beanContextFactory.link(inMemoryCacheRetriever).to(ICacheRetrieverExtendable.class)
					.with(User.class);
			beanContextFactory.link(inMemoryCacheRetriever).to(ICacheRetrieverExtendable.class)
					.with(Password.class);
		}
	}

	public static final String userName1 = "userName1", userPass1 = "abcd";

	@LogInstance
	private ILogger log;

	@Autowired
	protected IBusinessService businessService;

	@Autowired
	protected ICache cache;

	@Autowired
	protected ISecondLevelCacheManager secondLevelCacheManager;

	@Autowired
	protected IEmployeeService employeeService;

	@Autowired
	protected ISecurityActivation securityActivation;

	@Autowired(IN_MEMORY_CACHE_RETRIEVER)
	protected InMemoryCacheRetriever inMemoryCacheRetriever;

	@Override
	public void afterPropertiesSet() throws Throwable {
		super.afterPropertiesSet();

		char[] salt = "abcdef=".toCharArray();
		char[] value =
				Base64.encodeBytes(Passwords.hashPassword(userPass1.toCharArray(), Base64.decode(salt),
						Passwords.ALGORITHM, Passwords.ITERATION_COUNT, Passwords.KEY_SIZE)).toCharArray();

		IInMemoryConfig password10 = inMemoryCacheRetriever.add(Password.class, 10)
				.primitive(IPassword.Salt, salt).primitive(IPassword.Algorithm, Passwords.ALGORITHM)
				.primitive(IPassword.IterationCount, Passwords.ITERATION_COUNT)
				.primitive(IPassword.KeySize, Passwords.KEY_SIZE).primitive(IPassword.Value, value);
		inMemoryCacheRetriever.add(User.class, 1).primitive(User.SID, userName1)
				.addRelation(IUser.Password, password10);
	}

	@Test
	@TestAuthentication(name = userName1, password = userPass1)
	public void testListDelete() throws Throwable {
		final IBackgroundWorkerDelegate checkRootCache = new IBackgroundWorkerDelegate() {

			@Override
			public void invoke() throws Throwable {
				assertTrue(cache.getObjects(Employee.class, 1, 2, 3).isEmpty());

				// test privileged transactional root cache
				IRootCache rootCache = secondLevelCacheManager.selectSecondLevelCache();
				rootCache.getContent(new HandleContentDelegate() {
					@Override
					public void invoke(Class<?> entityType, byte idIndex, Object id, Object value) {
						if (Employee.class.isAssignableFrom(entityType)) {
							Assert.fail("RootCache must not contain an entry for a " + Employee.class.getName());
						}
					}
				});
			}
		};
		transaction.processAndCommit(new ResultingDatabaseCallback<Object>() {
			@Override
			public Object callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap)
					throws Throwable {
				Object result = securityActivation
						.executeWithoutSecurity(new IResultingBackgroundWorkerDelegate<Object>() {
							@Override
							public Object invoke() throws Throwable {
								List<Employee> employees = cache.getObjects(Employee.class, 1, 2, 3);
								assertFalse(employees.isEmpty());

								employeeService.delete(employees);

								// test privileged transactional root cache
								checkRootCache.invoke();
								return null;
							}
						});
				// test non-privileged transactional root cache
				checkRootCache.invoke();
				return result;
			}
		});
		// test committed root cache
		checkRootCache.invoke();
	}
}
