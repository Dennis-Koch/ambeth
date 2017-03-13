package com.koch.ambeth.persistence.blueprint;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.audit.server.IAuditConfiguration;
import com.koch.ambeth.audit.server.IAuditConfigurationProvider;
import com.koch.ambeth.audit.server.ioc.AuditModule;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.mapping.IMapperService;
import com.koch.ambeth.mapping.IMapperServiceFactory;
import com.koch.ambeth.mapping.ioc.MappingModule;
import com.koch.ambeth.merge.ITechnicalEntityTypeExtendable;
import com.koch.ambeth.merge.config.IEntityMetaDataReader;
import com.koch.ambeth.merge.orm.blueprint.IBlueprintOrmProvider;
import com.koch.ambeth.merge.orm.blueprint.IBlueprintProvider;
import com.koch.ambeth.merge.orm.blueprint.IBlueprintVomProvider;
import com.koch.ambeth.merge.orm.blueprint.IEntityAnnotationBlueprint;
import com.koch.ambeth.merge.orm.blueprint.IEntityAnnotationPropertyBlueprint;
import com.koch.ambeth.merge.orm.blueprint.IEntityPropertyBlueprint;
import com.koch.ambeth.merge.orm.blueprint.IEntityTypeBlueprint;
import com.koch.ambeth.persistence.blueprint.OrmBlueprintTest.OrmBlueprintTestModule;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLData;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.TestFrameworkModule;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;
import com.koch.ambeth.xml.ioc.XmlBlueprintModule;
import com.koch.ambeth.xml.ioc.XmlModule;
import com.koch.ambeth.xml.orm.blueprint.JavassistOrmEntityTypeProvider;

@SQLStructure("OrmBlueprint_structure.sql")
@SQLData("OrmBlueprint_data.sql")
@TestPropertiesList({
		@TestProperties(name = ServiceConfigurationConstants.mappingFile,
				value = "com/koch/ambeth/persistence/blueprint/orm.xml"),
		@TestProperties(name = ServiceConfigurationConstants.GenericTransferMapping, value = "true")})
@TestFrameworkModule({XmlModule.class, MappingModule.class, AuditModule.class})
@TestModule({XmlBlueprintModule.class, OrmBlueprintTestModule.class})
public class OrmBlueprintTest extends AbstractInformationBusWithPersistenceTest {
	public static final String AMBETH_PERSISTENCE_BLUEPRINT_TEST_CLASS =
			"com.koch.ambeth.persistence.blueprint.TestClass";
	public static final String AMBETH_PERSISTENCE_BLUEPRINT_TEST_CLASS_PROP = "Something";

	public static class OrmBlueprintTestModule implements IInitializingModule {
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
			beanContextFactory.link(IEntityTypeBlueprint.class).to(ITechnicalEntityTypeExtendable.class)
					.with(EntityTypeBlueprint.class);
			beanContextFactory.link(IEntityPropertyBlueprint.class)
					.to(ITechnicalEntityTypeExtendable.class).with(EntityPropertyBlueprint.class);
			beanContextFactory.link(IEntityAnnotationBlueprint.class)
					.to(ITechnicalEntityTypeExtendable.class).with(EntityAnnotationBlueprint.class);
			beanContextFactory.link(IEntityAnnotationPropertyBlueprint.class)
					.to(ITechnicalEntityTypeExtendable.class).with(EntityAnnotationPropertyBlueprint.class);
			beanContextFactory.registerBean(SQLOrmBlueprintProvider.class).autowireable(
					IBlueprintProvider.class, IBlueprintOrmProvider.class, IBlueprintVomProvider.class);
			beanContextFactory.registerBean(OrmVomDocumentCreator.class)
					.autowireable(IVomDocumentCreator.class, IOrmDocumentCreator.class);
			beanContextFactory.registerBean(EntityTypeBluePrintService.class)
					.autowireable(EntityTypeBluePrintService.class);
		}
	}

	@LogInstance
	private ILogger log;

	@Autowired(XmlBlueprintModule.JAVASSIST_ORM_ENTITY_TYPE_PROVIDER)
	protected JavassistOrmEntityTypeProvider entityTypeProvider;

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	@Autowired
	protected IEntityMetaDataReader entityMetaDataReader;

	@Autowired
	protected IMapperServiceFactory mapperServiceFactory;

	@Autowired
	protected IAuditConfigurationProvider auditConfigurationProvider;

	@Test
	public void testIntatiateBlueprintedEntity() throws Throwable {
		Class<?> resolveEntityType =
				entityTypeProvider.resolveEntityType(AMBETH_PERSISTENCE_BLUEPRINT_TEST_CLASS);
		Assert.assertNotNull(resolveEntityType);

		Object entity = entityFactory.createEntity(resolveEntityType);
		IPropertyInfo[] properties = propertyInfoProvider.getProperties(entity);

		IPropertyInfo prop = null;
		for (IPropertyInfo propertyInfo : properties) {
			if (propertyInfo.getName().equals(AMBETH_PERSISTENCE_BLUEPRINT_TEST_CLASS_PROP)) {
				prop = propertyInfo;
				break;
			}
		}
		Assert.assertNotNull(prop);
		prop.setValue(entity, "TestValue");

		Assert.assertEquals("TestValue", prop.getValue(entity));

		IAuditConfiguration auditConfiguration =
				auditConfigurationProvider.getAuditConfiguration(resolveEntityType);
		Assert.assertTrue(auditConfiguration.isAuditActive());

	}

	@Test
	public void testVomMappingBlueprintedEntity() throws Throwable {
		Class<?> resolveEntityType =
				entityTypeProvider.resolveEntityType(AMBETH_PERSISTENCE_BLUEPRINT_TEST_CLASS);
		Assert.assertNotNull(resolveEntityType);

		Object entity = entityFactory.createEntity(resolveEntityType);

		IPropertyInfo prop = propertyInfoProvider.getProperty(entity,
				AMBETH_PERSISTENCE_BLUEPRINT_TEST_CLASS_PROP);
		Assert.assertNotNull(prop);
		String testValue = "TestValue";
		prop.setValue(entity, testValue);

		Class<?> valueObjectType = entityTypeProvider
				.resolveEntityType(AMBETH_PERSISTENCE_BLUEPRINT_TEST_CLASS + "V");
		Assert.assertFalse(valueObjectType.isInterface());
		IMapperService mapper = mapperServiceFactory.create();
		try {
			Object valueObject = mapper.mapToValueObject(entity, valueObjectType);
			IPropertyInfo vomProperty = propertyInfoProvider.getProperty(valueObject,
					AMBETH_PERSISTENCE_BLUEPRINT_TEST_CLASS_PROP);
			Assert.assertEquals(testValue, vomProperty.getValue(valueObject));
		}
		finally {
			mapper.dispose();
		}
	}
}
