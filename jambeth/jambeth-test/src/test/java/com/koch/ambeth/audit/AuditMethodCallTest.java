package com.koch.ambeth.audit;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.audit.AuditMethodCallTest.AuditMethodCallTestFrameworkModule;
import com.koch.ambeth.audit.AuditMethodCallTest.AuditMethodCallTestModule;
import com.koch.ambeth.audit.model.IAuditEntry;
import com.koch.ambeth.audit.model.IAuditedEntity;
import com.koch.ambeth.audit.model.IAuditedEntityPrimitiveProperty;
import com.koch.ambeth.audit.model.IAuditedEntityRef;
import com.koch.ambeth.audit.model.IAuditedEntityRelationProperty;
import com.koch.ambeth.audit.model.IAuditedEntityRelationPropertyItem;
import com.koch.ambeth.audit.model.IAuditedService;
import com.koch.ambeth.audit.server.IAuditEntryReader;
import com.koch.ambeth.audit.server.IAuditInfoController;
import com.koch.ambeth.audit.server.config.AuditConfigurationConstants;
import com.koch.ambeth.audit.server.exceptions.AuditReasonMissingException;
import com.koch.ambeth.audit.server.ioc.AuditModule;
import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructureList;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IMergeProcess;
import com.koch.ambeth.merge.ITechnicalEntityTypeExtendable;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.security.ISecurityScopeProvider;
import com.koch.ambeth.merge.util.IPrefetchConfig;
import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
import com.koch.ambeth.security.IAuthorization;
import com.koch.ambeth.security.ISecurityContextHolder;
import com.koch.ambeth.security.TestUserResolver;
import com.koch.ambeth.security.model.IPassword;
import com.koch.ambeth.security.model.ISignature;
import com.koch.ambeth.security.model.IUser;
import com.koch.ambeth.security.server.IPasswordUtil;
import com.koch.ambeth.security.server.IUserIdentifierProvider;
import com.koch.ambeth.security.server.IUserResolver;
import com.koch.ambeth.security.server.config.SecurityServerConfigurationConstants;
import com.koch.ambeth.security.server.ioc.SecurityServerModule;
import com.koch.ambeth.security.server.privilege.IEntityPermissionRule;
import com.koch.ambeth.security.server.privilege.evaluation.IEntityPermissionEvaluation;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.model.ISecurityScope;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestFrameworkModule;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.util.state.IStateRollback;
import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;

@TestFrameworkModule({AuditModule.class, AuditMethodCallTestFrameworkModule.class})
@TestModule(AuditMethodCallTestModule.class)
@TestPropertiesList({
		@TestProperties(name = ServiceConfigurationConstants.mappingFile,
				value = "AuditMethodCall_orm.xml;security-orm.xml"),
		@TestProperties(name = AuditConfigurationConstants.AuditActive, value = "true"),
		@TestProperties(name = "ambeth.log.level", value = "DEBUG"),
		@TestProperties(name = SecurityServerConfigurationConstants.SignatureActive, value = "true"),
		@TestProperties(name = AuditConfigurationConstants.VerifyEntitiesOnLoad,
				value = "VERIFY_SYNC")})
@SQLStructureList({@SQLStructure("security-structure.sql"), //
		@SQLStructure("audit-structure.sql")})
public class AuditMethodCallTest extends AbstractInformationBusWithPersistenceTest {
	public static class ABC implements IEntityPermissionRule<User> {
		@Autowired
		protected ITestAuditService service;

		@Override
		public void buildPrefetchConfig(Class<? extends User> entityType,
				IPrefetchConfig prefetchConfig) {
		}

		@Override
		public void evaluatePermissionOnInstance(IObjRef objRef, User entity,
				IAuthorization currentUser, ISecurityScope[] securityScopes,
				IEntityPermissionEvaluation pe) {
			service.auditedServiceCall(new Integer(6));
		}
	}

	public static class AuditMethodCallTestModule implements IInitializingModule {
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
			beanContextFactory.registerBean(TestAuditService.class).autowireable(ITestAuditService.class);

			IBeanConfiguration bc = beanContextFactory.registerBean(ABC.class);
			SecurityServerModule.linkPermissionRule(beanContextFactory, bc, User.class);
		}
	}

	public static class AuditMethodCallTestFrameworkModule implements IInitializingModule {
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
			beanContextFactory.registerBean(UserIdentifierProvider.class)
					.autowireable(IUserIdentifierProvider.class);
			beanContextFactory.registerBean(TestUserResolver.class).autowireable(IUserResolver.class);

			beanContextFactory.link(IUser.class).to(ITechnicalEntityTypeExtendable.class)
					.with(User.class);
			beanContextFactory.link(IPassword.class).to(ITechnicalEntityTypeExtendable.class)
					.with(Password.class);
			beanContextFactory.link(ISignature.class).to(ITechnicalEntityTypeExtendable.class)
					.with(Signature.class);
			beanContextFactory.link(IAuditEntry.class).to(ITechnicalEntityTypeExtendable.class)
					.with(AuditEntry.class);
			beanContextFactory.link(IAuditedEntity.class).to(ITechnicalEntityTypeExtendable.class)
					.with(AuditedEntity.class);
			beanContextFactory.link(IAuditedService.class).to(ITechnicalEntityTypeExtendable.class)
					.with(AuditedService.class);
			beanContextFactory.link(IAuditedEntityRef.class).to(ITechnicalEntityTypeExtendable.class)
					.with(AuditedEntityRef.class);
			beanContextFactory.link(IAuditedEntityPrimitiveProperty.class)
					.to(ITechnicalEntityTypeExtendable.class).with(AuditedEntityPrimitiveProperty.class);
			beanContextFactory.link(IAuditedEntityRelationProperty.class)
					.to(ITechnicalEntityTypeExtendable.class).with(AuditedEntityRelationProperty.class);
			beanContextFactory.link(IAuditedEntityRelationPropertyItem.class)
					.to(ITechnicalEntityTypeExtendable.class).with(AuditedEntityRelationPropertyItem.class);
		}
	}

	@LogInstance
	private ILogger log;

	@Autowired
	protected IAuditInfoController auditController;

	@Autowired
	protected IAuditEntryReader auditEntryReader;

	@Autowired
	protected IAuditEntryVerifier auditEntryVerifier;

	@Autowired
	protected IEventDispatcher eventDispatcher;

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
	@TestProperties(name = SecurityServerConfigurationConstants.SignatureActive, value = "false")
	public void myTest() {
		Assert.assertEquals("5", testAuditService.auditedServiceCall(new Integer(5)));
	}

	// @Test
	// @TestProperties(name = SecurityServerConfigurationConstants.SignatureActive, value = "false")
	// public void myTest2() {
	// auditController.pushAuditReason("junit test");
	// try {
	// char[] passwordOfUser = "abc".toCharArray();
	// User[] users = new User[2];
	// for (int a = users.length; a-- > 0;) {
	// User user = entityFactory.createEntity(User.class);
	// user.setName("MyName" + a);
	// user.setSID("mySid" + a);
	//
	// passwordUtil.assignNewPassword(passwordOfUser, user, null);
	//
	// IStateRollback revert = auditController.pushAuthorizedUser(user, passwordOfUser, true);
	// try {
	// mergeProcess.process(user, null, null, null);
	// }
	// finally {
	// revert.rollback();
	// }
	// users[a] = user;
	// }
	// IStateRollback revert = auditController.pushAuthorizedUser(users[0], passwordOfUser, true);
	// try {
	// for (int a = users.length; a-- > 0;) {
	// users[a].setName(users[a].getName() + "x");
	// }
	// mergeProcess.process(users, null, null, null);
	// }
	// finally {
	// revert.rollback();
	// }
	// revert = auditController.pushAuthorizedUser(users[1], passwordOfUser, true);
	// try {
	// for (int a = users.length; a-- > 0;) {
	// users[a].setName(users[a].getName() + "x");
	// }
	// mergeProcess.process(users, null, null, null);
	// }
	// finally {
	// revert.rollback();
	// }
	// }
	// finally {
	// auditController.popAuditReason();
	// }
	// final String startTime = "startTime", endTime = "endTime";
	// final int fieldValueIndex, entityTypeIndex, entityIdIndex;
	// final IQuery<IAuditedEntityPrimitiveProperty> query;
	// {
	// IQueryBuilder<IAuditedEntityPrimitiveProperty> qb = queryBuilderFactory
	// .create(IAuditedEntityPrimitiveProperty.class);
	// IOperand name = qb.property(IAuditedEntityPrimitiveProperty.Name);
	//
	// IOperand entityType = qb.property(IAuditedEntityPrimitiveProperty.Entity + "."
	// + IAuditedEntity.Ref + "." + IAuditedEntityRef.EntityType);
	// IOperand entityId = qb.property(IAuditedEntityPrimitiveProperty.Entity + "."
	// + IAuditedEntity.Ref + "." + IAuditedEntityRef.EntityId);
	// IOperand changeType = qb
	// .property(IAuditedEntityPrimitiveProperty.Entity + "." + IAuditedEntity.ChangeType);
	// IOperand timestamp = qb.property(IAuditedEntityPrimitiveProperty.Entity + "."
	// + IAuditedEntity.Entry + "." + IAuditEntry.Timestamp);
	//
	// fieldValueIndex = qb
	// .select(qb.function("to_char", qb.property(IAuditedEntityPrimitiveProperty.NewValue)));
	// entityTypeIndex = qb.select(entityType);
	// entityIdIndex = qb.select(entityId);
	//
	// qb.orderBy(timestamp, OrderByType.DESC);
	//
	// query = qb.build(qb.and(//
	// qb.let(changeType).isEqualTo(qb.valueName(IAuditedEntity.ChangeType)), //
	// qb.let(entityType).isIn(qb.valueName(IAuditedEntityRef.EntityType)), //
	// qb.let(timestamp).isGreaterThan(qb.valueName(startTime)), //
	// qb.let(timestamp).isLessThanOrEqualTo(qb.valueName(endTime)), //
	// qb.let(name).isEqualTo(qb.valueName(IAuditedEntityPrimitiveProperty.Name))//
	// ));
	// }
	// transaction.runInTransaction(new IBackgroundWorkerDelegate() {
	// @Override
	// public void invoke() throws Exception {
	// long start = System.currentTimeMillis() - 10000;
	// long end = System.currentTimeMillis();
	// Tuple2KeyHashMap<String, String, String> map = new Tuple2KeyHashMap<>();
	// IDataCursor cursor = query.param(IAuditedEntityPrimitiveProperty.Name, "Name")//
	// .param(IAuditedEntity.ChangeType, AuditedEntityChangeType.UPDATE)//
	// .param(IAuditedEntityRef.EntityType, Arrays.asList(User.class.getName()))//
	// .param(startTime, start)//
	// .param(endTime, end)//
	// .retrieveAsData();
	// try {
	// for (IDataItem item : cursor) {
	// String lastValue = conversionHelper.convertValueToType(String.class,
	// item.getValue(fieldValueIndex));
	// String entityTypeName = conversionHelper.convertValueToType(String.class,
	// item.getValue(entityTypeIndex));
	// String entityId = conversionHelper.convertValueToType(String.class,
	// item.getValue(entityIdIndex));
	//
	// map.putIfNotExists(entityTypeName, entityId, lastValue);
	// }
	// }
	// finally {
	// cursor.dispose();
	// }
	// }
	// });
	// }

	@Test
	public void auditedEntity() {
		char[] passwordOfUser = "abc".toCharArray();
		User user = entityFactory.createEntity(User.class);
		user.setName("MyName");
		user.setSID("mySid");

		auditController.pushAuditReason("junit test");

		passwordUtil.assignNewPassword(passwordOfUser, user, null);

		IStateRollback revert = auditController.pushAuthorizedUser(user, passwordOfUser, true);
		try {
			mergeProcess.process(user, null, null, null);
		}
		finally {
			revert.rollback();
		}
		auditController.popAuditReason();
		((IThreadLocalCleanupBean) auditController).cleanupThreadLocal();
		Assert.assertTrue(user.getId() > 0);
	}

	// @Test
	// public void lobLeakTestOracle() throws Exception {
	// char[] passwordOfUser = "abc".toCharArray();
	// User user = entityFactory.createEntity(User.class);
	// user.setName("MyName");
	// user.setSID("mySid");
	//
	// Statement stm = null;
	// ResultSet rs = null;
	// Connection connection = connectionFactory.create();
	// try {
	// stm = connection.createStatement();
	// rs = stm.executeQuery("SELECT count(*) FROM V$TEMPSEG_USAGE");
	// rs.next();
	// Assert.assertEquals(0, rs.getLong(1));
	//
	// auditController.pushAuditReason("junit test");
	//
	// passwordUtil.assignNewPassword(passwordOfUser, user, null);
	//
	// IStateRollback revert = auditController.pushAuthorizedUser(user, passwordOfUser, true);
	// try {
	// mergeProcess.process(user, null, null, null);
	// }
	// finally {
	// revert.rollback();
	// }
	// auditController.popAuditReason();
	// ((IThreadLocalCleanupBean) auditController).cleanupThreadLocal();
	//
	// rs.close();
	// rs = stm.executeQuery("SELECT count(*) FROM V$TEMPSEG_USAGE");
	// rs.next();
	// Assert.assertEquals("There seems to be a temporary segment leak", 0, rs.getLong(1));
	// }
	// finally {
	// JdbcUtil.close(stm, rs);
	// JdbcUtil.close(connection);
	// }
	// }

	@Test(expected = AuditReasonMissingException.class)
	@TestProperties(name = SecurityServerConfigurationConstants.SignatureActive, value = "false")
	public void auditedEntity_NoReasonThrowsException() {
		char[] passwordOfUser = "abc".toCharArray();
		User user = entityFactory.createEntity(User.class);
		user.setName("MyName");
		user.setSID("mySid");

		passwordUtil.assignNewPassword(passwordOfUser, user, null);

		mergeProcess.process(user, null, null, null);
	}

	@Test
	public void verify() {
		final char[] passwordOfUser = "abc".toCharArray();
		final User user = entityFactory.createEntity(User.class);
		user.setName("MyName");
		user.setSID("mySid");

		passwordUtil.assignNewPassword(passwordOfUser, user, null);

		IPassword password = user.getPassword();

		auditController.pushAuditReason("junit test");
		try {
			IStateRollback revert = auditController.pushAuthorizedUser(user, passwordOfUser, true);
			try {
				transaction.runInTransaction(new IBackgroundWorkerDelegate() {
					@Override
					public void invoke() throws Exception {
						mergeProcess.process(user, null, null, null);
					}
				});
			}
			finally {
				revert.rollback();
			}
		}
		finally {
			auditController.popAuditReason();
		}
		{
			List<IAuditedEntity> auditedEntitiesOfUser = auditEntryReader
					.getAllAuditedEntitiesOfEntity(user);

			Assert.assertEquals(1, auditedEntitiesOfUser.size());
			boolean[] verifiedAuditedEntities = auditEntryVerifier
					.verifyAuditedEntities(auditedEntitiesOfUser);
			Assert.assertEquals(auditedEntitiesOfUser.size(), verifiedAuditedEntities.length);
			for (boolean verifySuccessful : verifiedAuditedEntities) {
				Assert.assertTrue(verifySuccessful);
			}
		}
		{
			List<IAuditedEntity> auditedEntitiesOfPassword = auditEntryReader
					.getAllAuditedEntitiesOfEntity(password);

			Assert.assertEquals(1, auditedEntitiesOfPassword.size());
			boolean[] verifiedAuditedEntities = auditEntryVerifier
					.verifyAuditedEntities(auditedEntitiesOfPassword);
			Assert.assertEquals(auditedEntitiesOfPassword.size(), verifiedAuditedEntities.length);
			for (boolean verifySuccessful : verifiedAuditedEntities) {
				Assert.assertTrue(verifySuccessful);
			}
		}
		{
			List<IAuditEntry> auditEntriesOfUser = auditEntryReader.getAllAuditEntriesOfEntity(user);

			Assert.assertEquals(1, auditEntriesOfUser.size());
			boolean[] verifiedAuditEntries = auditEntryVerifier.verifyAuditEntries(auditEntriesOfUser);
			Assert.assertEquals(auditEntriesOfUser.size(), verifiedAuditEntries.length);
			for (boolean verifySuccessful : verifiedAuditEntries) {
				Assert.assertTrue(verifySuccessful);
			}
		}
		eventDispatcher.dispatchEvent(ClearAllCachesEvent.getInstance());

		User reloadedUser = beanContext.getService(ICache.class).getObject(User.class, user.getId());
	}

	@Test
	@TestPropertiesList({
			@TestProperties(name = AuditConfigurationConstants.VerifyEntitiesOnLoad,
					value = "VERIFY_ASYNC"),
			@TestProperties(name = PersistenceConfigurationConstants.DatabasePoolMaxUsed, value = "2"),
			@TestProperties(name = PersistenceConfigurationConstants.DatabasePoolMaxUnused, value = "2")})
	public void verifyWithConcurrentUpdate() {
		final char[] passwordOfUser = "abc".toCharArray();
		final User user = entityFactory.createEntity(User.class);
		user.setName("MyName");
		user.setSID("mySid");

		passwordUtil.assignNewPassword(passwordOfUser, user, null);

		IPassword password = user.getPassword();

		auditController.pushAuditReason("junit test");
		try {
			IStateRollback revert = auditController.pushAuthorizedUser(user, passwordOfUser, true);
			try {
				transaction.runInTransaction(new IBackgroundWorkerDelegate() {
					@Override
					public void invoke() throws Exception {
						mergeProcess.process(user, null, null, null);
					}
				});
				transaction.runInTransaction(new IBackgroundWorkerDelegate() {
					@Override
					public void invoke() throws Exception {
						user.setName(user.getName() + 1);
						mergeProcess.process(user, null, null, null);
					}
				});
			}
			finally {
				revert.rollback();
			}
		}
		finally {
			auditController.popAuditReason();
		}
		{
			List<IAuditedEntity> auditedEntitiesOfUser = auditEntryReader
					.getAllAuditedEntitiesOfEntity(user);

			Assert.assertEquals(2, auditedEntitiesOfUser.size());
			boolean[] verifiedAuditedEntities = auditEntryVerifier
					.verifyAuditedEntities(auditedEntitiesOfUser);
			Assert.assertEquals(auditedEntitiesOfUser.size(), verifiedAuditedEntities.length);
			for (boolean verifySuccessful : verifiedAuditedEntities) {
				Assert.assertTrue(verifySuccessful);
			}
		}
		{
			List<IAuditedEntity> auditedEntitiesOfPassword = auditEntryReader
					.getAllAuditedEntitiesOfEntity(password);

			Assert.assertEquals(1, auditedEntitiesOfPassword.size());
			boolean[] verifiedAuditedEntities = auditEntryVerifier
					.verifyAuditedEntities(auditedEntitiesOfPassword);
			Assert.assertEquals(auditedEntitiesOfPassword.size(), verifiedAuditedEntities.length);
			for (boolean verifySuccessful : verifiedAuditedEntities) {
				Assert.assertTrue(verifySuccessful);
			}
		}
		{
			List<IAuditEntry> auditEntriesOfUser = auditEntryReader.getAllAuditEntriesOfEntity(user);

			Assert.assertEquals(2, auditEntriesOfUser.size());
			boolean[] verifiedAuditEntries = auditEntryVerifier.verifyAuditEntries(auditEntriesOfUser);
			Assert.assertEquals(auditEntriesOfUser.size(), verifiedAuditEntries.length);
			for (boolean verifySuccessful : verifiedAuditEntries) {
				Assert.assertTrue(verifySuccessful);
			}
		}
		eventDispatcher.dispatchEvent(ClearAllCachesEvent.getInstance());

		User reloadedUser = beanContext.getService(ICache.class).getObject(User.class, user.getId());
	}

	@Test
	public void testNotAuditedServiceCall() {
		Assert.assertEquals("5", testAuditService.notAuditedServiceCall(new Integer(5)));
	}

	@Test
	public void testAuditedAnnotatedServiceCall_NoAudit() {
		Assert.assertEquals("5", testAuditService.auditedAnnotatedServiceCall_NoAudit(new Integer(5)));
	}

	@Test
	@TestProperties(name = SecurityServerConfigurationConstants.SignatureActive, value = "false")
	public void testAuditedServiceCallWithAuditedArgument() {
		Assert.assertEquals("5", testAuditService.auditedServiceCallWithAuditedArgument(new Integer(5),
				"secret_not_audited"));
	}
}
