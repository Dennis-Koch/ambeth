package com.koch.ambeth.persistence.jdbc.mapping;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.mapping.IMapperService;
import com.koch.ambeth.mapping.IMapperServiceFactory;
import com.koch.ambeth.merge.IMergeProcess;
import com.koch.ambeth.model.Material;
import com.koch.ambeth.model.MaterialGroup;
import com.koch.ambeth.model.Unit;
import com.koch.ambeth.persistence.jdbc.mapping.MapperPerformanceTest.MapperTestModule;
import com.koch.ambeth.persistence.jdbc.mapping.models.OneToManyEntityService;
import com.koch.ambeth.persistence.jdbc.mapping.models.SelfReferencingEntityService;
import com.koch.ambeth.service.IMaterialService;
import com.koch.ambeth.service.MaterialService;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.SQLDataRebuild;
import com.koch.ambeth.testutil.SQLStructure;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.testutil.category.PerformanceTests;
import com.koch.ambeth.transfer.MaterialComplexVO;
import com.koch.ambeth.transfer.MaterialGroupVO;
import com.koch.ambeth.transfer.UnitVO;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.HashMap;

@Category(PerformanceTests.class)
@SQLStructure("Mapper_structure.sql")
@SQLDataRebuild
@TestModule(MapperTestModule.class)
@TestPropertiesList({
		@TestProperties(name = ServiceConfigurationConstants.GenericTransferMapping, value = "true"),
		@TestProperties(name = ServiceConfigurationConstants.mappingFile,
				value = MapperPerformanceTest.basePath + "orm.xml"),
		@TestProperties(name = ServiceConfigurationConstants.valueObjectFile,
				value = MapperPerformanceTest.basePath + "value-object.xml")})
public class MapperPerformanceTest extends AbstractInformationBusWithPersistenceTest {
	public static final String basePath = "com/koch/ambeth/persistence/jdbc/mapping/";

	public static class MapperTestModule implements IInitializingModule {
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
			beanContextFactory.registerAutowireableBean(IMaterialService.class, MaterialService.class);
			beanContextFactory.registerAutowireableBean(ISelfReferencingEntityService.class,
					SelfReferencingEntityService.class);
			beanContextFactory.registerAutowireableBean(IOneToManyEntityService.class,
					OneToManyEntityService.class);
		}
	}

	private IMapperServiceFactory mapperServiceFactory;

	private IMapperService fixture;

	private HashMap<String, Object> buidToIdMap = new HashMap<String, Object>();

	int count = 500, innerCount = 10;

	@Override
	public void afterPropertiesSet() throws Throwable {
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(mapperServiceFactory, "MapperServiceFactory");
	}

	public void setMapperServiceFactory(IMapperServiceFactory mapperServiceFactory) {
		this.mapperServiceFactory = mapperServiceFactory;
	}

	@Before
	public void setUp() throws Exception {
		fixture = mapperServiceFactory.create();

		List<Material> objects = new ArrayList<Material>();
		for (int a = count; a-- > 0;) {
			MaterialGroup materialGroup = entityFactory.createEntity(MaterialGroup.class);
			materialGroup.setBuid("MaterialGroup" + a);
			materialGroup.setName(materialGroup.getBuid());
			for (int b = innerCount; b-- > 0;) {
				Material material = entityFactory.createEntity(Material.class);
				Unit unit = entityFactory.createEntity(Unit.class);
				unit.setBuid("Unit" + (a * innerCount + b));
				unit.setName(unit.getBuid());

				material.setBuid("Material" + (a * innerCount + b));
				material.setName(material.getBuid());
				material.setMaterialGroup(materialGroup);
				material.setUnit(unit);
				objects.add(material);
			}
		}
		beanContext.getService(IMergeProcess.class).process(objects, null, null, null);
		beanContext.getService(IEventDispatcher.class).dispatchEvent(ClearAllCachesEvent.getInstance());
		for (int a = objects.size(); a-- > 0;) {
			Material material = objects.get(a);
			map(material.getBuid(), material.getId());
			map(material.getMaterialGroup().getBuid(), material.getMaterialGroup().getId());
			map(material.getUnit().getBuid(), material.getUnit().getId());
		}
	}

	protected void map(String buid, Object id) {
		Object existingId = buidToIdMap.get(buid);
		if (existingId != null && !existingId.equals(id)) {
			throw new IllegalStateException("Must never happen");
		}
		buidToIdMap.put(buid, id);
	}

	protected <T> T readMap(String buid, Class<T> requestedType) {
		Object id = buidToIdMap.get(buid);
		if (id == null) {
			throw new IllegalStateException("Must never happen");
		}
		return conversionHelper.convertValueToType(requestedType, id);
	}

	@After
	public void tearDown() throws Exception {
		fixture = null;
	}

	@Test
	public void testMultipleMapperInstances() {
		List<MaterialComplexVO> materialVOs = new ArrayList<MaterialComplexVO>();
		String change = "_2";
		for (int a = count; a-- > 0;) {
			MaterialGroupVO materialGroup = new MaterialGroupVO();
			materialGroup.setBuid("MaterialGroup" + a);
			materialGroup.setId(readMap(materialGroup.getBuid(), String.class));
			materialGroup.setName(materialGroup.getBuid() + change);
			for (int b = innerCount; b-- > 0;) {
				MaterialComplexVO material = new MaterialComplexVO();
				UnitVO unit = new UnitVO();
				unit.setBuid("Unit" + (a * innerCount + b));
				unit.setId(readMap(unit.getBuid(), String.class));
				unit.setName(unit.getBuid() + change);

				material.setBuid("Material" + (a * innerCount + b));
				material.setId(readMap(unit.getBuid(), Integer.class));
				material.setName(material.getBuid() + change);
				material.setMaterialGroup(materialGroup);
				material.setUnit(unit);
				materialVOs.add(material);
			}
		}
		long start = System.currentTimeMillis();

		List<Material> materials = fixture.mapToBusinessObjectList(materialVOs);

		long end = System.currentTimeMillis();
		Assert.assertEquals(materialVOs.size(), materials.size());
		Assert.assertTrue("Mapping needed " + (end - start) + "ms which is more than allowed (5000ms)",
				(end - start) < 5000);
	}
}
