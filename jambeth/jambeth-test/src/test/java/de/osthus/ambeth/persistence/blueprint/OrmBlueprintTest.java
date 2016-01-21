package de.osthus.ambeth.persistence.blueprint;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.audit.IAuditConfiguration;
import de.osthus.ambeth.audit.IAuditConfigurationProvider;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.AuditModule;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.MappingModule;
import de.osthus.ambeth.ioc.XmlModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.mapping.IMapperService;
import de.osthus.ambeth.mapping.IMapperServiceFactory;
import de.osthus.ambeth.merge.ITechnicalEntityTypeExtendable;
import de.osthus.ambeth.merge.config.IEntityMetaDataReader;
import de.osthus.ambeth.orm.blueprint.IBlueprintOrmProvider;
import de.osthus.ambeth.orm.blueprint.IBlueprintProvider;
import de.osthus.ambeth.orm.blueprint.IBlueprintVomProvider;
import de.osthus.ambeth.orm.blueprint.IEntityAnnotationBlueprint;
import de.osthus.ambeth.orm.blueprint.IEntityAnnotationPropertyBlueprint;
import de.osthus.ambeth.orm.blueprint.IEntityPropertyBlueprint;
import de.osthus.ambeth.orm.blueprint.IEntityTypeBlueprint;
import de.osthus.ambeth.orm.blueprint.JavassistOrmEntityTypeProvider;
import de.osthus.ambeth.persistence.blueprint.OrmBlueprintTest.OrmBlueprintTestModule;
import de.osthus.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestFrameworkModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.typeinfo.IPropertyInfo;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;

@SQLStructure("OrmBlueprint_structure.sql")
@SQLData("OrmBlueprint_data.sql")
@TestPropertiesList({ @TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/persistence/blueprint/orm.xml"),
		@TestProperties(name = ServiceConfigurationConstants.GenericTransferMapping, value = "true") })
@TestFrameworkModule({ XmlModule.class, MappingModule.class, AuditModule.class, OrmBlueprintTestModule.class })
public class OrmBlueprintTest extends AbstractInformationBusWithPersistenceTest
{
	public static final String DE_OSTHUS_AMBETH_PERSISTENCE_BLUEPRINT_TEST_CLASS = "de.osthus.ambeth.persistence.blueprint.TestClass";
	public static final String DE_OSTHUS_AMBETH_PERSISTENCE_BLUEPRINT_TEST_CLASS_PROP = "Something";

	public static class OrmBlueprintTestModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerBean(InitialEntityTypeBluePrintLoadingService.class).autowireable(InitialEntityTypeBluePrintLoadingService.class);

			beanContextFactory.link(IEntityTypeBlueprint.class).to(ITechnicalEntityTypeExtendable.class).with(EntityTypeBlueprint.class);
			beanContextFactory.link(IEntityPropertyBlueprint.class).to(ITechnicalEntityTypeExtendable.class).with(EntityPropertyBlueprint.class);
			beanContextFactory.link(IEntityAnnotationBlueprint.class).to(ITechnicalEntityTypeExtendable.class).with(EntityAnnotationBlueprint.class);
			beanContextFactory.link(IEntityAnnotationPropertyBlueprint.class).to(ITechnicalEntityTypeExtendable.class)
					.with(EntityAnnotationPropertyBlueprint.class);
			beanContextFactory.registerBean(SQLOrmBlueprintProvider.class).autowireable(IBlueprintProvider.class, IBlueprintOrmProvider.class,
					IBlueprintVomProvider.class);
			beanContextFactory.registerBean(OrmVomDocumentCreator.class).autowireable(IVomDocumentCreator.class, IOrmDocumentCreator.class);
		}
	}

	@LogInstance
	private ILogger log;

	@Autowired(XmlModule.JAVASSIST_ORM_ENTITY_TYPE_PROVIDER)
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
	public void testIntatiateBlueprintedEntity() throws Throwable
	{
		Class<?> resolveEntityType = entityTypeProvider.resolveEntityType(DE_OSTHUS_AMBETH_PERSISTENCE_BLUEPRINT_TEST_CLASS);
		Assert.assertNotNull(resolveEntityType);

		Object entity = entityFactory.createEntity(resolveEntityType);
		IPropertyInfo[] properties = propertyInfoProvider.getProperties(entity);

		IPropertyInfo prop = null;
		for (IPropertyInfo propertyInfo : properties)
		{
			if (propertyInfo.getName().equals(DE_OSTHUS_AMBETH_PERSISTENCE_BLUEPRINT_TEST_CLASS_PROP))
			{
				prop = propertyInfo;
				break;
			}
		}
		Assert.assertNotNull(prop);
		prop.setValue(entity, "TestValue");

		Assert.assertEquals("TestValue", prop.getValue(entity));

		IAuditConfiguration auditConfiguration = auditConfigurationProvider.getAuditConfiguration(resolveEntityType);
		Assert.assertTrue(auditConfiguration.isAuditActive());

	}

	@Test
	public void testVomMappingBlueprintedEntity() throws Throwable
	{
		Class<?> resolveEntityType = entityTypeProvider.resolveEntityType(DE_OSTHUS_AMBETH_PERSISTENCE_BLUEPRINT_TEST_CLASS);
		Assert.assertNotNull(resolveEntityType);

		Object entity = entityFactory.createEntity(resolveEntityType);

		IPropertyInfo prop = propertyInfoProvider.getProperty(entity, DE_OSTHUS_AMBETH_PERSISTENCE_BLUEPRINT_TEST_CLASS_PROP);
		Assert.assertNotNull(prop);
		String testValue = "TestValue";
		prop.setValue(entity, testValue);

		Class<?> valueObjectType = entityTypeProvider.resolveEntityType(DE_OSTHUS_AMBETH_PERSISTENCE_BLUEPRINT_TEST_CLASS + "V");
		Assert.assertFalse(valueObjectType.isInterface());
		IMapperService mapper = mapperServiceFactory.create();
		try
		{
			Object valueObject = mapper.mapToValueObject(entity, valueObjectType);
			IPropertyInfo vomProperty = propertyInfoProvider.getProperty(valueObject, DE_OSTHUS_AMBETH_PERSISTENCE_BLUEPRINT_TEST_CLASS_PROP);
			Assert.assertEquals(testValue, vomProperty.getValue(valueObject));
		}
		finally
		{
			mapper.dispose();
		}
	}
}
