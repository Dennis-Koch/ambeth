package de.osthus.ambeth.audit;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.audit.AuditMethodCallTest.AuditMethodCallTestFrameworkModule;
import de.osthus.ambeth.audit.AuditMethodCallTest.AuditMethodCallTestModule;
import de.osthus.ambeth.config.AuditConfigurationConstants;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.AuditModule;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.SecurityServerModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.privilege.IEntityPermissionRule;
import de.osthus.ambeth.privilege.evaluation.IEntityPermissionEvaluation;
import de.osthus.ambeth.security.IAuthorization;
import de.osthus.ambeth.security.User;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestFrameworkModule;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.util.IPrefetchConfig;

@TestFrameworkModule({ AuditModule.class, AuditMethodCallTestFrameworkModule.class })
@TestModule(AuditMethodCallTestModule.class)
@TestPropertiesList({ @TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/audit/AuditMethodCall_orm.xml"),
		@TestProperties(name = AuditConfigurationConstants.AuditActive, value = "true") })
@SQLStructure("AuditMethodCall_structure.sql")
public class AuditMethodCallTest extends AbstractPersistenceTest
{
	public static class ABC implements IEntityPermissionRule<User>
	{
		@Autowired
		protected ITestAuditService service;

		@Override
		public void buildPrefetchConfig(Class<? extends User> entityType, IPrefetchConfig prefetchConfig)
		{
		}

		@Override
		public void evaluatePermissionOnInstance(IObjRef objRef, User entity, IAuthorization currentUser, ISecurityScope[] securityScopes,
				IEntityPermissionEvaluation pe)
		{
			service.auditedServiceCall(new Integer(6));
		}
	}

	public static class AuditMethodCallTestModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerAnonymousBean(TestAuditService.class).autowireable(ITestAuditService.class);

			IBeanConfiguration bc = beanContextFactory.registerAnonymousBean(ABC.class);
			SecurityServerModule.linkPermissionRule(beanContextFactory, bc, User.class);
		}
	}

	public static class AuditMethodCallTestFrameworkModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
		}
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ITestAuditService testAuditService;

	@Test
	public void myTest()
	{
		Assert.assertEquals("5", testAuditService.auditedServiceCall(new Integer(5)));
	}
}
