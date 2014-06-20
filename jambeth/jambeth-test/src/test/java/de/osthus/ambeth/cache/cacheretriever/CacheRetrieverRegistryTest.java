package de.osthus.ambeth.cache.cacheretriever;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import de.osthus.ambeth.cache.CacheDirective;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.cacheretriever.CacheRetrieverRegistryTest.CacheRetrieverRegistryTestModule;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.service.ICacheRetrieverExtendable;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.util.ParamChecker;

@TestModule(CacheRetrieverRegistryTestModule.class)
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = CacheRetrieverRegistryTest.basePath + "orm.xml;"
		+ CacheRetrieverRegistryTest.basePath + "external-orm.xml")
@SQLData("CacheRetrieverRegistry_data.sql")
@SQLStructure("CacheRetrieverRegistry_structure.sql")
public class CacheRetrieverRegistryTest extends AbstractPersistenceTest
{
	public static final String basePath = "de/osthus/ambeth/cache/cacheretriever/";

	@FrameworkModule
	public static class CacheRetrieverRegistryTestModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerBean("cacheRetrieverExternal", CacheRetrieverExternalFake.class);
			beanContextFactory.link("cacheRetrieverExternal").to(ICacheRetrieverExtendable.class).with(ExternalEntity.class);
			beanContextFactory.link("cacheRetrieverExternal").to(ICacheRetrieverExtendable.class).with(ExternalEntity2.class);
		}
	}

	protected ICache cache;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(cache, "cache");
	}

	public void setCache(ICache cache)
	{
		this.cache = cache;
	}

	@Test
	public void testGetEntityById() throws Throwable
	{
		int id = 1;
		String name = "one";
		ExternalEntity actual = cache.getObject(ExternalEntity.class, id);
		assertNotNull(actual);
		assertEquals(id, actual.getId());
		assertEquals(name, actual.getName());
		assertEquals(name.length(), actual.getValue());
		assertNull(actual.getParent());

		id = 104578;
		name = "one oh four five seven eight";
		actual = cache.getObject(ExternalEntity.class, 104578);
		assertNotNull(actual);
		assertEquals(id, actual.getId());
		assertEquals(name, actual.getName());
		assertEquals(name.length(), actual.getValue());
		assertEquals(25, actual.getParent().getId());
	}

	@Test
	public void testGetEntityByAlternateId() throws Throwable
	{
		int id = 152;
		String name = "one five two";
		IObjRef objRef = new ObjRef(ExternalEntity.class, (byte) 0, name, null);
		ExternalEntity actual = (ExternalEntity) cache.getObject(objRef, Collections.<CacheDirective> emptySet());
		assertNotNull(actual);
		assertEquals(id, actual.getId());
		assertEquals(name, actual.getName());
		assertEquals(name.length(), actual.getValue());
		assertEquals(8, actual.getParent().getId());

		id = 717;
		name = "seven one seven";
		objRef = new ObjRef(ExternalEntity.class, (byte) 0, name, null);
		actual = (ExternalEntity) cache.getObject(objRef, Collections.<CacheDirective> emptySet());
		assertNotNull(actual);
		assertEquals(id, actual.getId());
		assertEquals(name, actual.getName());
		assertEquals(name.length(), actual.getValue());
		assertEquals(15, actual.getParent().getId());
	}

	@Test
	public void testParent() throws Throwable
	{
		ExternalEntity actual = cache.getObject(ExternalEntity.class, 853);
		assertNotNull(actual);
		ExternalEntity parent = actual.getParent();

		int id = 16;
		String name = "one six";
		assertNotNull(parent);
		assertEquals(id, parent.getId());
		assertEquals(name, parent.getName());
		assertEquals(name.length(), parent.getValue());
		assertEquals(7, parent.getParent().getId());
	}

	@Test
	public void testSecondEntity() throws Throwable
	{
		int id = 83;
		String name = "eight three";
		ExternalEntity2 actual = cache.getObject(ExternalEntity2.class, id);
		assertNotNull(actual);
		assertEquals(id, actual.getId());
		assertEquals(name, actual.getName());
		assertEquals(name.length(), actual.getValue());

		id = 11;
		name = "one one";
		ExternalEntity parent = actual.getParent();
		assertNotNull(parent);
		assertEquals(id, parent.getId());
		assertEquals(name, parent.getName());
		assertEquals(name.length(), parent.getValue());
		assertEquals(2, parent.getParent().getId());
	}

	@Test
	public void testLocalEntityWithExternalToOneRelationFK() throws Throwable
	{
		int id = 893;
		String name = "LocalEntity " + id;
		LocalEntity actual = cache.getObject(LocalEntity.class, id);
		assertNotNull(actual);
		assertEquals(id, actual.getId());
		assertEquals(name, actual.getName());
		assertEquals(name.length(), actual.getValue());

		id = 20;
		name = "two oh";
		ExternalEntity2 parent = actual.getParent();
		assertNotNull(parent);
		assertEquals(id, parent.getId());
		assertEquals(name, parent.getName());
		assertEquals(name.length(), parent.getValue());
		assertEquals(2, parent.getParent().getId());
	}

	@Test
	public void testLocalEntityWithExternalToOneRelationLinkTable() throws Throwable
	{
		int id = 893;
		String name = "LocalEntity " + id;
		LocalEntity actual = cache.getObject(LocalEntity.class, id);
		assertNotNull(actual);
		assertEquals(id, actual.getId());
		assertEquals(name, actual.getName());
		assertEquals(name.length(), actual.getValue());

		id = 42;
		name = "four two";
		ExternalEntity sibling = actual.getSibling();
		assertNotNull(sibling);
		assertEquals(id, sibling.getId());
		assertEquals(name, sibling.getName());
		assertEquals(name.length(), sibling.getValue());
		assertEquals(6, sibling.getParent().getId());
	}

	@Test
	public void testLocalEntityWithExternalToManyRelationLinkTable() throws Throwable
	{
		int id = 893;
		LocalEntity actual = cache.getObject(LocalEntity.class, id);

		Set<Integer> expectedIds = new HashSet<Integer>(Arrays.asList(new Integer[] { 893, 1234, 9 }));
		Set<ExternalEntity> externals = actual.getExternals();
		assertNotNull(externals);
		assertEquals(expectedIds.size(), externals.size());
		for (ExternalEntity external : externals)
		{
			assertTrue(expectedIds.contains(external.getId()));
		}
	}

	// TODO to-one with external storage

	// TODO to-many with external storage
}
