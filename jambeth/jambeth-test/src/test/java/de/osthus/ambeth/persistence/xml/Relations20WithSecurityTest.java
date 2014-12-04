package de.osthus.ambeth.persistence.xml;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import de.osthus.ambeth.cache.imc.IInMemoryConfig;
import de.osthus.ambeth.cache.imc.InMemoryCacheRetriever;
import de.osthus.ambeth.cache.interceptor.CacheInterceptor;
import de.osthus.ambeth.codec.Base64;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.xml.Relations20WithSecurityTest.Relations20WithSecurityTestModule;
import de.osthus.ambeth.persistence.xml.model.Employee;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.security.Password;
import de.osthus.ambeth.security.Passwords;
import de.osthus.ambeth.security.SecurityTest;
import de.osthus.ambeth.security.SecurityTest.SecurityTestModule;
import de.osthus.ambeth.security.TestAuthentication;
import de.osthus.ambeth.security.User;
import de.osthus.ambeth.security.config.SecurityConfigurationConstants;
import de.osthus.ambeth.security.config.SecurityServerConfigurationConstants;
import de.osthus.ambeth.security.model.IPassword;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestFrameworkModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.util.setup.IDatasetBuilderExtensionExtendable;

@SQLStructure("Relations_structure_with_security.sql")
@TestFrameworkModule(Relations20WithSecurityTestModule.class)
@TestPropertiesList({
		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/persistence/xml/orm.xml;de/osthus/ambeth/security/orm.xml"),
		@TestProperties(name = SecurityConfigurationConstants.SecurityActive, value = "true"),
		@TestProperties(name = SecurityServerConfigurationConstants.LoginPasswordAutoRehashActive, value = "false") })
@TestAuthentication(name = SecurityTest.userName1, password = SecurityTest.userPass1)
public class Relations20WithSecurityTest extends Relations20Test
{
	public static class Relations20WithSecurityTestModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerBean(SecurityTestModule.class);

			IBeanConfiguration dataSetBuilder = beanContextFactory.registerBean(Relations20WithSecurityTestDataSetBuilder.class);
			beanContextFactory.link(dataSetBuilder).to(IDatasetBuilderExtensionExtendable.class);
		}
	}

	@Autowired
	protected Connection conn;

	@Autowired
	protected IDatabase database;

	@Autowired(SecurityTest.IN_MEMORY_CACHE_RETRIEVER)
	protected InMemoryCacheRetriever inMemoryCacheRetriever;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		char[] salt = "abcdef=".toCharArray();
		char[] value = Base64.encodeBytes(
				Passwords.hashPassword(SecurityTest.userPass1.toCharArray(), Base64.decode(salt), Passwords.ALGORITHM, Passwords.ITERATION_COUNT,
						Passwords.KEY_SIZE)).toCharArray();

		IInMemoryConfig password10 = inMemoryCacheRetriever.add(Password.class, 10)//
				.primitive(IPassword.Salt, salt)//
				.primitive(IPassword.Algorithm, Passwords.ALGORITHM)//
				.primitive(IPassword.IterationCount, Passwords.ITERATION_COUNT)//
				.primitive(IPassword.KeySize, Passwords.KEY_SIZE)//
				.primitive(IPassword.Value, value);
		inMemoryCacheRetriever.add(User.class, 1)//
				.primitive(User.SID, SecurityTest.userName1.toLowerCase())//
				.addRelation(User.Password, password10);
	}

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
			List<Employee> actual = query.retrieve();// businessService.retrieve(names);
			assertEquals(1, actual.size());
		}
		finally
		{
			CacheInterceptor.pauseCache.remove();
		}
	}
}
