package de.osthus.ambeth.bytecode;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.List;

import org.junit.Test;

import de.osthus.ambeth.cache.bytecode.EntityBytecodeTest.EntityBytecodeTestModule;
import de.osthus.ambeth.cache.model.ILoadContainer;
import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.cache.model.IObjRelationResult;
import de.osthus.ambeth.event.EntityMetaDataAddedEvent;
import de.osthus.ambeth.event.IEventListenerExtendable;
import de.osthus.ambeth.ioc.BytecodeModule;
import de.osthus.ambeth.ioc.CacheBytecodeModule;
import de.osthus.ambeth.ioc.CacheDataChangeModule;
import de.osthus.ambeth.ioc.CacheModule;
import de.osthus.ambeth.ioc.CompositeIdModule;
import de.osthus.ambeth.ioc.EventModule;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.MergeModule;
import de.osthus.ambeth.ioc.ServiceModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.extendable.ExtendableBean;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.merge.EntityMetaDataProvider;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.merge.IEntityMetaDataExtendable;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IValueObjectConfig;
import de.osthus.ambeth.merge.IValueObjectConfigExtendable;
import de.osthus.ambeth.merge.ValueObjectMap;
import de.osthus.ambeth.merge.config.IndependentEntityMetaDataReader;
import de.osthus.ambeth.merge.config.ValueObjectConfigReader;
import de.osthus.ambeth.merge.model.ICUDResult;
import de.osthus.ambeth.merge.model.IEntityLifecycleExtendable;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.model.IOriCollection;
import de.osthus.ambeth.model.IDataObject;
import de.osthus.ambeth.model.IMethodDescription;
import de.osthus.ambeth.objectcollector.ByteBuffer65536CollectableController;
import de.osthus.ambeth.objectcollector.ICollectableControllerExtendable;
import de.osthus.ambeth.orm.IOrmXmlReaderExtendable;
import de.osthus.ambeth.orm.IOrmXmlReaderRegistry;
import de.osthus.ambeth.orm.OrmXmlReader20;
import de.osthus.ambeth.orm.OrmXmlReaderLegathy;
import de.osthus.ambeth.service.ICacheRetriever;
import de.osthus.ambeth.service.IMergeService;
import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.testutil.AbstractIocTest;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.testutil.TestRebuildContext;
import de.osthus.ambeth.typeinfo.IRelationProvider;
import de.osthus.ambeth.typeinfo.RelationProvider;
import de.osthus.ambeth.util.XmlConfigUtil;
import de.osthus.ambeth.util.xml.IXmlConfigUtil;

@TestModule({ BytecodeModule.class, CacheModule.class, CacheBytecodeModule.class, CacheDataChangeModule.class, CompositeIdModule.class,
		EntityBytecodeTestModule.class, EventModule.class, MergeModule.class, ServiceModule.class })
@TestPropertiesList({ @TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/bytecode/ConstructorBytecodeTest-orm.xml") })
@TestRebuildContext
public class ConstructorBytecodeTest extends AbstractIocTest
{
	@FrameworkModule
	public static class EntityBytecodeTestModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerExternalBean(CacheModule.DEFAULT_CACHE_RETRIEVER, new ICacheRetriever()
			{
				@Override
				public List<IObjRelationResult> getRelations(List<IObjRelation> objRelations)
				{
					throw new UnsupportedOperationException();
				}

				@Override
				public List<ILoadContainer> getEntities(List<IObjRef> orisToLoad)
				{
					throw new UnsupportedOperationException();
				}
			});
			IBeanConfiguration valueObjectMap = beanContextFactory.registerAnonymousBean(ValueObjectMap.class);
			beanContextFactory
					.registerBean("independantMetaDataProvider", EntityMetaDataProvider.class)
					.propertyRef("ValueObjectMap", valueObjectMap)
					.autowireable(IEntityMetaDataProvider.class, IValueObjectConfigExtendable.class, IEntityLifecycleExtendable.class,
							IEntityMetaDataExtendable.class, EntityMetaDataProvider.class);
			beanContextFactory.registerBean(MergeModule.INDEPENDENT_META_DATA_READER, IndependentEntityMetaDataReader.class);
			beanContextFactory.registerBean("valueObjectConfigReader", ValueObjectConfigReader.class);
			beanContextFactory.link("valueObjectConfigReader").to(IEventListenerExtendable.class).with(EntityMetaDataAddedEvent.class);

			beanContextFactory.registerBean("ormXmlReader", ExtendableBean.class).propertyValue(ExtendableBean.P_PROVIDER_TYPE, IOrmXmlReaderRegistry.class)
					.propertyValue(ExtendableBean.P_EXTENDABLE_TYPE, IOrmXmlReaderExtendable.class)
					.propertyRef(ExtendableBean.P_DEFAULT_BEAN, "ormXmlReaderLegathy").autowireable(IOrmXmlReaderRegistry.class, IOrmXmlReaderExtendable.class);
			beanContextFactory.registerBean("ormXmlReaderLegathy", OrmXmlReaderLegathy.class);
			IBeanConfiguration ormXmlReader20BC = beanContextFactory.registerAnonymousBean(OrmXmlReader20.class);
			beanContextFactory.link(ormXmlReader20BC).to(IOrmXmlReaderExtendable.class).with(OrmXmlReader20.ORM_XML_NS);

			beanContextFactory.registerBean("xmlConfigUtil", XmlConfigUtil.class).autowireable(IXmlConfigUtil.class);
			beanContextFactory.registerBean("relationProvider", RelationProvider.class).autowireable(IRelationProvider.class);

			IBeanConfiguration byteBufferCC = beanContextFactory.registerAnonymousBean(ByteBuffer65536CollectableController.class);
			beanContextFactory.link(byteBufferCC).to(ICollectableControllerExtendable.class).with(ByteBuffer.class);

			beanContextFactory.registerExternalBean(new IMergeService()
			{
				@Override
				public IOriCollection merge(ICUDResult cudResult, IMethodDescription methodDescription)
				{
					throw new UnsupportedOperationException();
				}

				@Override
				public IValueObjectConfig getValueObjectConfig(Class<?> valueType)
				{
					throw new UnsupportedOperationException();
				}

				@Override
				public List<IEntityMetaData> getMetaData(List<Class<?>> entityTypes)
				{
					throw new UnsupportedOperationException();
				}
			}).autowireable(IMergeService.class);
		}
	}

	@Autowired
	protected IEntityFactory entityFactory;

	@Test
	public void testWithNonDefaultConstructor() throws Exception
	{
		TestEntityWithNonDefaultConstructor testEntity = entityFactory.createEntity(TestEntityWithNonDefaultConstructor.class);

		assertNotNull(testEntity);
	}

	@Test
	public void testIsToBeCreated() throws Exception
	{
		TestEntityWithNonDefaultConstructor testEntity = entityFactory.createEntity(TestEntityWithNonDefaultConstructor.class);

		assertTrue(testEntity instanceof IDataObject);
		assertTrue(((IDataObject) testEntity).hasPendingChanges());
	}
}
