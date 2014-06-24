package de.osthus.ambeth.audit;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.audit.AuditMethodCallTest.AuditMethodCallTestFrameworkModule;
import de.osthus.ambeth.audit.AuditMethodCallTest.AuditMethodCallTestModule;
import de.osthus.ambeth.config.AuditConfigurationConstants;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.AuditModule;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestFrameworkModule;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;

@TestFrameworkModule({ AuditModule.class, AuditMethodCallTestFrameworkModule.class })
@TestModule(AuditMethodCallTestModule.class)
@TestPropertiesList({ @TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/audit/AuditMethodCall_orm.xml"),
		@TestProperties(name = AuditConfigurationConstants.AuditMethodActive, value = "true") })
@SQLStructure("AuditMethodCall_structure.sql")
public class AuditMethodCallTest extends AbstractPersistenceTest
{
	public static class AuditMethodCallTestModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerAnonymousBean(TestAuditService.class).autowireable(ITestAuditService.class);
		}
	}

	public static class AuditMethodCallTestFrameworkModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerAnonymousBean(AuditEntryFactory.class).autowireable(IAuditEntryFactory.class);
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
		Assert.assertEquals("5", testAuditService.funnyMethod(new Integer(5)));
	}
}
