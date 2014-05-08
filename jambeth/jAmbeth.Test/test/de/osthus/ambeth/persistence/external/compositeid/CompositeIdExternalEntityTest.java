package de.osthus.ambeth.persistence.external.compositeid;import org.junit.Assert;import org.junit.Test;import de.osthus.ambeth.cache.CacheDirective;import de.osthus.ambeth.cache.CacheRetrieverFake;import de.osthus.ambeth.cache.ICache;import de.osthus.ambeth.compositeid.ICompositeIdFactory;import de.osthus.ambeth.event.EntityMetaDataAddedEvent;import de.osthus.ambeth.event.IEventListenerExtendable;import de.osthus.ambeth.ioc.BytecodeModule;import de.osthus.ambeth.ioc.CacheBytecodeModule;import de.osthus.ambeth.ioc.CacheDataChangeModule;import de.osthus.ambeth.ioc.CacheModule;import de.osthus.ambeth.ioc.CompositeIdModule;import de.osthus.ambeth.ioc.EventDataChangeModule;import de.osthus.ambeth.ioc.EventModule;import de.osthus.ambeth.ioc.EventServerModule;import de.osthus.ambeth.ioc.IInitializingModule;import de.osthus.ambeth.ioc.MergeModule;import de.osthus.ambeth.ioc.ObjectCopierModule;import de.osthus.ambeth.ioc.ServiceModule;import de.osthus.ambeth.ioc.annotation.FrameworkModule;import de.osthus.ambeth.ioc.config.IBeanConfiguration;import de.osthus.ambeth.ioc.extendable.ExtendableBean;import de.osthus.ambeth.ioc.factory.IBeanContextFactory;import de.osthus.ambeth.merge.IEntityFactory;import de.osthus.ambeth.merge.IEntityMetaDataExtendable;import de.osthus.ambeth.merge.IEntityMetaDataProvider;import de.osthus.ambeth.merge.IValueObjectConfigExtendable;import de.osthus.ambeth.merge.IndependentEntityMetaDataClient;import de.osthus.ambeth.merge.MergeServiceRegistry;import de.osthus.ambeth.merge.NoopMergeService;import de.osthus.ambeth.merge.ValueObjectMap;import de.osthus.ambeth.merge.config.IndependentEntityMetaDataReader;import de.osthus.ambeth.merge.config.ValueObjectConfigReader;import de.osthus.ambeth.merge.model.IEntityMetaData;import de.osthus.ambeth.merge.transfer.ObjRef;import de.osthus.ambeth.orm.IOrmXmlReaderExtendable;import de.osthus.ambeth.orm.IOrmXmlReaderRegistry;import de.osthus.ambeth.orm.OrmXmlReader20;import de.osthus.ambeth.orm.OrmXmlReaderLegathy;import de.osthus.ambeth.persistence.external.compositeid.CompositeIdExternalEntityTest.CompositeIdExternalEntityTestModule;import de.osthus.ambeth.service.ICacheRetrieverExtendable;import de.osthus.ambeth.service.IMergeService;import de.osthus.ambeth.service.IMergeServiceExtendable;import de.osthus.ambeth.service.config.ConfigurationConstants;import de.osthus.ambeth.testutil.AbstractIocTest;import de.osthus.ambeth.testutil.TestFrameworkModule;import de.osthus.ambeth.testutil.TestProperties;import de.osthus.ambeth.typeinfo.IRelationProvider;import de.osthus.ambeth.typeinfo.RelationProvider;import de.osthus.ambeth.util.IPrintable;import de.osthus.ambeth.util.ParamChecker;import de.osthus.ambeth.util.XmlConfigUtil;import de.osthus.ambeth.util.xml.IXmlConfigUtil;@TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/persistence/external/compositeid/external_orm.xml")@TestFrameworkModule({ CompositeIdExternalEntityTestModule.class, BytecodeModule.class, CacheModule.class, CacheDataChangeModule.class,		CompositeIdModule.class, CacheBytecodeModule.class, EventModule.class, EventServerModule.class, EventDataChangeModule.class, MergeModule.class,		ObjectCopierModule.class, ServiceModule.class })public class CompositeIdExternalEntityTest extends AbstractIocTest{	@FrameworkModule	public static class CompositeIdExternalEntityTestModule implements IInitializingModule	{		@Override		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable		{			beanContextFactory.registerBean(CacheModule.DEFAULT_CACHE_RETRIEVER, CacheRetrieverFake.class);			IBeanConfiguration bc = beanContextFactory.registerAnonymousBean(CompositeIdEntityCacheRetriever.class);			beanContextFactory.link(bc).to(ICacheRetrieverExtendable.class).with(CompositeIdEntity.class);			beanContextFactory.link(bc).to(ICacheRetrieverExtendable.class).with(CompositeIdEntity2.class);			IBeanConfiguration valueObjectMap = beanContextFactory.registerAnonymousBean(ValueObjectMap.class);			beanContextFactory.registerBean("independantMetaDataProvider", IndependentEntityMetaDataClient.class).propertyRef("ValueObjectMap", valueObjectMap)					.autowireable(IEntityMetaDataProvider.class, IValueObjectConfigExtendable.class, IEntityMetaDataExtendable.class);			beanContextFactory.registerBean("valueObjectConfigReader", ValueObjectConfigReader.class);			beanContextFactory.link("valueObjectConfigReader").to(IEventListenerExtendable.class).with(EntityMetaDataAddedEvent.class);			beanContextFactory.registerBean("ormXmlReader", ExtendableBean.class).propertyValue(ExtendableBean.P_PROVIDER_TYPE, IOrmXmlReaderRegistry.class)					.propertyValue(ExtendableBean.P_EXTENDABLE_TYPE, IOrmXmlReaderExtendable.class)					.propertyRef(ExtendableBean.P_DEFAULT_BEAN, "ormXmlReaderLegathy").autowireable(IOrmXmlReaderRegistry.class, IOrmXmlReaderExtendable.class);			beanContextFactory.registerBean("ormXmlReaderLegathy", OrmXmlReaderLegathy.class);			beanContextFactory.registerBean("ormXmlReader 2.0", OrmXmlReader20.class);			beanContextFactory.link("ormXmlReader 2.0").to(IOrmXmlReaderExtendable.class).with(OrmXmlReader20.ORM_XML_NS);			beanContextFactory.registerBean("xmlConfigUtil", XmlConfigUtil.class).autowireable(IXmlConfigUtil.class);			beanContextFactory.registerBean("relationProvider", RelationProvider.class).autowireable(IRelationProvider.class);			beanContextFactory.registerBean(MergeModule.INDEPENDENT_META_DATA_READER, IndependentEntityMetaDataReader.class);			beanContextFactory.registerBean("mergeService", NoopMergeService.class);			beanContextFactory.registerBean("mergeServiceRegistry", MergeServiceRegistry.class).propertyRefs("mergeService")					.autowireable(IMergeService.class, IMergeServiceExtendable.class);		}	}	protected ICache cache;	protected ICompositeIdFactory compositeIdFactory;	protected IEntityFactory entityFactory;	protected IEntityMetaDataProvider entityMetaDataProvider;	@Override	public void afterPropertiesSet() throws Throwable	{		super.afterPropertiesSet();		ParamChecker.assertNotNull(cache, "cache");		ParamChecker.assertNotNull(compositeIdFactory, "compositeIdFactory");		ParamChecker.assertNotNull(entityFactory, "entityFactory");		ParamChecker.assertNotNull(entityMetaDataProvider, "entityMetaDataProvider");	}	public void setCache(ICache cache)	{		this.cache = cache;	}	public void setCompositeIdFactory(ICompositeIdFactory compositeIdFactory)	{		this.compositeIdFactory = compositeIdFactory;	}	public void setEntityFactory(IEntityFactory entityFactory)	{		this.entityFactory = entityFactory;	}	public void setEntityMetaDataProvider(IEntityMetaDataProvider entityMetaDataProvider)	{		this.entityMetaDataProvider = entityMetaDataProvider;	}	@Test	public void testCompositeIdBehaviorEquals() throws Exception	{		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(CompositeIdEntity.class);		Object left = compositeIdFactory.createCompositeId(metaData, metaData.getIdMember(), CompositeIdEntityCacheRetriever.id1_2_data[0],				CompositeIdEntityCacheRetriever.id1_2_data[1]);		Object right = compositeIdFactory.createCompositeId(metaData, metaData.getIdMember(), CompositeIdEntityCacheRetriever.id1_2_data[0],				CompositeIdEntityCacheRetriever.id1_2_data[1]);		Assert.assertNotNull(left);		Assert.assertNotNull(right);		Assert.assertNotSame(left, right);		Assert.assertEquals(left, right);		Assert.assertTrue(left instanceof IPrintable);		StringBuilder sb = new StringBuilder();		((IPrintable) left).toString(sb);		Assert.assertEquals(left.toString(), sb.toString());	}	@Test	public void testCompositeIdBehaviorNotEqual() throws Exception	{		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(CompositeIdEntity.class);		Object left = compositeIdFactory.createCompositeId(metaData, metaData.getIdMember(), CompositeIdEntityCacheRetriever.id1_2_data[0],				CompositeIdEntityCacheRetriever.id1_2_data[1]);		Object right = compositeIdFactory.createCompositeId(metaData, metaData.getIdMember(),				((Number) CompositeIdEntityCacheRetriever.id1_2_data[0]).intValue() + 2, CompositeIdEntityCacheRetriever.id1_2_data[1]);		Assert.assertNotNull(left);		Assert.assertNotNull(right);		Assert.assertNotSame(left, right);		Assert.assertFalse(left.equals(right));		int idIndex = 0;		Object right2 = compositeIdFactory.createCompositeId(metaData, metaData.getAlternateIdMembers()[idIndex],				CompositeIdEntityCacheRetriever.id1_2_data[4], ((Number) CompositeIdEntityCacheRetriever.id1_2_data[3]).shortValue() + 2);		Assert.assertNotNull(right2);		Assert.assertNotSame(left, right2);		Assert.assertFalse(left.equals(right2));	}	@Test	public void testPrimaryId() throws Exception	{		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(CompositeIdEntity.class);		Object compositeId = compositeIdFactory.createCompositeId(metaData, metaData.getIdMember(), CompositeIdEntityCacheRetriever.id1_2_data[0],				CompositeIdEntityCacheRetriever.id1_2_data[1]);		CompositeIdEntity entity = cache.getObject(CompositeIdEntity.class, compositeId);		Assert.assertNotNull(entity);		Assert.assertEquals(CompositeIdEntityCacheRetriever.id1_2_data[0], entity.getId1());		Assert.assertEquals(CompositeIdEntityCacheRetriever.id1_2_data[1], entity.getId2());		Assert.assertEquals(CompositeIdEntityCacheRetriever.id1_2_data[2], entity.getName());		Assert.assertEquals(CompositeIdEntityCacheRetriever.id1_2_data[3], entity.getAid1());		Assert.assertEquals(CompositeIdEntityCacheRetriever.id1_2_data[4], entity.getAid2());	}	@Test	public void testAlternateId() throws Exception	{		int idIndex = 0;		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(CompositeIdEntity.class);		Object compositeId = compositeIdFactory.createCompositeId(metaData, metaData.getAlternateIdMembers()[idIndex],				CompositeIdEntityCacheRetriever.id1_2_data[4], CompositeIdEntityCacheRetriever.id1_2_data[3]);		CompositeIdEntity entity = (CompositeIdEntity) cache.getObject(new ObjRef(CompositeIdEntity.class, (byte) idIndex, compositeId, null),				CacheDirective.none());		Assert.assertNotNull(entity);		Assert.assertEquals(CompositeIdEntityCacheRetriever.id1_2_data[0], entity.getId1());		Assert.assertEquals(CompositeIdEntityCacheRetriever.id1_2_data[1], entity.getId2());		Assert.assertEquals(CompositeIdEntityCacheRetriever.id1_2_data[2], entity.getName());		Assert.assertEquals(CompositeIdEntityCacheRetriever.id1_2_data[3], entity.getAid1());		Assert.assertEquals(CompositeIdEntityCacheRetriever.id1_2_data[4], entity.getAid2());	}	@Test	public void testPrimaryIdEmbedded() throws Exception	{		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(CompositeIdEntity2.class);		Object compositeId = compositeIdFactory.createCompositeId(metaData, metaData.getIdMember(), CompositeIdEntityCacheRetriever.id1_2_data[0],				CompositeIdEntityCacheRetriever.id1_2_data[1]);		CompositeIdEntity2 entity = cache.getObject(CompositeIdEntity2.class, compositeId);		Assert.assertNotNull(entity);		Assert.assertEquals(CompositeIdEntityCacheRetriever.entity2_id1_2_data[0], entity.getId1());		Assert.assertEquals(CompositeIdEntityCacheRetriever.entity2_id1_2_data[1], entity.getId2().getSid());		Assert.assertEquals(CompositeIdEntityCacheRetriever.entity2_id1_2_data[2], entity.getName());		Assert.assertEquals(CompositeIdEntityCacheRetriever.entity2_id1_2_data[3], entity.getAid1());		Assert.assertEquals(CompositeIdEntityCacheRetriever.entity2_id1_2_data[4], entity.getAid2().getSid());	}	@Test	public void testAlternateIdEmbedded() throws Exception	{		int idIndex = 0;		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(CompositeIdEntity2.class);		Object compositeId = compositeIdFactory.createCompositeId(metaData, metaData.getAlternateIdMembers()[idIndex],				CompositeIdEntityCacheRetriever.entity2_id1_2_data[4], CompositeIdEntityCacheRetriever.entity2_id1_2_data[3]);		CompositeIdEntity2 entity = (CompositeIdEntity2) cache.getObject(new ObjRef(CompositeIdEntity2.class, (byte) idIndex, compositeId, null),				CacheDirective.none());		Assert.assertNotNull(entity);		Assert.assertEquals(CompositeIdEntityCacheRetriever.entity2_id1_2_data[0], entity.getId1());		Assert.assertEquals(CompositeIdEntityCacheRetriever.entity2_id1_2_data[1], entity.getId2().getSid());		Assert.assertEquals(CompositeIdEntityCacheRetriever.entity2_id1_2_data[2], entity.getName());		Assert.assertEquals(CompositeIdEntityCacheRetriever.entity2_id1_2_data[3], entity.getAid1());		Assert.assertEquals(CompositeIdEntityCacheRetriever.entity2_id1_2_data[4], entity.getAid2().getSid());	}}