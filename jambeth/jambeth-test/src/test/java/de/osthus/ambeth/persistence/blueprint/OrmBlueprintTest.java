package de.osthus.ambeth.persistence.blueprint;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.ISecondLevelCacheManager;
import de.osthus.ambeth.collections.LinkedHashSet;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.XmlModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataExtendable;
import de.osthus.ambeth.merge.IMergeProcess;
import de.osthus.ambeth.merge.ITechnicalEntityTypeExtendable;
import de.osthus.ambeth.merge.config.IEntityMetaDataReader;
import de.osthus.ambeth.merge.model.EntityMetaData;
import de.osthus.ambeth.model.IAbstractEntity;
import de.osthus.ambeth.orm.IEntityConfig;
import de.osthus.ambeth.orm.IOrmConfigGroup;
import de.osthus.ambeth.orm.IOrmConfigGroupProvider;
import de.osthus.ambeth.orm.blueprint.IEntityAnnotationBlueprint;
import de.osthus.ambeth.orm.blueprint.IEntityAnnotationPropertyBlueprint;
import de.osthus.ambeth.orm.blueprint.IEntityPropertyBlueprint;
import de.osthus.ambeth.orm.blueprint.IEntityTypeBlueprint;
import de.osthus.ambeth.orm.blueprint.IOrmBlueprintProvider;
import de.osthus.ambeth.orm.blueprint.JavassistOrmEntityTypeProvider;
import de.osthus.ambeth.persistence.blueprint.OrmBlueprintTest.OrmBlueprintTestFrameworkModule;
import de.osthus.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestFrameworkModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.typeinfo.IPropertyInfo;
import de.osthus.ambeth.typeinfo.IPropertyInfoProvider;
import de.osthus.ambeth.util.xml.IXmlConfigUtil;

@SQLStructure("OrmBlueprint_structure.sql")
@TestPropertiesList({ @TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/persistence/blueprint/orm.xml") })
@TestFrameworkModule({ XmlModule.class, OrmBlueprintTestFrameworkModule.class })
public class OrmBlueprintTest extends AbstractInformationBusWithPersistenceTest
{
	public static final String DE_OSTHUS_AMBETH_PERSISTENCE_BLUEPRINT_TEST_CLASS = "de.osthus.ambeth.persistence.blueprint.TestClass";
	public static final String DE_OSTHUS_AMBETH_PERSISTENCE_BLUEPRINT_TEST_CLASS_PROP = "Something";

	public static final String IN_MEMORY_CACHE_RETRIEVER = "inMemoryCacheRetriever";

	public static class OrmBlueprintTestFrameworkModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerBean(SQLOrmBlueprintProvider.class).autowireable(IOrmBlueprintProvider.class);

			beanContextFactory.link(IEntityTypeBlueprint.class).to(ITechnicalEntityTypeExtendable.class).with(EntityTypeBlueprint.class);
			beanContextFactory.link(IEntityPropertyBlueprint.class).to(ITechnicalEntityTypeExtendable.class).with(EntityPropertyBlueprint.class);
			beanContextFactory.link(IEntityAnnotationBlueprint.class).to(ITechnicalEntityTypeExtendable.class).with(EntityAnnotationBlueprint.class);
			beanContextFactory.link(IEntityAnnotationPropertyBlueprint.class).to(ITechnicalEntityTypeExtendable.class)
					.with(EntityAnnotationPropertyBlueprint.class);

			// IBeanConfiguration inMemoryCacheRetriever = beanContextFactory.registerBean(IN_MEMORY_CACHE_RETRIEVER, InMemoryCacheRetriever.class);
			// beanContextFactory.link(inMemoryCacheRetriever).to(ICacheRetrieverExtendable.class).with(EntityTypeBlueprint.class);
			// beanContextFactory.link(inMemoryCacheRetriever).to(ICacheRetrieverExtendable.class).with(EntityPropertyBlueprint.class);
			// beanContextFactory.link(inMemoryCacheRetriever).to(ICacheRetrieverExtendable.class).with(EntityAnnotationBlueprint.class);
			// beanContextFactory.link(inMemoryCacheRetriever).to(ICacheRetrieverExtendable.class).with(EntityAnnotationPropertyBlueprint.class);
		}
	}

	protected void readConfig(IOrmConfigGroup ormConfigGroup)
	{
		LinkedHashSet<IEntityConfig> entities = new LinkedHashSet<IEntityConfig>();
		entities.addAll(ormConfigGroup.getLocalEntityConfigs());
		entities.addAll(ormConfigGroup.getExternalEntityConfigs());

		for (IEntityConfig entityConfig : entities)
		{
			Class<?> entityType = entityConfig.getEntityType();
			if (entityMetaDataProvider.getMetaData(entityType, true) != null)
			{
				continue;
			}
			Class<?> realType = entityConfig.getRealType();

			EntityMetaData metaData = new EntityMetaData();
			metaData.setEntityType(entityType);
			metaData.setRealType(realType);
			metaData.setLocalEntity(entityConfig.isLocal());

			entityMetaDataReader.addMembers(metaData, entityConfig);

			entityMetaDataExtendable.registerEntityMetaData(metaData);
		}
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICache cache;
	//
	// @Autowired(IN_MEMORY_CACHE_RETRIEVER)
	// protected InMemoryCacheRetriever inMemoryCacheRetriever;

	@Autowired
	protected ISecondLevelCacheManager secondLevelCacheManager;

	@Autowired(XmlModule.JAVASSIST_ORM_ENTITY_TYPE_PROVIDER)
	protected JavassistOrmEntityTypeProvider entityTypeProvider;

	@Autowired
	protected IPropertyInfoProvider propertyInfoProvider;

	@Autowired
	protected IMergeProcess mergeProcess;

	@Autowired
	protected IEntityMetaDataReader entityMetaDataReader;

	@Autowired
	protected IXmlConfigUtil xmlConfigUtil;

	@Autowired
	protected IEntityMetaDataExtendable entityMetaDataExtendable;

	@Autowired
	protected IOrmConfigGroupProvider ormConfigGroupProvider;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();
		EntityTypeBlueprint entityTypeBlueprint = entityFactory.createEntity(EntityTypeBlueprint.class);
		entityTypeBlueprint.setName(DE_OSTHUS_AMBETH_PERSISTENCE_BLUEPRINT_TEST_CLASS);
		entityTypeBlueprint.getInherits().add(IAbstractEntity.class.getName());

		EntityPropertyBlueprint entityPropertyBlueprint = entityFactory.createEntity(EntityPropertyBlueprint.class);
		entityPropertyBlueprint.setName(DE_OSTHUS_AMBETH_PERSISTENCE_BLUEPRINT_TEST_CLASS_PROP);
		entityPropertyBlueprint.setType(String.class.getName());

		entityTypeBlueprint.getProperties().add(entityPropertyBlueprint);

		mergeProcess.process(entityTypeBlueprint, null, null, null);

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		Document document = dbf
				.newDocumentBuilder()
				.parse(new InputSource(
						new StringReader(
								"<?xml version=\"1.0\" encoding=\"UTF-8\"?><or-mappings xmlns=\"http://osthus.de/ambeth/ambeth_orm_2_0\"><entity-mappings><external-entity class=\"de.osthus.ambeth.persistence.blueprint.TestClass\"/></entity-mappings></or-mappings>")));
		IOrmConfigGroup ormConfigGroup = ormConfigGroupProvider.getOrmConfigGroup(new Document[] { document }, entityTypeProvider);

		readConfig(ormConfigGroup);
	}

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
	}
}
