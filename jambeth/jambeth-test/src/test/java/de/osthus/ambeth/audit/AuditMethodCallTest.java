package de.osthus.ambeth.audit;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.IAuditEntryVerifier;
import de.osthus.ambeth.audit.AuditMethodCallTest.AuditMethodCallTestFrameworkModule;
import de.osthus.ambeth.audit.AuditMethodCallTest.AuditMethodCallTestModule;
import de.osthus.ambeth.audit.model.IAuditEntry;
import de.osthus.ambeth.audit.model.IAuditedEntity;
import de.osthus.ambeth.audit.model.IAuditedEntityPrimitiveProperty;
import de.osthus.ambeth.audit.model.IAuditedEntityRef;
import de.osthus.ambeth.audit.model.IAuditedEntityRelationProperty;
import de.osthus.ambeth.audit.model.IAuditedEntityRelationPropertyItem;
import de.osthus.ambeth.audit.model.IAuditedService;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.config.AuditConfigurationConstants;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.exceptions.AuditReasonMissingException;
import de.osthus.ambeth.ioc.AuditModule;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.SecurityServerModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IMergeProcess;
import de.osthus.ambeth.merge.ITechnicalEntityTypeExtendable;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.privilege.IEntityPermissionRule;
import de.osthus.ambeth.privilege.evaluation.IEntityPermissionEvaluation;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.query.OrderByType;
import de.osthus.ambeth.security.CallPermission;
import de.osthus.ambeth.security.DefaultAuthentication;
import de.osthus.ambeth.security.DefaultAuthorization;
import de.osthus.ambeth.security.IAuthorization;
import de.osthus.ambeth.security.IPasswordUtil;
import de.osthus.ambeth.security.ISecurityContext;
import de.osthus.ambeth.security.ISecurityContextHolder;
import de.osthus.ambeth.security.ISecurityScopeProvider;
import de.osthus.ambeth.security.IUserIdentifierProvider;
import de.osthus.ambeth.security.IUserResolver;
import de.osthus.ambeth.security.PasswordType;
import de.osthus.ambeth.security.TestUserResolver;
import de.osthus.ambeth.security.model.IPassword;
import de.osthus.ambeth.security.model.ISignature;
import de.osthus.ambeth.security.model.IUser;
import de.osthus.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.SQLStructureList;
import de.osthus.ambeth.testutil.TestFrameworkModule;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.util.IPrefetchConfig;

@TestFrameworkModule({ AuditModule.class, AuditMethodCallTestFrameworkModule.class })
@TestModule(AuditMethodCallTestModule.class)
@TestPropertiesList({ @TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "AuditMethodCall_orm.xml;security-orm.xml"),
		@TestProperties(name = AuditConfigurationConstants.AuditActive, value = "true") })
@SQLStructureList({ @SQLStructure("security-structure.sql"),//
		@SQLStructure("audit-structure.sql") })
public class AuditMethodCallTest extends AbstractInformationBusWithPersistenceTest
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
			beanContextFactory.registerBean(TestAuditService.class).autowireable(ITestAuditService.class);

			IBeanConfiguration bc = beanContextFactory.registerBean(ABC.class);
			SecurityServerModule.linkPermissionRule(beanContextFactory, bc, User.class);
		}
	}

	public static class AuditMethodCallTestFrameworkModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerBean(UserIdentifierProvider.class).autowireable(IUserIdentifierProvider.class);
			beanContextFactory.registerBean(TestUserResolver.class).autowireable(IUserResolver.class);

			beanContextFactory.link(IAuditedEntity.class).to(ITechnicalEntityTypeExtendable.class).with(AuditedEntity.class);
			beanContextFactory.link(IAuditedEntityRef.class).to(ITechnicalEntityTypeExtendable.class).with(AuditedEntityRef.class);
			beanContextFactory.link(IAuditedEntityPrimitiveProperty.class).to(ITechnicalEntityTypeExtendable.class).with(AuditedEntityPrimitiveProperty.class);
			beanContextFactory.link(IAuditedEntityRelationProperty.class).to(ITechnicalEntityTypeExtendable.class).with(AuditedEntityRelationProperty.class);
			beanContextFactory.link(IAuditedEntityRelationPropertyItem.class).to(ITechnicalEntityTypeExtendable.class)
					.with(AuditedEntityRelationPropertyItem.class);
			beanContextFactory.link(IAuditedService.class).to(ITechnicalEntityTypeExtendable.class).with(AuditedService.class);
			beanContextFactory.link(IAuditEntry.class).to(ITechnicalEntityTypeExtendable.class).with(AuditEntry.class);
			beanContextFactory.link(ISignature.class).to(ITechnicalEntityTypeExtendable.class).with(Signature.class);
			beanContextFactory.link(IUser.class).to(ITechnicalEntityTypeExtendable.class).with(User.class);
			beanContextFactory.link(IPassword.class).to(ITechnicalEntityTypeExtendable.class).with(Password.class);
		}
	}

	@LogInstance
	private ILogger log;

	@Autowired
	protected IAuditInfoController auditController;

	@Autowired
	protected IAuditEntryVerifier auditEntryVerifier;

	@Autowired
	protected IMergeProcess mergeProcess;

	@Autowired
	protected IPasswordUtil passwordUtil;

	@Autowired
	protected ISecurityContextHolder securityContextHolder;

	@Autowired
	protected ISecurityScopeProvider securityScopeProvider;

	@Autowired
	protected ITestAuditService testAuditService;

	@Test
	public void myTest()
	{
		Assert.assertEquals("5", testAuditService.auditedServiceCall(new Integer(5)));
	}

	@Test
	public void auditedEntity()
	{
		char[] passwordOfUser = "abc".toCharArray();
		User user = entityFactory.createEntity(User.class);
		user.setName("MyName");
		user.setSID("mySid");

		auditController.pushAuditReason("junit test");

		Password password = entityFactory.createEntity(Password.class);
		passwordUtil.assignNewPassword(passwordOfUser, password, user);
		user.setPassword(password);

		mergeProcess.process(user, null, null, null);
		auditController.popAuditReason();
		((IThreadLocalCleanupBean) auditController).cleanupThreadLocal();
		Assert.assertTrue(user.getId() > 0);
	}

	@Test(expected = AuditReasonMissingException.class)
	public void auditedEntity_NoReasonThrowsException()
	{
		char[] passwordOfUser = "abc".toCharArray();
		User user = entityFactory.createEntity(User.class);
		user.setName("MyName");
		user.setSID("mySid");

		Password password = entityFactory.createEntity(Password.class);
		passwordUtil.assignNewPassword(passwordOfUser, password, user);
		user.setPassword(password);

		mergeProcess.process(user, null, null, null);
	}

	@Test
	public void verify()
	{
		char[] passwordOfUser = "abc".toCharArray();
		User user = entityFactory.createEntity(User.class);
		user.setName("MyName");
		user.setSID("mySid");
		user.setSignature(entityFactory.createEntity(ISignature.class));

		Password password = entityFactory.createEntity(Password.class);
		passwordUtil.assignNewPassword(passwordOfUser, password, user);
		user.setPassword(password);

		auditController.pushAuditReason("junit test");
		try
		{
			mergeProcess.process(user, null, null, null);

			// ISecurityScope[] scopes = new ISecurityScope[] { DefaultSecurityScope.INSTANCE };
			ISecurityContext context = securityContextHolder.getCreateContext();
			context.setAuthentication(new DefaultAuthentication(user.getSID(), passwordOfUser, PasswordType.PLAIN));
			IAuthorization authorization = new DefaultAuthorization(user.getSID(), new ISecurityScope[0], CallPermission.UNDEFINED);
			context.setAuthorization(authorization);
			// securityScopeProvider.setSecurityScopes(scopes);

			user.setName(user.getName() + "2");
			mergeProcess.process(user, null, null, null);
		}
		finally
		{
			auditController.popAuditReason();
		}

		String refPath = IAuditEntry.Entities + "." + IAuditedEntity.Ref;
		IQueryBuilder<IAuditEntry> qb = queryBuilderFactory.create(IAuditEntry.class);
		qb.orderBy(qb.property(IAuditEntry.Timestamp), OrderByType.ASC);

		IQuery<IAuditEntry> query = qb.build(qb.and(qb.isEqualTo(qb.property(refPath + "." + IAuditedEntityRef.EntityType), qb.value(User.class)),
				qb.isEqualTo(qb.property(refPath + "." + IAuditedEntityRef.EntityId), qb.value(user.getId()))));

		IList<IAuditEntry> auditEntriesOfUser = query.retrieve();
		Assert.assertEquals(2, auditEntriesOfUser.size());
		Assert.assertTrue(auditEntryVerifier.verifyAuditEntries(auditEntriesOfUser.subList(1, auditEntriesOfUser.size())));
	}

	@Test
	public void testNotAuditedServiceCall()
	{
		Assert.assertEquals("5", testAuditService.notAuditedServiceCall(new Integer(5)));
	}

	@Test
	public void testAuditedAnnotatedServiceCall_NoAudit()
	{
		Assert.assertEquals("5", testAuditService.auditedAnnotatedServiceCall_NoAudit(new Integer(5)));
	}

	@Test
	public void testAuditedServiceCallWithAuditedArgument()
	{
		Assert.assertEquals("5", testAuditService.auditedServiceCallWithAuditedArgument(new Integer(5), "secret_not_audited"));
	}
}
