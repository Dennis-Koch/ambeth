package de.osthus.ambeth.cache;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.EnumSet;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.model.Material;
import de.osthus.ambeth.model.MaterialGroup;
import de.osthus.ambeth.model.Unit;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.service.IMaterialService;
import de.osthus.ambeth.service.TestServicesModule;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.util.ParamChecker;

@TestModule({ TestServicesModule.class })
@SQLStructure("../persistence/jdbc/JDBCDatabase_structure.sql")
@SQLData("../persistence/jdbc/Example_data.sql")
@TestPropertiesList({ @TestProperties(name = PersistenceConfigurationConstants.DatabaseTablePrefix, value = "D_"),
		@TestProperties(name = PersistenceConfigurationConstants.DatabaseFieldPrefix, value = "F_"),
		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "orm.xml") })
public class RootCacheInvalidationTest extends AbstractPersistenceTest
{
	protected ICacheContext cacheContext;

	protected ICacheFactory cacheFactory;

	protected ICache fixture;

	protected IMaterialService materialService;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(cacheContext, "CacheContext");
		ParamChecker.assertNotNull(cacheFactory, "CacheFactory");
		ParamChecker.assertNotNull(materialService, "MaterialService");
		ParamChecker.assertNotNull(fixture, "Fixture");
	}

	public void setCacheContext(ICacheContext cacheContext)
	{
		this.cacheContext = cacheContext;
	}

	public void setCacheFactory(ICacheFactory cacheFactory)
	{
		this.cacheFactory = cacheFactory;
	}

	public void setFixture(ICache fixture)
	{
		this.fixture = fixture;
	}

	public void setMaterialService(IMaterialService materialService)
	{
		this.materialService = materialService;
	}

	@Test
	public void testRootCacheDataChangePerformance() throws Throwable
	{
		final IDisposableCache cache = cacheFactory.create(CacheFactoryDirective.SubscribeGlobalDCE, "test");
		cacheContext.executeWithCache(cache, new ISingleCacheRunnable<Object>()
		{
			@Override
			public Object run() throws Throwable
			{
				MaterialGroup mg = cache.getObject(MaterialGroup.class, "pl");
				Unit unit = cache.getObject(Unit.class, (long) 1);
				IList<Material> materials = new ArrayList<Material>();
				for (int a = 100; a-- > 0;)
				{
					Material material = entityFactory.createEntity(Material.class);
					material.setName("new material");
					material.setMaterialGroup(mg);
					material.setUnit(unit);
					materials.add(material);
				}
				materialService.updateMaterials(materials.toArray(Material.class));
				for (int a = materials.size(); a-- > 0;)
				{
					Material material = materials.get(a);
					material.setName(material.getName() + "2");
				}
				materialService.updateMaterials(materials.toArray(Material.class));
				return null;
			}
		});
	}

	@Test
	public void testRootCacheInvalidation()
	{
		MaterialGroup mg = fixture.getObject(MaterialGroup.class, "pl");
		Unit unit = fixture.getObject(Unit.class, (long) 1);
		rootCacheInvalidation(mg, unit, false);
	}

	@Test
	public void testRootCacheInvalidation2() throws Throwable
	{
		final IDisposableCache cache = cacheFactory.create(CacheFactoryDirective.SubscribeTransactionalDCE, "test");
		cacheContext.executeWithCache(cache, new ISingleCacheRunnable<Object>()
		{
			@Override
			public Object run() throws Throwable
			{
				MaterialGroup mg = cache.getObject(MaterialGroup.class, "pl");
				Unit unit = cache.getObject(Unit.class, (long) 1);
				rootCacheInvalidation(mg, unit, false);
				return null;
			}
		});
	}

	protected void rootCacheInvalidation(MaterialGroup mg, Unit unit, boolean mustBeNull)
	{
		assertNotNull(mg);
		assertNotNull(unit);

		Material material = entityFactory.createEntity(Material.class);
		material.setName("new material");
		material.setMaterialGroup(mg);
		material.setUnit(unit);
		materialService.updateMaterial(material);

		Object hardRef = fixture.getObject(new ObjRef(Material.class, material.getId(), null), CacheDirective.cacheValueResult());
		assertNotNull(hardRef);

		Object object = fixture.getObject(new ObjRef(Material.class, material.getId(), null),
				EnumSet.of(CacheDirective.FailInCacheHierarchy, CacheDirective.CacheValueResult));

		assertNotNull(object);
		Assert.assertSame(object, hardRef);

		material.setName("updated material");
		materialService.updateMaterial(material);

		Object object2 = fixture.getObject(new ObjRef(Material.class, material.getId(), null),
				EnumSet.of(CacheDirective.FailInCacheHierarchy, CacheDirective.CacheValueResult));

		if (mustBeNull)
		{
			assertNull(object2);
		}
		else
		{
			assertNotNull(object2);
		}
	}
}