package com.koch.ambeth.audit;

import com.koch.ambeth.audit.AuditMethodCallTest.AuditMethodCallTestFrameworkModule;
import com.koch.ambeth.audit.AuditTamperTest.AuditTamperTestModule;
import com.koch.ambeth.audit.model.AuditedEntityChangeType;
import com.koch.ambeth.audit.model.AuditedEntityPropertyItemChangeType;
import com.koch.ambeth.audit.model.IAuditEntry;
import com.koch.ambeth.audit.model.IAuditedEntity;
import com.koch.ambeth.audit.model.IAuditedEntityPrimitiveProperty;
import com.koch.ambeth.audit.model.IAuditedEntityRef;
import com.koch.ambeth.audit.model.IAuditedEntityRelationProperty;
import com.koch.ambeth.audit.model.IAuditedEntityRelationPropertyItem;
import com.koch.ambeth.audit.model.IAuditedService;
import com.koch.ambeth.audit.server.AuditEntryWriterV1;
import com.koch.ambeth.audit.server.IAuditEntryReader;
import com.koch.ambeth.audit.server.IAuditEntryWriterExtendable;
import com.koch.ambeth.audit.server.IAuditInfoController;
import com.koch.ambeth.audit.server.config.AuditConfigurationConstants;
import com.koch.ambeth.audit.server.ioc.AuditModule;
import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructureList;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IMergeProcess;
import com.koch.ambeth.merge.security.ISecurityScopeProvider;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.merge.util.setup.AbstractDatasetBuilder;
import com.koch.ambeth.merge.util.setup.IDataSetupWithAuthorization;
import com.koch.ambeth.merge.util.setup.IDatasetBuilder;
import com.koch.ambeth.merge.util.setup.IDatasetBuilderExtendable;
import com.koch.ambeth.persistence.api.IDatabasePool;
import com.koch.ambeth.persistence.jdbc.IConnectionFactory;
import com.koch.ambeth.persistence.jdbc.JdbcUtil;
import com.koch.ambeth.security.DefaultAuthentication;
import com.koch.ambeth.security.ISecurityContextHolder;
import com.koch.ambeth.security.PasswordType;
import com.koch.ambeth.security.StringSecurityScope;
import com.koch.ambeth.security.server.IPasswordUtil;
import com.koch.ambeth.security.server.IPrivateKeyProvider;
import com.koch.ambeth.security.server.ISignatureUtil;
import com.koch.ambeth.security.server.config.SecurityServerConfigurationConstants;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLDataRebuild;
import com.koch.ambeth.testutil.TestFrameworkModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.util.codec.Base64;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.collections.LinkedHashSet;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.function.CheckedConsumer;
import com.koch.ambeth.util.model.IDataObject;
import com.koch.ambeth.util.state.IStateRollback;
import com.koch.ambeth.util.state.StateRollback;
import com.koch.ambeth.xml.ioc.XmlModule;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@TestFrameworkModule({
        AuditModule.class, AuditMethodCallTestFrameworkModule.class, AuditTamperTestModule.class, XmlModule.class
})
@TestPropertiesList({
        @TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "AuditMethodCall_orm.xml;security-orm.xml"),
        @TestProperties(name = AuditConfigurationConstants.AuditActive, value = "true"),
        @TestProperties(name = SecurityServerConfigurationConstants.EncryptionPaddedKeyIterationCount, value = "1"),
        @TestProperties(name = SecurityServerConfigurationConstants.LoginPasswordAlgorithmIterationCount, value = "1"),
        @TestProperties(name = SecurityServerConfigurationConstants.SignaturePaddedKeyIterationCount, value = "1"),
        // @TestProperties(name = "ambeth.log.level.com.koch.ambeth.persistence", value = "DEBUG"),
        // @TestProperties(name = "ambeth.log.level.com.koch.ambeth.merge", value = "DEBUG"),
        @TestProperties(name = SecurityServerConfigurationConstants.SignatureActive, value = "true")
})
@SQLStructureList({
        @SQLStructure("security-structure.sql"), //
        @SQLStructure("audit-structure.sql")
})
@SQLDataRebuild
public class AuditTamperTest extends AbstractInformationBusWithPersistenceTest {
    @Autowired
    protected IAuditInfoController auditController;
    @Autowired
    protected IAuditEntryReader auditEntryReader;
    @Autowired
    protected IAuditEntryVerifier auditEntryVerifier;
    @Autowired
    protected AuditTamperDataBuilder auditTamperDataBuilder;
    @Autowired
    protected IConnectionFactory connectionFactory;
    @Autowired
    protected IDatabasePool databasePool;
    @Autowired
    protected IEventDispatcher eventDispatcher;
    @Autowired
    protected IMergeProcess mergeProcess;
    @Autowired
    protected IPrivateKeyProvider privateKeyProvider;
    @Autowired
    protected ITestAuditService testAuditService;
    @LogInstance
    private ILogger log;
    private AuditEntry auditEntry;
    private List<IAuditEntry> allAuditEntriesOfUser;
    private AuditedEntity auditedEntity;
    private AuditedEntityPrimitiveProperty auditedEntityPrimitive;
    private AuditedEntityRef auditedEntityRef;

    protected void before() {
        allAuditEntriesOfUser = null;
        auditEntry = null;
        User user = auditTamperDataBuilder.defaultUser;
        List<IAuditEntry> allAuditEntriesOfUser = auditEntryReader.getAllAuditEntriesOfUser(user);
        Assert.assertEquals(1, allAuditEntriesOfUser.size());
        this.allAuditEntriesOfUser = allAuditEntriesOfUser;
        auditEntry = (AuditEntry) allAuditEntriesOfUser.get(0);
        auditedEntity = auditEntry.getEntities().get(0);
        auditedEntityRef = (AuditedEntityRef) auditedEntity.getRef();
        auditedEntityPrimitive = auditedEntity.getPrimitives().get(0);
    }

    @Test
    public void tamperAuditEntry_POSITIVE() throws Exception {
        before();

        manipulateTables(stm -> {
            // intended blank
        }, "AUDIT_ENTRY");
        verify(auditEntry, true);
    }

    @Test
    public void tamperAuditEntryChain_deleteAuditEntry() throws Exception {
        User otherUser = auditTamperDataBuilder.otherUser;
        prepareAuditEntries(otherUser);
        Assert.assertTrue(auditEntryVerifier.verifyEntities(Arrays.asList(otherUser)));
        List<IAuditEntry> auditEntries = auditEntryReader.getAllAuditEntriesOfEntity(otherUser);
        // 1 initial insert and 3 updates from 3 transactions above => 4 audit entries
        Assert.assertEquals(4, auditEntries.size());
        IAuditEntry secondAuditEntry = auditEntries.get(1);
        markDelete(secondAuditEntry);
        mergeProcess.process(secondAuditEntry);

        // ensure that the verifier always reads data directly from the persistence again
        eventDispatcher.dispatchEvent(ClearAllCachesEvent.getInstance());
        Assert.assertFalse(auditEntryVerifier.verifyEntities(Arrays.asList(otherUser)));
    }

    @Test
    public void tamperAuditEntryChain_deleteAuditedEntity0() throws Exception {
        tamperAuditEntryChain_deleteAuditedEntity_intern(auditTamperDataBuilder.otherUser, 0);
    }

    @Test
    public void tamperAuditEntryChain_deleteAuditedEntity1() throws Exception {
        tamperAuditEntryChain_deleteAuditedEntity_intern(auditTamperDataBuilder.otherUser, 1);
    }

    @Test
    public void tamperAuditEntryChain_deleteAuditedEntity2() throws Exception {
        tamperAuditEntryChain_deleteAuditedEntity_intern(auditTamperDataBuilder.otherUser, 2);
    }

    @Test
    public void tamperAuditEntryChain_deleteAuditedEntity3() throws Exception {
        tamperAuditEntryChain_deleteAuditedEntity_intern(auditTamperDataBuilder.otherUser, 3);
    }

    @Test
    public void tamperAuditEntryChain_deleteAuditedEntity4() throws Exception {
        tamperAuditEntryChain_deleteAuditedEntity_intern(auditTamperDataBuilder.otherUser, 4);
    }

    protected void tamperAuditEntryChain_deleteAuditedEntity_intern(User otherUser, int indexToDrop) throws Exception {
        ObjRef sidObjRef = new ObjRef(User.class, (byte) 0, otherUser.getSID(), otherUser.getVersion());
        prepareAuditEntries(otherUser);
        Assert.assertTrue(auditEntryVerifier.verifyEntities(Arrays.asList(otherUser)));
        Assert.assertTrue(auditEntryVerifier.verifyEntities(Arrays.asList(sidObjRef)));
        List<IAuditedEntity> auditedEntities = auditEntryReader.getAllAuditedEntitiesOfEntity(otherUser);
        // 1 initial insert from the DATA SETUP and 4 updates from 3 transactions within
        // prepareAuditEntries() called above => 5 audited entities
        Assert.assertEquals(5, auditedEntities.size());
        markDelete(auditedEntities.get(indexToDrop));
        mergeProcess.process(auditedEntities);

        // ensure that the verifier always reads data directly from the persistence again
        eventDispatcher.dispatchEvent(ClearAllCachesEvent.getInstance());
        Assert.assertFalse(auditEntryVerifier.verifyEntities(Arrays.asList(sidObjRef)));
    }

    private void prepareAuditEntries(final User userToModify) {
        var rollback = auditController.pushAuthorizedUser(auditTamperDataBuilder.defaultUser, AuditTamperDataBuilder.DEFAULT_PASSWORD.toCharArray(), true);
        try {
            var auditRollback = auditController.pushAuditReason("modify user '" + userToModify.getSID() + "' first");
            try {
                transaction.runInTransaction(() -> {
                    userToModify.setActive(!userToModify.isActive());
                    mergeProcess.process(userToModify);
                    userToModify.setActive(!userToModify.isActive());
                    mergeProcess.process(userToModify);
                });
            } finally {
                auditRollback.rollback();
            }
            auditRollback = auditController.pushAuditReason("modify user '" + userToModify.getSID() + "' second");
            try {
                transaction.runInTransaction(() -> {
                    userToModify.setActive(!userToModify.isActive());
                    mergeProcess.process(userToModify);
                });
            } finally {
                auditRollback.rollback();
            }
            auditRollback = auditController.pushAuditReason("modify user '" + userToModify.getSID() + "' third");
            try {
                transaction.runInTransaction(() -> {
                    userToModify.setActive(!userToModify.isActive());
                    mergeProcess.process(userToModify);
                });
            } finally {
                auditRollback.rollback();
            }
        } finally {
            rollback.rollback();
        }
    }

    @Test
    public void tamperAuditEntry_Context() throws Exception {
        before();
        simpleManipulation("AUDIT_ENTRY", "\"CONTEXT\"='other-context'", auditEntry);
        verify(auditEntry, false);
    }

    @Test
    public void tamperAuditEntry_Protocol() throws Exception {
        before();
        simpleManipulation("AUDIT_ENTRY", "\"PROTOCOL\"=" + AuditTamperTestModule.TAMPERED_WRITER_VERSION, auditEntry);
        verify(auditEntry, false);
    }

    @Test
    public void tamperAuditEntry_Reason() throws Exception {
        before();
        simpleManipulation("AUDIT_ENTRY", "\"REASON\"='other-reason'", auditEntry);
        verify(auditEntry, false);
    }

    @Test
    public void tamperAuditEntry_Timestamp() throws Exception {
        before();
        simpleManipulation("AUDIT_ENTRY", "\"TIMESTAMP\"=1234", auditEntry);
        verify(auditEntry, false);
    }

    @Test
    public void tamperAuditEntry_UserIdentifier() throws Exception {
        before();
        simpleManipulation("AUDIT_ENTRY", "\"USER_IDENTIFIER\"=null", auditEntry);
        verify(auditEntry, false);
    }

    @Test
    public void tamperAuditEntry_HashAlgorithm() throws Exception {
        before();
        simpleManipulation("AUDIT_ENTRY", "\"HASH_ALGORITHM\"='MD5'", auditEntry);
        verify(auditEntry, false);
    }

    @Test
    public void tamperAuditEntry_SignedValue() throws Exception {
        before();
        java.security.Signature signature = privateKeyProvider.getSigningHandle(auditTamperDataBuilder.defaultUser, AuditTamperDataBuilder.DEFAULT_PASSWORD.toCharArray());
        signature.update("other-signature".getBytes("UTF-8"));
        simpleManipulation("AUDIT_ENTRY", "\"SIGNED_VALUE\"='" + Base64.encodeBytes(signature.sign()) + "'", auditEntry);
        verify(auditEntry, false);
    }

    @Test
    public void tamperAuditEntry_SignatureOfUser() throws Exception {
        before();
        Signature signature = (Signature) auditTamperDataBuilder.otherUser.getSignature();
        simpleManipulation("AUDIT_ENTRY", "\"SIGNATURE_OF_USER_ID\"=" + signature.getId(), auditEntry);
        verify(auditEntry, false);
    }

    @Test
    public void tamperAuditEntry_Services() throws Exception {
        before();
        Short oldVersion = auditEntry.getVersion();
        AuditedService auditedService = entityFactory.createEntity(AuditedService.class);
        IEntityMetaData metaData = entityMetaDataProvider.getMetaData(AuditedService.class);
        metaData.getMemberByName(IAuditedService.ServiceType).setValue(auditedService, "ServiceType");
        metaData.getMemberByName(IAuditedService.MethodName).setValue(auditedService, "methodName");
        metaData.getMemberByName(IAuditedService.SpentTime).setValue(auditedService, 123);
        metaData.getMemberByName(IAuditedService.Entry).setValue(auditedService, auditEntry);
        metaData.getMemberByName(IAuditedService.Order).setValue(auditedService, 1);
        mergeProcess.process(auditedService);
        simpleManipulation("AUDIT_ENTRY", "\"VERSION\"=" + oldVersion, auditEntry);
        verify(auditEntry, false);
    }

    @Test
    public void tamperAuditEntry_Entities() throws Exception {
        before();
        Short oldVersion = auditEntry.getVersion();
        AuditedEntity auditedEntity = auditEntry.getEntities().get(0);
        ((IDataObject) auditedEntity).setToBeDeleted(true);
        for (IAuditedEntityPrimitiveProperty primitive : auditedEntity.getPrimitives()) {
            ((IDataObject) primitive).setToBeDeleted(true);
        }
        for (IAuditedEntityRelationProperty relation : auditedEntity.getRelations()) {
            ((IDataObject) relation).setToBeDeleted(true);
            for (IAuditedEntityRelationPropertyItem item : relation.getItems()) {
                ((IDataObject) item).setToBeDeleted(true);
            }
        }
        mergeProcess.process(auditedEntity);
        simpleManipulation("AUDIT_ENTRY", "\"VERSION\"=" + oldVersion, auditEntry);
        verify(auditEntry, false);
    }

    @Test
    public void tamperAuditedEntity_ChangeType() throws Exception {
        before();
        simpleManipulation("AUDITED_ENTITY", "\"CHANGE_TYPE\"='" + AuditedEntityChangeType.UPDATE.name() + "'", auditedEntity);
        verify(auditEntry, false);
    }

    @Test
    public void tamperAuditedEntity_Order() throws Exception {
        before();
        simpleManipulation("AUDITED_ENTITY", "\"ORDER\"=100", auditedEntity);
        verify(auditEntry, false);
    }

    @Test
    public void tamperAuditedEntity_SignedValue() throws Exception {
        before();
        java.security.Signature signature = privateKeyProvider.getSigningHandle(auditTamperDataBuilder.defaultUser, AuditTamperDataBuilder.DEFAULT_PASSWORD.toCharArray());
        signature.update("other-signature".getBytes("UTF-8"));
        simpleManipulation("AUDITED_ENTITY", "\"SIGNED_VALUE\"='" + Base64.encodeBytes(signature.sign()) + "'", auditedEntity);
        verify(auditEntry, false);
    }

    @Test
    public void tamperAuditedEntity_Primitives() throws Exception {
        before();
        Short oldVersion = auditedEntity.getVersion();
        AuditedEntityPrimitiveProperty primitive = entityFactory.createEntity(AuditedEntityPrimitiveProperty.class);
        IEntityMetaData metaData = entityMetaDataProvider.getMetaData(AuditedEntityPrimitiveProperty.class);
        metaData.getMemberByName(IAuditedEntityPrimitiveProperty.Entity).setValue(primitive, auditedEntity);
        metaData.getMemberByName(IAuditedEntityPrimitiveProperty.Name).setValue(primitive, "methodName");
        metaData.getMemberByName(IAuditedEntityPrimitiveProperty.NewValue).setValue(primitive, "Hallo");
        metaData.getMemberByName(IAuditedEntityPrimitiveProperty.Order).setValue(primitive, auditedEntity.getPrimitives().size() + 1);
        mergeProcess.process(primitive);
        simpleManipulation("AUDITED_ENTITY", "\"VERSION\"=" + oldVersion, auditedEntity);
        verify(auditEntry, false);
    }

    @Test
    public void tamperAuditedEntity_Relations() throws Exception {
        before();
        Short oldVersion = auditedEntity.getVersion();
        AuditedEntityRelationProperty relation = entityFactory.createEntity(AuditedEntityRelationProperty.class);
        AuditedEntityRelationPropertyItem item = entityFactory.createEntity(AuditedEntityRelationPropertyItem.class);
        AuditedEntityRef ref = entityFactory.createEntity(AuditedEntityRef.class);

        relation.getItems().add(item);
        {
            IEntityMetaData metaData = entityMetaDataProvider.getMetaData(AuditedEntityRef.class);
            metaData.getMemberByName(IAuditedEntityRef.EntityType).setValue(ref, getClass().getName());
            metaData.getMemberByName(IAuditedEntityRef.EntityId).setValue(ref, "1");
            metaData.getMemberByName(IAuditedEntityRef.EntityVersion).setValue(ref, "1");
        }
        {
            IEntityMetaData metaData = entityMetaDataProvider.getMetaData(AuditedEntityRelationPropertyItem.class);
            metaData.getMemberByName(IAuditedEntityRelationPropertyItem.ChangeType).setValue(item, AuditedEntityPropertyItemChangeType.ADD);
            metaData.getMemberByName(IAuditedEntityRelationPropertyItem.Ref).setValue(item, ref);
            metaData.getMemberByName(IAuditedEntityRelationPropertyItem.Order).setValue(item, 1);
        }
        {
            IEntityMetaData metaData = entityMetaDataProvider.getMetaData(AuditedEntityRelationProperty.class);
            metaData.getMemberByName(IAuditedEntityRelationProperty.Entity).setValue(relation, auditedEntity);
            metaData.getMemberByName(IAuditedEntityRelationProperty.Name).setValue(relation, "methodName");
            metaData.getMemberByName(IAuditedEntityRelationProperty.Order).setValue(relation, auditedEntity.getRelations().size() + 1);
        }
        mergeProcess.process(relation);
        simpleManipulation("AUDITED_ENTITY", "\"VERSION\"=" + oldVersion, auditedEntity);
        verify(auditEntry, false);
    }

    @Test
    public void tamperAuditedEntityRef_EntityType() throws Exception {
        before();
        simpleManipulation("AUDITED_ENTITY_REF", "\"ENTITY_TYPE\"='" + getClass().getName() + "'", auditedEntityRef);
        verify(auditEntry, false);
    }

    @Test
    public void tamperAuditedEntityRef_EntityId() throws Exception {
        before();
        simpleManipulation("AUDITED_ENTITY_REF", "\"ENTITY_ID\"=123", auditedEntityRef);
        verify(auditEntry, false);
    }

    @Test
    public void tamperAuditedEntityRef_EntityVersion() throws Exception {
        before();
        simpleManipulation("AUDITED_ENTITY_REF", "\"ENTITY_VERSION\"=123", auditedEntityRef);
        verify(auditEntry, false);
    }

    @Test
    public void tamperAuditedEntityPrimitive_Name() throws Exception {
        before();
        simpleManipulation("AE_PRIMITIVE", "\"NAME\"='abc'", auditedEntityPrimitive);
        verify(auditEntry, false);
    }

    @Test
    public void tamperAuditedEntityPrimitive_NewValue() throws Exception {
        before();
        simpleManipulation("AE_PRIMITIVE", "\"NEW_VALUE\"='abc'", auditedEntityPrimitive);
        verify(auditEntry, false);
    }

    @Test
    public void tamperAuditedEntityPrimitive_Order() throws Exception {
        before();
        simpleManipulation("AE_PRIMITIVE", "\"ORDER\"=500", auditedEntityPrimitive);
        verify(auditEntry, false);
    }

    protected void simpleManipulation(String tableName, String sqlUpdateFragment, IAbstractAuditEntity entity) throws Exception {
        manipulateTables(createSimpleUpdate(tableName, sqlUpdateFragment, entity), tableName);
    }

    protected void simpleManipulation(final String[] tableNames, final String[] sqlUpdateFragments, final IAbstractAuditEntity entity) throws Exception {
        CheckedConsumer<Statement> delegate = state -> {
            for (int a = 0, size = tableNames.length; a < size; a++) {
                String tableName = tableNames[a];
                if (sqlUpdateFragments[a] == null) {
                    continue;
                }
                var currDelegate = createSimpleUpdate(tableName, sqlUpdateFragments[a], entity);
                CheckedConsumer.invoke(currDelegate, state);
            }
        };
        manipulateTables(delegate, tableNames);
    }

    protected CheckedConsumer<Statement> createSimpleUpdate(final String tableName, final String sqlUpdateFragment, final IAbstractAuditEntity entity) {
        return stm -> {
            Assert.assertEquals(1, stm.executeUpdate("UPDATE \"" + tableName + "\" SET " + sqlUpdateFragment + " WHERE \"ID\"=" + entity.getId()));
        };
    }

    protected void verify(IAuditEntry auditEntry, boolean assertedValue) {
        // ensure that the verifier always reads data directly from the persistence again
        eventDispatcher.dispatchEvent(ClearAllCachesEvent.getInstance());

        boolean[] results = auditEntryVerifier.verifyAuditEntries(Arrays.asList(auditEntry));
        Assert.assertEquals(1, results.length);
        for (boolean result : results) {
            Assert.assertEquals(assertedValue, result);
        }
    }

    @SneakyThrows
    protected void manipulateTables(Connection connection, CheckedConsumer<Statement> delegate, String... tableNames) {
        Statement stm = null;
        ResultSet rs = null;
        try {
            stm = connection.createStatement();
            ISet<String> tableNameSet = new LinkedHashSet<>(tableNames);
            for (String tableName : tableNameSet) {
                stm.execute("ALTER TABLE \"" + tableName + "\" DISABLE TRIGGER \"TR_" + tableName + "_OL\"");
            }
            CheckedConsumer.invoke(delegate, stm);
            connection.commit();
            for (String tableName : tableNameSet) {
                stm.execute("ALTER TABLE \"" + tableName + "\" ENABLE TRIGGER \"TR_" + tableName + "_OL\"");
            }
        } finally {
            JdbcUtil.close(stm, rs);
        }
    }

    protected void manipulateTables(CheckedConsumer<Statement> delegate, String... tableNames) {
        var database = databasePool.acquireDatabase();
        try {
            var connection = database.getAutowiredBeanInContext(Connection.class);
            manipulateTables(connection, delegate, tableNames);
            database.flushAndRelease();
        } catch (Throwable e) {
            database.release(true);
            throw RuntimeExceptionUtil.mask(e);
        }
    }

    protected void markDelete(IAuditEntry auditEntry) {
        ((IDataObject) auditEntry).setToBeDeleted(true);
        for (IAuditedEntity auditedEntity : auditEntry.getEntities()) {
            markDelete(auditedEntity);
        }
        for (IAuditedService auditedService : auditEntry.getServices()) {
            ((IDataObject) auditedService).setToBeDeleted(true);
        }
    }

    protected void markDelete(IAuditedEntity auditedEntity) {
        ((IDataObject) auditedEntity).setToBeDeleted(true);
        for (IAuditedEntityPrimitiveProperty primitive : auditedEntity.getPrimitives()) {
            ((IDataObject) primitive).setToBeDeleted(true);
        }
        for (IAuditedEntityRelationProperty relation : auditedEntity.getRelations()) {
            ((IDataObject) relation).setToBeDeleted(true);
            for (IAuditedEntityRelationPropertyItem item : relation.getItems()) {
                ((IDataObject) item).setToBeDeleted(true);
            }
        }
    }

    public static class AuditTamperTestModule implements IInitializingModule {
        public static final int TAMPERED_WRITER_VERSION = 50000;

        @Override
        public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
            beanContextFactory.registerBean(TestAuditService.class).autowireable(ITestAuditService.class);

            IBeanConfiguration auditTamperDataBuilder = beanContextFactory.registerBean(AuditTamperDataBuilder.class).autowireable(AuditTamperDataBuilder.class);
            beanContextFactory.link(auditTamperDataBuilder).to(IDatasetBuilderExtendable.class);

            IBeanConfiguration tamperedAuditEntryWriter = beanContextFactory.registerBean(AuditEntryWriterV1.class);
            beanContextFactory.link(tamperedAuditEntryWriter).to(IAuditEntryWriterExtendable.class).with(Integer.valueOf(TAMPERED_WRITER_VERSION));

        }
    }

    public static class AuditTamperDataBuilder extends AbstractDatasetBuilder implements IDataSetupWithAuthorization {
        public static final String DEFAULT_USER = "dummyUser", DEFAULT_PASSWORD = "dummyPassword";

        public static final String OTHER_USER = "dummyUser2", OTHER_PASSWORD = "dummyPassword2";
        public User defaultUser, otherUser;
        @Autowired
        protected IAuditInfoController auditInfoController;
        @Autowired
        protected IPasswordUtil passwordUtil;
        @Autowired
        protected ISecurityContextHolder securityContextHolder;
        @Autowired
        protected ISecurityScopeProvider securityScopeProvider;
        @Autowired
        protected ISignatureUtil signatureUtil;

        @Override
        public Collection<Class<? extends IDatasetBuilder>> getDependsOn() {
            return null;
        }

        @Override
        protected void buildDatasetInternal() {
            defaultUser = createUser(DEFAULT_USER, DEFAULT_PASSWORD);
            defaultUser.setActive(true);
            otherUser = createUser(OTHER_USER, OTHER_PASSWORD);
            otherUser.setActive(true);
        }

        protected User createUser(String name, String password) {
            return createUser(name, password.toCharArray());
        }

        protected User createUser(String name, char[] password) {
            User user = createEntity(User.class);
            user.setSID(name.toLowerCase());
            passwordUtil.assignNewPassword(password, user, null);
            Signature signature = createEntity(Signature.class);
            signatureUtil.generateNewSignature(signature, password);
            user.setSignature(signature);
            return user;
        }

        @Override
        public IStateRollback pushAuthorization() {
            return StateRollback.chain(chain -> {
                chain.append(securityContextHolder.pushAuthentication(new DefaultAuthentication(DEFAULT_USER, DEFAULT_PASSWORD.toCharArray(), PasswordType.PLAIN)));
                chain.append(securityScopeProvider.pushSecurityScopes(StringSecurityScope.DEFAULT_SCOPE));
                chain.append(auditInfoController.pushAuthorizedUser(defaultUser, DEFAULT_PASSWORD.toCharArray(), true));
            });
        }
    }
}
