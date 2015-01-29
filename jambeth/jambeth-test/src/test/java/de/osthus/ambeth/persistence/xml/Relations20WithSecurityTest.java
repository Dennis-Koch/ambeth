package de.osthus.ambeth.persistence.xml;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import de.osthus.ambeth.audit.Password;
import de.osthus.ambeth.audit.Signature;
import de.osthus.ambeth.audit.User;
import de.osthus.ambeth.cache.interceptor.CacheInterceptor;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.merge.ITechnicalEntityTypeExtendable;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.xml.Relations20WithSecurityTest.Relations20WithSecurityTestModule;
import de.osthus.ambeth.persistence.xml.model.Employee;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.security.SecurityTest;
import de.osthus.ambeth.security.SecurityTest.SecurityTestFrameworkModule;
import de.osthus.ambeth.security.TestAuthentication;
import de.osthus.ambeth.security.config.SecurityConfigurationConstants;
import de.osthus.ambeth.security.config.SecurityServerConfigurationConstants;
import de.osthus.ambeth.security.model.IPassword;
import de.osthus.ambeth.security.model.ISignature;
import de.osthus.ambeth.security.model.IUser;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.SQLStructureList;
import de.osthus.ambeth.testutil.TestFrameworkModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.util.IPrefetchHelper;
import de.osthus.ambeth.util.setup.IDatasetBuilderExtendable;

@SQLStructureList({ @SQLStructure("Relations_structure_with_security.sql"), @SQLStructure("de/osthus/ambeth/audit/security-structure.sql") })
@TestFrameworkModule(Relations20WithSecurityTestModule.class)
@TestPropertiesList({
		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/persistence/xml/orm20.xml;de/osthus/ambeth/audit/security-orm.xml"),
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
			beanContextFactory.registerBean(SecurityTestFrameworkModule.class);

			beanContextFactory.link(ISignature.class).to(ITechnicalEntityTypeExtendable.class).with(Signature.class);
			beanContextFactory.link(IUser.class).to(ITechnicalEntityTypeExtendable.class).with(User.class);
			beanContextFactory.link(IPassword.class).to(ITechnicalEntityTypeExtendable.class).with(Password.class);

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
