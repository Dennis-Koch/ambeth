package de.osthus.ambeth.cache.directobjref;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.Arrays;

import org.junit.Test;

import de.osthus.ambeth.cache.CacheDirective;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.rootcachevalue.RootCacheValue;
import de.osthus.ambeth.cache.transfer.LoadContainer;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.merge.model.IDirectObjRef;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.DirectObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.model.Material;
import de.osthus.ambeth.service.ProcessServiceTestModule;
import de.osthus.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.util.ParamChecker;

@SQLStructure("ChildCacheDirectObjRefTest_structure.sql")
@SQLData("ChildCacheDirectObjRefTest_data.sql")
@TestModule(ProcessServiceTestModule.class)
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "orm.xml")
public class ChildCacheDirectObjRefTest extends AbstractInformationBusWithPersistenceTest
{
	protected ICache fixture;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(fixture, "cache");
	}

	public void setFixture(ICache fixture)
	{
		this.fixture = fixture;
	}

	@Test
	public void testGetObject_ObjRef_normal()
	{
		IObjRef ori = new ObjRef(Material.class, 1, 1);
		Material actual = (Material) fixture.getObject(ori, CacheDirective.none());
		assertNotNull(actual);
		assertEquals(1, actual.getId());
	}

	@Test
	public void testGetObject_ObjRef_CacheValue()
	{
		IObjRef ori = new ObjRef(Material.class, 1, 1);
		RootCacheValue actual = (RootCacheValue) fixture.getObject(ori, CacheDirective.cacheValueResult());
		assertNotNull(actual);
		assertEquals(1, actual.getId());
	}

	@Test
	public void testGetObject_ObjRef_LoadContainer()
	{
		IObjRef ori = new ObjRef(Material.class, 1, 1);
		LoadContainer actual = (LoadContainer) fixture.getObject(ori, CacheDirective.loadContainerResult());
		assertNotNull(actual);
		assertEquals(1, actual.getReference().getId());
	}

	@Test
	public void testGetObject_DirectObjRef_normal()
	{
		IDirectObjRef dori = getDORI();
		Material actual = (Material) fixture.getObject(dori, CacheDirective.none());
		assertNotNull(actual);
		assertSame(dori.getDirect(), actual);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetObject_DirectObjRef_CacheValue()
	{
		IObjRef dori = getDORI();
		fixture.getObject(dori, CacheDirective.cacheValueResult());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetObject_DirectObjRef_LoadContainer()
	{
		IObjRef dori = getDORI();
		fixture.getObject(dori, CacheDirective.loadContainerResult());
	}

	@Test
	public void testGetObjects_normal()
	{
		IObjRef ori = new ObjRef(Material.class, 1, 1);
		IDirectObjRef dori = getDORI();

		IList<Object> actuals = fixture.getObjects(Arrays.asList(ori, dori), CacheDirective.none());

		assertNotNull(actuals);
		assertEquals(2, actuals.size());

		Material actual = (Material) actuals.get(0);
		assertNotNull(actual);
		assertEquals(1, actual.getId());

		actual = (Material) actuals.get(1);
		assertNotNull(actual);
		assertSame(dori.getDirect(), actual);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetObjects_CacheValue()
	{
		IObjRef ori = new ObjRef(Material.class, 1, 1);
		IObjRef dori = getDORI();

		fixture.getObjects(Arrays.asList(ori, dori), CacheDirective.cacheValueResult());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetObjects_LoadContainer()
	{
		IObjRef ori = new ObjRef(Material.class, 1, 1);
		IObjRef dori = getDORI();

		fixture.getObjects(Arrays.asList(ori, dori), CacheDirective.loadContainerResult());
	}

	protected IDirectObjRef getDORI()
	{
		Material newMaterial = entityFactory.createEntity(Material.class);
		newMaterial.setBuid("direct buid");
		IDirectObjRef dori = new DirectObjRef(Material.class, newMaterial);
		return dori;
	}
}
