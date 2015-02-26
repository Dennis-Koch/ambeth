package de.osthus.ambeth.persistence.jdbc.mapping;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.ICacheContext;
import de.osthus.ambeth.cache.ICacheProvider;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.mapping.IDedicatedMapperExtendable;
import de.osthus.ambeth.mapping.IMapperService;
import de.osthus.ambeth.mapping.IMapperServiceFactory;
import de.osthus.ambeth.model.Material;
import de.osthus.ambeth.model.MaterialGroup;
import de.osthus.ambeth.model.Unit;
import de.osthus.ambeth.persistence.jdbc.mapping.MapperTest.MapperTestModule;
import de.osthus.ambeth.persistence.jdbc.mapping.models.OneToManyEntity;
import de.osthus.ambeth.persistence.jdbc.mapping.models.OneToManyEntityListType;
import de.osthus.ambeth.persistence.jdbc.mapping.models.OneToManyEntityRefListType;
import de.osthus.ambeth.persistence.jdbc.mapping.models.OneToManyEntityService;
import de.osthus.ambeth.persistence.jdbc.mapping.models.OneToManyEntityVO;
import de.osthus.ambeth.persistence.jdbc.mapping.models.SelfReferencingEntity;
import de.osthus.ambeth.persistence.jdbc.mapping.models.SelfReferencingEntityService;
import de.osthus.ambeth.persistence.jdbc.mapping.models.SelfReferencingEntityVO;
import de.osthus.ambeth.persistence.jdbc.mapping.models.StringListType;
import de.osthus.ambeth.service.IMaterialService;
import de.osthus.ambeth.service.MaterialService;
import de.osthus.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.transfer.MaterialSmallVO;
import de.osthus.ambeth.transfer.MaterialVO;

@SQLData("Mapper_data.sql")
@SQLStructure("Mapper_structure.sql")
@TestModule(MapperTestModule.class)
@TestPropertiesList({
		@TestProperties(name = ServiceConfigurationConstants.GenericTransferMapping, value = "true"),
		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = MapperTest.basePath + "orm.xml;" + MapperTest.basePath + "orm2.xml"),
		@TestProperties(name = ServiceConfigurationConstants.valueObjectFile, value = MapperTest.basePath + "value-object.xml;" + MapperTest.basePath
				+ "value-object2.xml") })
public class MapperTest extends AbstractInformationBusWithPersistenceTest
{
	public static final String basePath = "de/osthus/ambeth/persistence/jdbc/mapping/";

	public static class MapperTestModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerAutowireableBean(IMaterialService.class, MaterialService.class);
			beanContextFactory.registerAutowireableBean(ISelfReferencingEntityService.class, SelfReferencingEntityService.class);
			beanContextFactory.registerAutowireableBean(IOneToManyEntityService.class, OneToManyEntityService.class);

			beanContextFactory.registerBean("oneToManyEntityMapper", OneToManyEntityMapper.class);
			beanContextFactory.link("oneToManyEntityMapper").to(IDedicatedMapperExtendable.class).with(OneToManyEntity.class);
		}
	}

	@Autowired
	protected IMapperServiceFactory mapperServiceFactory;

	private IMapperService fixture;

	@Before
	public void setUp() throws Exception
	{
		fixture = mapperServiceFactory.create();
	}

	@After
	public void tearDown() throws Exception
	{
		fixture.dispose();
		fixture = null;
	}

	@Test
	public void testMapperServiceFactory() throws Exception
	{
		Assert.assertNotNull(fixture);
	}

	@Test
	public void testNewEntity()
	{
		OneToManyEntity expected = entityFactory.createEntity(OneToManyEntity.class);
		expected.setName("testNewEntity");
		expected.setNeedsSpecialMapping(new Date());

		OneToManyEntityVO actualVO = fixture.mapToValueObject(expected, OneToManyEntityVO.class);
		assertNotNull(actualVO);
		assertEquals(0, actualVO.getId());
		assertEquals(0, expected.getId());
		assertEquals(expected.getName(), actualVO.getName());
		assertEquals(expected.getNeedsSpecialMapping().getTime(), (long) actualVO.getNeedsSpecialMapping());

		OneToManyEntity actual = fixture.mapToBusinessObject(actualVO);
		assertNotNull(actual);
		assertEquals(0, actual.getId());
		assertEquals(0, actualVO.getId());
		assertEquals(expected.getName(), actual.getName());
		assertNotNull(actual.getNeedsSpecialMapping());
		assertEquals(expected.getNeedsSpecialMapping(), actual.getNeedsSpecialMapping());
	}

	@Test
	public void testNewEntityWithRelation()
	{
		OneToManyEntity expected = entityFactory.createEntity(OneToManyEntity.class);
		expected.setName("testNewEntity");
		OneToManyEntity child = entityFactory.createEntity(OneToManyEntity.class);
		child.setName("testChildEntity");
		child.setNeedsSpecialMapping(new Date());
		expected.getByListType().add(child);

		OneToManyEntityVO actualVO = fixture.mapToValueObject(expected, OneToManyEntityVO.class);
		assertNotNull(actualVO);
		assertEquals(0, actualVO.getId());
		assertEquals(0, expected.getId());
		assertEquals(expected.getName(), actualVO.getName());
		assertEquals(0., actualVO.getNeedsSpecialMapping(), Double.MIN_NORMAL);
		assertNotNull(actualVO.getByListType());
		assertFalse(actualVO.getByListType().getOneToManyEntities().isEmpty());
		OneToManyEntityVO childVO = actualVO.getByListType().getOneToManyEntities().get(0);
		assertEquals(child.getName(), childVO.getName());
		assertEquals(child.getNeedsSpecialMapping().getTime(), (long) childVO.getNeedsSpecialMapping());

		OneToManyEntity actual = fixture.mapToBusinessObject(actualVO);
		assertNotNull(actual);
		assertEquals(0, actual.getId());
		assertEquals(0, actualVO.getId());
		assertEquals(expected.getName(), actual.getName());
		assertEquals(expected.getNeedsSpecialMapping(), actual.getNeedsSpecialMapping());
		assertNotNull(actual.getByListType());
		assertFalse(actual.getByListType().isEmpty());
		OneToManyEntity childMapped = actual.getByListType().get(0);
		assertEquals(child.getName(), childMapped.getName());
		assertEquals(child.getNeedsSpecialMapping(), childMapped.getNeedsSpecialMapping());
	}

	@Test
	public void testExistingEntity()
	{
		OneToManyEntity expected = entityFactory.createEntity(OneToManyEntity.class);
		expected.setName("testNewEntity");
		IOneToManyEntityService service = beanContext.getService(IOneToManyEntityService.class);
		service.updateOneToManyEntity(expected);

		OneToManyEntityVO actualVO = fixture.mapToValueObject(expected, OneToManyEntityVO.class);
		assertNotNull(actualVO);
		assertEquals(expected.getId(), actualVO.getId());
		assertEquals(expected.getName(), actualVO.getName());
		assertEquals(expected.getCreatedBy(), actualVO.getCreatedBy());
		assertEquals(expected.getCreatedOn(), actualVO.getCreatedOn());

		// To test to not set technical attributes to defaults
		actualVO.setCreatedBy(null);
		actualVO.setCreatedOn(0);

		OneToManyEntity actual = fixture.mapToBusinessObject(actualVO);
		assertNotNull(actual);
		assertEquals(actualVO.getId(), actual.getId());
		assertEquals(expected.getName(), actual.getName());
		assertEquals(expected.getCreatedBy(), actual.getCreatedBy());
		assertTrue((expected.getCreatedOn() - actual.getCreatedOn()) < 1000);
	}

	@Test
	public void testExistingEntity_resetVersion()
	{
		OneToManyEntity expected = entityFactory.createEntity(OneToManyEntity.class);
		expected.setName("testNewEntity");
		IOneToManyEntityService service = beanContext.getService(IOneToManyEntityService.class);
		service.updateOneToManyEntity(expected);

		OneToManyEntityVO actualVO = fixture.mapToValueObject(expected, OneToManyEntityVO.class);
		assertNotNull(actualVO);

		// To test to not set technical attributes to defaults
		actualVO.setVersion((short) 0);
		actualVO.setCreatedBy(null);
		actualVO.setCreatedOn(0);

		OneToManyEntity actual = fixture.mapToBusinessObject(actualVO);
		assertNotNull(actual);
		// If no version is give we must not set the current one, otherwise optimistic-locking would not work
		assertEquals(actualVO.getVersion(), actual.getVersion());
	}

	@Test
	@Ignore
	public void testExistingEntity2() throws Throwable
	{
		ICacheContext cacheContext = beanContext.getService(ICacheContext.class);
		ICacheProvider cacheProvider = beanContext.getService(ICacheProvider.class);

		final ICache cache = cacheProvider.getCurrentCache();
		cacheContext.executeWithCache(cache, new IResultingBackgroundWorkerDelegate<Object>()
		{
			@Override
			public Object invoke() throws Throwable
			{
				OneToManyEntity expected = entityFactory.createEntity(OneToManyEntity.class);
				expected.setName("testNewEntity");
				expected.setBuid("buid");
				IOneToManyEntityService service = beanContext.getService(IOneToManyEntityService.class);
				service.updateOneToManyEntity(expected);

				OneToManyEntityVO actualVO = new OneToManyEntityVO();
				// actualVO.setId(expected.getId());
				actualVO.setVersion(expected.getVersion());
				actualVO.setName(expected.getName());
				// actualVO.setName(expected.getName() + "_VO");

				OneToManyEntity actual;

				IMapperService mapperService = mapperServiceFactory.create();
				try
				{
					actual = mapperService.mapToBusinessObject(actualVO);
				}
				finally
				{
					mapperService.dispose();
				}
				Assert.assertSame(expected, actual);

				OneToManyEntityListType listTypeVO = new OneToManyEntityListType();
				listTypeVO.getOneToManyEntities().add(actualVO);

				List<Object> listBO;
				mapperService = mapperServiceFactory.create();
				try
				{
					listBO = fixture.mapToBusinessObjectListFromListType(listTypeVO);
				}
				finally
				{
					mapperService.dispose();
				}
				Assert.assertEquals(1, listBO.size());
				Assert.assertSame(expected, listBO.get(0));

				List<Object> listVO = new ArrayList<Object>();
				listVO.add(actualVO);

				mapperService = mapperServiceFactory.create();
				try
				{
					listBO = fixture.mapToBusinessObjectList(listVO);
				}
				finally
				{
					mapperService.dispose();
				}
				Assert.assertEquals(1, listBO.size());
				Assert.assertSame(expected, listBO.get(0));

				return null;
			}
		});
	}

	@Test
	public void testTwoNewEntities()
	{
		OneToManyEntity expected1 = entityFactory.createEntity(OneToManyEntity.class);
		expected1.setName("testTwoNewEntities 1");
		OneToManyEntity expected2 = entityFactory.createEntity(OneToManyEntity.class);
		expected2.setName("testTwoNewEntities 2");
		List<OneToManyEntity> expectedList = Arrays.asList(new OneToManyEntity[] { expected1, expected2 });

		List<OneToManyEntityVO> actualVOs = fixture.mapToValueObjectList(expectedList, OneToManyEntityVO.class);
		assertEquals(expectedList.size(), actualVOs.size());
		for (int i = 0; i < expectedList.size(); i++)
		{
			OneToManyEntity expected = expectedList.get(i);
			OneToManyEntityVO actualVO = actualVOs.get(i);
			assertEquals(0, actualVO.getId());
			assertEquals(0, expected.getId());
			assertEquals(expectedList.get(i).getName(), actualVOs.get(i).getName());
		}

		List<OneToManyEntity> actualList = fixture.mapToBusinessObjectList(actualVOs);
		assertEquals(expectedList.size(), actualList.size());
		for (int i = 0; i < expectedList.size(); i++)
		{
			OneToManyEntity expected = expectedList.get(i);
			OneToManyEntity actual = actualList.get(i);
			assertEquals(0, expected.getId());
			assertEquals(0, actual.getId());
			assertEquals(expected.getName(), actual.getName());
		}
	}

	@Test
	public void testNewEntityWithRelations()
	{
		OneToManyEntity parent = entityFactory.createEntity(OneToManyEntity.class);
		parent.setName("testNewEntityWithRelations");
		OneToManyEntity child1 = entityFactory.createEntity(OneToManyEntity.class);
		child1.setName("child1");
		OneToManyEntity child2 = entityFactory.createEntity(OneToManyEntity.class);
		child2.setName("child2");
		List<OneToManyEntity> expectedChildren = Arrays.asList(new OneToManyEntity[] { child1, child2 });
		parent.getOneToManyEntities().add(child1);
		parent.getOneToManyEntities().add(child2);
		SelfReferencingEntity selfRefEntity = entityFactory.createEntity(SelfReferencingEntity.class);
		selfRefEntity.setName("selfRefEntity");
		parent.getSelfReferencingEntities().add(selfRefEntity);

		List<OneToManyEntity> expected = new ArrayList<OneToManyEntity>(expectedChildren);
		int parentIndex = expected.size();
		expected.add(parent);

		List<OneToManyEntityVO> actualVOs = fixture.mapToValueObjectList(expected, OneToManyEntityVO.class);
		OneToManyEntityVO actualVO = actualVOs.get(parentIndex);

		assertNotNull(actualVO);
		assertEquals(0, actualVO.getId());
		assertEquals(0, parent.getId());
		assertEquals(parent.getName(), actualVO.getName());
		List<String> actualVOChildren = actualVO.getOneToManyEntities();
		assertEquals(expectedChildren.size(), actualVOChildren.size());
		for (int i = 0; i < expectedChildren.size(); i++)
		{
			assertEquals(0, expectedChildren.get(i).getId());
			assertEquals(expectedChildren.get(i).getName(), actualVOChildren.get(i));
		}
		List<SelfReferencingEntityVO> actualVOSelfRefEntities = actualVO.getSelfReferencingEntities();
		assertEquals(parent.getSelfReferencingEntities().size(), actualVOSelfRefEntities.size());
		for (int i = 0; i < actualVOSelfRefEntities.size(); i++)
		{
			assertEquals(0, actualVOSelfRefEntities.get(i).getId());
		}

		List<OneToManyEntity> actuals = fixture.mapToBusinessObjectList(actualVOs);
		OneToManyEntity actual = actuals.get(parentIndex);

		assertNotNull(actual);
		assertEquals(0, actual.getId());
		assertEquals(0, actualVO.getId());
		assertEquals(parent.getName(), actual.getName());
		List<OneToManyEntity> actualChildren = actual.getOneToManyEntities();
		assertEquals(expectedChildren.size(), actualChildren.size());
		for (int i = 0; i < expectedChildren.size(); i++)
		{
			assertEquals(0, actualChildren.get(i).getId());
			assertEquals(expectedChildren.get(i).getName(), actualChildren.get(i).getName());
		}
		Set<SelfReferencingEntity> actualSelfRefEntities = actual.getSelfReferencingEntities();
		assertEquals(parent.getSelfReferencingEntities().size(), actualSelfRefEntities.size());
		Iterator<SelfReferencingEntity> iter = actualSelfRefEntities.iterator();
		while (iter.hasNext())
		{
			assertEquals(0, iter.next().getId());
		}
	}

	@Test
	public void mapToVO() throws Exception
	{
		IMaterialService materialService = beanContext.getService(IMaterialService.class);

		Material material = entityFactory.createEntity(Material.class);
		MaterialGroup mg = entityFactory.createEntity(MaterialGroup.class);
		mg.setBuid("mapToBO MG");
		material.setMaterialGroup(mg);
		Unit unit = entityFactory.createEntity(Unit.class);
		unit.setBuid("mapToBO Unit");
		material.setUnit(unit);
		material.setName("TestName");
		material.setBuid("myBuid");
		materialService.updateMaterial(material);

		MaterialVO copy = fixture.mapToValueObject(material, MaterialVO.class);
		Assert.assertEquals(material.getName(), copy.getName());
		Assert.assertEquals(material.getBuid(), copy.getBuid());
		Assert.assertEquals(material.getMaterialGroup().getBuid(), copy.getMaterialGroup());
		Assert.assertEquals(material.getUnit().getBuid(), copy.getUnit());
	}

	@Test
	public void mapToBO() throws Exception
	{
		OneToManyEntityVO otmVo = new OneToManyEntityVO();

		OneToManyEntity otmBo = fixture.mapToBusinessObject(otmVo);
		Assert.assertNotNull(otmBo);
	}

	@Test
	public void mapToVOToBO() throws Exception
	{
		IMaterialService materialService = beanContext.getService(IMaterialService.class);

		Material material = entityFactory.createEntity(Material.class);
		material.setName("TestName");
		material.setBuid("matBuid1");

		MaterialGroup matGroup = entityFactory.createEntity(MaterialGroup.class);
		matGroup.setName("theMatGroup");
		matGroup.setBuid("matGroupBuid1");
		material.setMaterialGroup(matGroup);

		Unit unit = entityFactory.createEntity(Unit.class);
		unit.setName("unitName");
		unit.setBuid("unitBuid1");
		material.setUnit(unit);

		materialService.updateMaterial(material);

		MaterialVO copyVO = fixture.mapToValueObject(material, MaterialVO.class);
		Assert.assertEquals("matBuid1", copyVO.getBuid());
		Assert.assertEquals("TestName", copyVO.getName());
		Assert.assertEquals("unitBuid1", copyVO.getUnit());
		Assert.assertEquals("matGroupBuid1", copyVO.getMaterialGroup());

		Material copyBO = fixture.mapToBusinessObject(copyVO);
		Assert.assertEquals("matBuid1", copyBO.getBuid());
		Assert.assertEquals("TestName", copyBO.getName());
		Assert.assertNotNull(copyBO.getUnit());
		Assert.assertNotNull(copyBO.getMaterialGroup());
		Assert.assertEquals("unitBuid1", copyBO.getUnit().getBuid());
		Assert.assertEquals("matGroupBuid1", copyBO.getMaterialGroup().getBuid());

		// This will fail because the childcache generates a clone.
		assertProxyEquals(matGroup, copyBO.getMaterialGroup());

		// This will work, because the childcache references always the same clone.
		MaterialVO copyVO2 = fixture.mapToValueObject(copyBO, MaterialVO.class);
		Material copyBO2 = fixture.mapToBusinessObject(copyVO2);
		assertProxyEquals(copyBO.getMaterialGroup(), copyBO2.getMaterialGroup());
	}

	@Test
	public void mapToSmallVOToBO() throws Exception
	{
		IMaterialService materialService = beanContext.getService(IMaterialService.class);

		Material material = entityFactory.createEntity(Material.class);
		material.setName("TestName");
		material.setBuid("matBuid1");

		MaterialGroup matGroup = entityFactory.createEntity(MaterialGroup.class);
		matGroup.setName("theMatGroup");
		matGroup.setBuid("matGroupBuid1");
		material.setMaterialGroup(matGroup);

		Unit unit = entityFactory.createEntity(Unit.class);
		unit.setName("unitName");
		unit.setBuid("unitBuid1");
		material.setUnit(unit);

		materialService.updateMaterial(material);

		MaterialSmallVO copyVO = fixture.mapToValueObject(material, MaterialSmallVO.class);
		Assert.assertEquals("matBuid1", copyVO.getBuid());

		Material copyBO = fixture.mapToBusinessObject(copyVO);
		Assert.assertEquals("matBuid1", copyBO.getBuid());
		Assert.assertEquals("TestName", copyBO.getName());
		Assert.assertNotNull("getUnit() null", copyBO.getUnit());
		Assert.assertNotNull("getMaterialGroup() null", copyBO.getMaterialGroup());
		Assert.assertEquals("unitBuid1", copyBO.getUnit().getBuid());
		Assert.assertEquals("matGroupBuid1", copyBO.getMaterialGroup().getBuid());

		// This will fail because the childcache generates a clone.
		assertProxyEquals(matGroup, copyBO.getMaterialGroup());

		// This will work, because the childcache references always the same clone.
		MaterialVO copyVO2 = fixture.mapToValueObject(copyBO, MaterialVO.class);
		Material copyBO2 = fixture.mapToBusinessObject(copyVO2);
		assertProxyEquals(copyBO.getMaterialGroup(), copyBO2.getMaterialGroup());
	}

	@Test
	public void cyclicReferences() throws Exception
	{
		ISelfReferencingEntityService entityService = beanContext.getService(ISelfReferencingEntityService.class);

		SelfReferencingEntity ent1 = entityFactory.createEntity(SelfReferencingEntity.class);
		ent1.setName("ent1");
		SelfReferencingEntity ent2 = entityFactory.createEntity(SelfReferencingEntity.class);
		ent2.setName("ent2");
		SelfReferencingEntity ent3 = entityFactory.createEntity(SelfReferencingEntity.class);
		ent3.setName("ent3");
		SelfReferencingEntity ent4 = entityFactory.createEntity(SelfReferencingEntity.class);
		ent4.setName("ent4");

		ent1.setRelation1(ent1);
		ent1.setRelation2(ent2);
		ent2.setRelation1(ent1);
		ent2.setRelation2(ent3);
		ent3.setRelation1(ent3);
		ent3.setRelation2(ent4);
		ent4.setRelation1(ent1);
		ent4.setRelation2(ent2);

		entityService.updateSelfReferencingEntity(ent1);
		entityService.updateSelfReferencingEntity(ent2);
		entityService.updateSelfReferencingEntity(ent3);
		entityService.updateSelfReferencingEntity(ent4);
		SelfReferencingEntityVO ent1VO = fixture.mapToValueObject(ent1, SelfReferencingEntityVO.class);
		SelfReferencingEntityVO ent2VO = fixture.mapToValueObject(ent2, SelfReferencingEntityVO.class);
		SelfReferencingEntityVO ent3VO = fixture.mapToValueObject(ent3, SelfReferencingEntityVO.class);
		SelfReferencingEntityVO ent4VO = fixture.mapToValueObject(ent4, SelfReferencingEntityVO.class);

		SelfReferencingEntity ent1BO = fixture.mapToBusinessObject(ent1VO);
		SelfReferencingEntity ent2BO = fixture.mapToBusinessObject(ent2VO);
		SelfReferencingEntity ent3BO = fixture.mapToBusinessObject(ent3VO);
		SelfReferencingEntity ent4BO = fixture.mapToBusinessObject(ent4VO);

		assertProxyEquals(ent1BO, ent1BO.getRelation1());
		assertProxyEquals(ent2BO, ent1BO.getRelation2());
		assertProxyEquals(ent1BO, ent2BO.getRelation1());
		assertProxyEquals(ent3BO, ent2BO.getRelation2());
		assertProxyEquals(ent3BO, ent3BO.getRelation1());
		assertProxyEquals(ent4BO, ent3BO.getRelation2());
		assertProxyEquals(ent1BO, ent4BO.getRelation1());
		assertProxyEquals(ent2BO, ent4BO.getRelation2());
	}

	@Test
	public void oneToMany()
	{
		IOneToManyEntityService oneToManyService = beanContext.getService(IOneToManyEntityService.class);

		OneToManyEntity[] originals = new OneToManyEntity[5];
		for (int i = originals.length; i-- > 0;)
		{
			originals[i] = entityFactory.createEntity(OneToManyEntity.class);
			originals[i].setName("o2mEnt" + (i + 1));
		}
		originals[2].getMyEmbedded().setName("emb name");
		originals[2].getMyEmbedded().setValue(23);

		SelfReferencingEntity selfRefEnt1 = entityFactory.createEntity(SelfReferencingEntity.class);
		selfRefEnt1.setName("sreEnt1");
		selfRefEnt1.setRelation1(selfRefEnt1);
		selfRefEnt1.setRelation2(selfRefEnt1);
		SelfReferencingEntity selfRefEnt2 = entityFactory.createEntity(SelfReferencingEntity.class);
		selfRefEnt2.setName("sreEnt2");
		selfRefEnt2.setRelation1(selfRefEnt1);
		selfRefEnt2.setRelation2(selfRefEnt2);

		// Empty Collections
		originals[1].getOneToManyEntities().clear();
		originals[1].getSelfReferencingEntities().clear();
		// Basic Test
		originals[2].getOneToManyEntities().addAll(Arrays.asList(originals[0], originals[2], originals[3]));
		originals[2].getSelfReferencingEntities().addAll(Arrays.asList(selfRefEnt1, selfRefEnt2));
		// Self referencing
		originals[4].getOneToManyEntities().addAll(Arrays.asList(originals[4], originals[0]));
		originals[4].getSelfReferencingEntities().addAll(Arrays.asList(selfRefEnt1));

		for (int i = originals.length; i-- > 0;)
		{
			oneToManyService.updateOneToManyEntity(originals[i]);
		}

		OneToManyEntityVO[] vos = new OneToManyEntityVO[originals.length];
		for (int i = originals.length; i-- > 0;)
		{
			vos[i] = fixture.mapToValueObject(originals[i], OneToManyEntityVO.class);
		}

		for (int i = originals.length; i-- > 0;)
		{
			OneToManyEntity original = originals[i];
			OneToManyEntityVO vo = vos[i];
			assertNotNull(vo);
			assertEquals(original.getId(), vo.getId());
			assertEquals(original.getVersion(), vo.getVersion());
			assertEquals(original.getName(), vo.getName());
			if (original.getOneToManyEntities() != null)
			{
				if (original.getOneToManyEntities().isEmpty())
				{
					assertNotNull(vo.getOneToManyEntities());
					assertEquals(0, vo.getOneToManyEntities().size());
				}
				else
				{
					assertNotNull(vo.getOneToManyEntities());
					assertEquals(original.getOneToManyEntities().size(), vo.getOneToManyEntities().size());
					for (Object childId : vo.getOneToManyEntities())
					{
						Assert.assertTrue(childId instanceof String);
					}
				}
			}
			if (original.getSelfReferencingEntities() != null)
			{
				if (original.getSelfReferencingEntities().isEmpty())
				{
					assertNotNull(vo.getSelfReferencingEntities());
					assertEquals(0, vo.getSelfReferencingEntities().size());
				}
				else
				{
					assertNotNull(vo.getSelfReferencingEntities());
					assertEquals(original.getSelfReferencingEntities().size(), vo.getSelfReferencingEntities().size());
					for (Object childVO : vo.getSelfReferencingEntities())
					{
						Assert.assertTrue(childVO instanceof SelfReferencingEntityVO);
					}
				}
			}
		}
		assertEquals("emb name", vos[2].getMyEmbeddedType().getNameString());
		assertEquals(0, vos[2].getMyEmbeddedType().getValueNumber()); // default value, member is set to be ignored
	}

	@Test
	public void testListTypes()
	{
		OneToManyEntity[] originals = generateTestEnitiesWithListTypes("testListTypes");

		OneToManyEntityVO[] vos = new OneToManyEntityVO[originals.length];
		for (int i = originals.length; i-- > 0;)
		{
			vos[i] = fixture.mapToValueObject(originals[i], OneToManyEntityVO.class);
		}

		testVOsWithListTypes(originals, vos);

		OneToManyEntity[] copies = new OneToManyEntity[vos.length];
		for (int i = vos.length; i-- > 0;)
		{
			copies[i] = fixture.mapToBusinessObject(vos[i]);
		}

		testBOsWithListTypes(originals, copies);
	}

	@Test
	public void testRoundTripWithList()
	{
		List<OneToManyEntity> originals = Arrays.asList(generateTestEnitiesWithListTypes("OneList"));
		List<OneToManyEntity> originalsObj = new ArrayList<OneToManyEntity>(originals);

		List<OneToManyEntityVO> vos = fixture.mapToValueObjectList(originalsObj, OneToManyEntityVO.class);

		testVOsWithListTypes(originals, vos);

		OneToManyEntityRefListType refList = fixture.mapToValueObjectRefListType(originalsObj, OneToManyEntityRefListType.class);
		Assert.assertNotNull("RefList is null", refList);
		Assert.assertNotNull("RefList.BUID is null", refList.getBUID());
		Assert.assertEquals("RefList size is wrong", originalsObj.size(), refList.getBUID().size());
		for (int a = originalsObj.size(); a-- > 0;)
		{
			OneToManyEntity originalObj = originalsObj.get(a);
			Assert.assertEquals("Alternate id wrong", originalObj.getName(), refList.getBUID().get(a));
		}

		List<OneToManyEntity> copies = fixture.mapToBusinessObjectList(new ArrayList<Object>(vos));

		testBOsWithListTypes(originals, copies);
	}

	@Test
	public void testRoundTripWithListType()
	{
		List<OneToManyEntity> originals = Arrays.asList(generateTestEnitiesWithListTypes("OneListType"));
		List<Object> originalsObj = new ArrayList<Object>(originals);

		OneToManyEntityListType vos = fixture.mapToValueObjectListType(originalsObj, OneToManyEntityVO.class, OneToManyEntityListType.class);

		testVOsWithListTypes(originals, vos.getOneToManyEntities());

		List<OneToManyEntity> copies = fixture.mapToBusinessObjectListFromListType(vos);

		testBOsWithListTypes(originals, copies);
	}

	@Test
	public void testRoundTripWithChange_Simple()
	{
		MaterialVO original = new MaterialVO();
		original.setId(1);
		original.setBuid("test material");
		original.setVersion((short) 1);
		original.setName("test material");

		Material bo = fixture.mapToBusinessObject(original);
		bo.setName("test material 2");

		MaterialVO vo = fixture.mapToValueObject(bo, MaterialVO.class);
		assertEquals("test material 2", vo.getName());

		bo = fixture.mapToBusinessObject(original);
		bo.setName("test material 2");

		vo = fixture.mapToValueObject(bo, MaterialVO.class);
		assertEquals("test material 2", vo.getName());
	}

	@Test
	public void testPrimitiveCollections()
	{
		SelfReferencingEntity sre = entityFactory.createEntity(SelfReferencingEntity.class);
		sre.setName("testPrimitiveCollections");
		sre.setValues(new String[] { "value1", "value2", "value3" });
		Set<String> values2 = new HashSet<String>();
		values2.addAll(Arrays.asList("value2.1", "value2.2", "value2.3"));
		sre.setValues2(values2);
		sre.setRelation1(sre);

		SelfReferencingEntityVO sreVo = fixture.mapToValueObject(sre, SelfReferencingEntityVO.class);
		assertNotNull(sreVo);
		assertEquals(sre.getName(), sreVo.getName());
		List<String> voValues = sreVo.getValues();
		assertNotNull(voValues);
		assertArrayEquals(sre.getValues(), voValues.toArray(new String[voValues.size()]));
		StringListType voValues2List = sreVo.getValues2List();
		assertNotNull(voValues2List);
		List<String> voValues2 = voValues2List.getStrings();
		assertNotNull(voValues2);
		assertCollectionsSimilar(sre.getValues2(), voValues2);
		assertSame(sreVo.getName(), sreVo.getRelation1());
		assertNull(sreVo.getRelation2());

		SelfReferencingEntity sreBo = fixture.mapToBusinessObject(sreVo);
		assertNotNull(sreBo);
		assertEquals(sre.getName(), sreBo.getName());
		assertArrayEquals(sre.getValues(), sreBo.getValues());
		assertCollectionsSimilar(sre.getValues2(), sreBo.getValues2());
		assertProxyEquals(sreBo, sreBo.getRelation1());
		assertNull(sreBo.getRelation2());
	}

	@Test
	public void testMultipleMapperInstances()
	{
		OneToManyEntityVO expected1 = new OneToManyEntityVO();
		expected1.setName("NewEntity 1");

		OneToManyEntity actualVO1 = fixture.mapToBusinessObject(expected1);
		assertNotNull(actualVO1);

		IMapperService fixture2 = mapperServiceFactory.create();
		OneToManyEntityVO expected2 = new OneToManyEntityVO();
		expected2.setName("NewEntity 2");

		OneToManyEntity actualVO2 = fixture2.mapToBusinessObject(expected2);
		assertNotNull(actualVO2);
	}

	@Test
	public void testListOfString()
	{
		OneToManyEntity expected = entityFactory.createEntity(OneToManyEntity.class);
		expected.setName("NewEntity");
		expected.setNicknames(Arrays.asList("nick1", "nick2"));

		OneToManyEntityVO actualVO = fixture.mapToValueObject(expected, OneToManyEntityVO.class);
		assertNotNull(actualVO);
		assertNotNull(actualVO.getNicknames());
		assertEquals(expected.getNicknames().size(), actualVO.getNicknames().size());
		expected.getNicknames().containsAll(actualVO.getNicknames());

		OneToManyEntity actualBO = fixture.mapToBusinessObject(actualVO);
		assertNotNull(actualBO);
		assertNotNull(actualBO.getNicknames());
		assertEquals(expected.getNicknames().size(), actualBO.getNicknames().size());
		expected.getNicknames().containsAll(actualBO.getNicknames());
	}

	private OneToManyEntity[] generateTestEnitiesWithListTypes(String namePart)
	{
		IOneToManyEntityService oneToManyService = beanContext.getService(IOneToManyEntityService.class);

		OneToManyEntity[] originals = new OneToManyEntity[5];

		for (int i = originals.length; i-- > 0;)
		{
			originals[i] = entityFactory.createEntity(OneToManyEntity.class);
			originals[i].setName(namePart + " " + (i + 1));
		}
		for (int i = originals.length; i-- > 0;)
		{
			for (int j = 0; j < i; j++)
			{
				originals[i].getByListType().add(originals[j]);
			}
		}
		for (int i = originals.length; i-- > 0;)
		{
			for (int j = 0; j < i - 1; j++)
			{
				originals[i].getByRefListType().add(originals[j]);
			}
		}

		for (int i = originals.length; i-- > 0;)
		{
			oneToManyService.updateOneToManyEntity(originals[i]);
		}

		return originals;
	}

	private void testVOsWithListTypes(OneToManyEntity[] originals, OneToManyEntityVO[] vos)
	{
		testVOsWithListTypes(Arrays.asList(originals), Arrays.asList(vos));
	}

	private void testVOsWithListTypes(List<OneToManyEntity> originals, List<OneToManyEntityVO> vos)
	{
		for (int i = originals.size(); i-- > 0;)
		{
			OneToManyEntity original = originals.get(i);
			OneToManyEntityVO vo = vos.get(i);
			assertNotNull(vo);
			assertEquals(original.getId(), vo.getId());
			assertEquals(original.getVersion(), vo.getVersion());
			assertEquals(original.getName(), vo.getName());
			if (original.getByListType() != null)
			{
				assertNotNull(vo.getByListType());
				assertEquals(original.getByListType().size(), vo.getByListType().getOneToManyEntities().size());
				for (Object childVO : vo.getByListType().getOneToManyEntities())
				{
					Assert.assertTrue(childVO instanceof OneToManyEntityVO);
				}
			}
			if (original.getByRefListType() != null)
			{
				assertNotNull(vo.getByREFListType());
				if (original.getByRefListType().isEmpty())
				{
					assertNull(vo.getByREFListType().getBUID());
				}
				else
				{
					assertEquals(original.getByRefListType().size(), vo.getByREFListType().getBUID().size());
					for (Object childId : vo.getByREFListType().getBUID())
					{
						Assert.assertTrue(childId instanceof String);
					}
				}
			}
		}
	}

	private void testBOsWithListTypes(OneToManyEntity[] originals, OneToManyEntity[] copies)
	{
		testBOsWithListTypes(Arrays.asList(originals), Arrays.asList(copies));
	}

	private void testBOsWithListTypes(List<OneToManyEntity> originals, List<OneToManyEntity> copies)
	{
		for (int i = originals.size(); i-- > 0;)
		{
			OneToManyEntity original = originals.get(i);
			assertNotNull(copies.get(i));
			assertEquals(original.getId(), copies.get(i).getId());
			assertEquals(original.getVersion(), copies.get(i).getVersion());
			assertEquals(original.getName(), copies.get(i).getName());
			if (original.getByListType() != null)
			{
				assertNotNull(copies.get(i).getByListType());
				assertEquals(original.getByListType().size(), copies.get(i).getByListType().size());
				for (Object child : copies.get(i).getByListType())
				{
					Assert.assertTrue(child instanceof OneToManyEntity);
				}
				for (int j = original.getByListType().size(); j-- > 0;)
				{
					assertEquals(original.getByListType().get(j).getId(), copies.get(i).getByListType().get(j).getId());
				}
			}
			if (original.getByRefListType() != null)
			{
				assertNotNull(copies.get(i).getByRefListType());
				assertEquals(original.getByRefListType().size(), copies.get(i).getByRefListType().size());
				for (Object child : copies.get(i).getByRefListType())
				{
					Assert.assertTrue(child instanceof OneToManyEntity);
				}
				for (int j = original.getByRefListType().size(); j-- > 0;)
				{
					assertEquals(original.getByRefListType().get(j).getId(), copies.get(i).getByRefListType().get(j).getId());
				}
			}
		}
	}

	private void assertCollectionsSimilar(Collection<? extends Object> expected, Collection<? extends Object> actual)
	{
		assertEquals(expected.size(), actual.size());
		Iterator<? extends Object> iter = expected.iterator();
		while (iter.hasNext())
		{
			Object entry = iter.next();
			assertTrue(actual.contains(entry));
		}
	}
}
