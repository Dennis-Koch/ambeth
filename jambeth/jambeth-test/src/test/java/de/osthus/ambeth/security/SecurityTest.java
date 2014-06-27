package de.osthus.ambeth.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.cache.HandleContentDelegate;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.IRootCache;
import de.osthus.ambeth.cache.ISecondLevelCacheManager;
import de.osthus.ambeth.cache.config.CacheConfigurationConstants;
import de.osthus.ambeth.cache.imc.IInMemoryConfig;
import de.osthus.ambeth.cache.imc.InMemoryCacheRetriever;
import de.osthus.ambeth.codec.Base64;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.database.ITransaction;
import de.osthus.ambeth.database.ResultingDatabaseCallback;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.xml.TestServicesModule;
import de.osthus.ambeth.persistence.xml.model.Employee;
import de.osthus.ambeth.persistence.xml.model.IBusinessService;
import de.osthus.ambeth.persistence.xml.model.IEmployeeService;
import de.osthus.ambeth.security.SecurityTest.SecurityTestModule;
import de.osthus.ambeth.security.config.SecurityConfigurationConstants;
import de.osthus.ambeth.security.model.IPassword;
import de.osthus.ambeth.service.ICacheRetrieverExtendable;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestFrameworkModule;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;

@SQLData("/de/osthus/ambeth/persistence/xml/Relations_data.sql")
@SQLStructure("/de/osthus/ambeth/persistence/xml/Relations_structure.sql")
@TestProperties(name = SecurityConfigurationConstants.SecurityActive, value = "true")
@TestPropertiesList({
		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/persistence/xml/orm.xml;de/osthus/ambeth/security/orm.xml"),
		@TestProperties(name = CacheConfigurationConstants.ServiceResultCacheActive, value = "false") })
@TestModule(TestServicesModule.class)
@TestFrameworkModule(SecurityTestModule.class)
public class SecurityTest extends AbstractPersistenceTest
{
	public static final String IN_MEMORY_CACHE_RETRIEVER = "inMemoryCacheRetriever";

	public static class SecurityTestModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerAnonymousBean(TestAuthorizationManager.class).autowireable(IAuthorizationManager.class);
			beanContextFactory.registerAnonymousBean(TestUserResolver.class).autowireable(IUserResolver.class);

			IBeanConfiguration inMemoryCacheRetriever = beanContextFactory.registerBean(IN_MEMORY_CACHE_RETRIEVER, InMemoryCacheRetriever.class);
			beanContextFactory.link(inMemoryCacheRetriever).to(ICacheRetrieverExtendable.class).with(User.class);
			beanContextFactory.link(inMemoryCacheRetriever).to(ICacheRetrieverExtendable.class).with(Password.class);
		}
	}

	public static final String userName1 = "userName1", userPass1 = "abcd";

	@SuppressWarnings("unused")
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

	@Autowired
	protected ITransaction transaction;

	@Autowired(IN_MEMORY_CACHE_RETRIEVER)
	protected InMemoryCacheRetriever inMemoryCacheRetriever;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		char[] salt = "abcdef=".toCharArray();
		char[] value = Base64.encodeBytes(
				Passwords.hashPassword(userPass1.toCharArray(), Base64.decode(salt), Passwords.ALGORITHM, Passwords.ITERATION_COUNT, Passwords.KEY_SIZE))
				.toCharArray();

		IInMemoryConfig password10 = inMemoryCacheRetriever.add(Password.class, 10).primitive(IPassword.Salt, salt)
				.primitive(IPassword.Algorithm, Passwords.ALGORITHM).primitive(IPassword.IterationCount, Passwords.ITERATION_COUNT)
				.primitive(IPassword.KeySize, Passwords.KEY_SIZE).primitive(IPassword.Value, value);
		inMemoryCacheRetriever.add(User.class, 1).primitive(User.SID, userName1.toLowerCase()).addRelation(User.Password, password10);
	}

	@Test
	@TestAuthentication(name = userName1, password = userPass1)
	public void testListDelete() throws Throwable
	{
		final IBackgroundWorkerDelegate checkRootCache = new IBackgroundWorkerDelegate()
		{

			@Override
			public void invoke() throws Throwable
			{
				assertTrue(cache.getObjects(Employee.class, 1, 2, 3).isEmpty());

				// test privileged transactional root cache
				IRootCache rootCache = secondLevelCacheManager.selectSecondLevelCache();
				rootCache.getContent(new HandleContentDelegate()
				{
					@Override
					public void invoke(Class<?> entityType, byte idIndex, Object id, Object value)
					{
						if (Employee.class.isAssignableFrom(entityType))
						{
							Assert.fail("RootCache must not contain an entry for a " + Employee.class.getName());
						}
					}
				});
			}
		};
		transaction.processAndCommit(new ResultingDatabaseCallback<Object>()
		{
			@Override
			public Object callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) throws Throwable
			{
				Object result = securityActivation.executeWithoutSecurity(new IResultingBackgroundWorkerDelegate<Object>()
				{
					@Override
					public Object invoke() throws Throwable
					{
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
