package de.osthus.ambeth.relations.one.fk.reverse.none;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import junit.framework.Assert;

import org.junit.Test;

import de.osthus.ambeth.relations.AbstractRelationsTest;
import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.util.IPrefetchHandle;
import de.osthus.ambeth.util.IPrefetchHelper;

@SQLData("OneNoReverseRelations_data.sql")
@SQLStructure("OneNoReverseRelations_structure.sql")
@TestPropertiesList(@TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/relations/one/fk/reverse/none/orm.xml"))
public abstract class AbstractOneNoReverseRelationsTest extends AbstractRelationsTest
{
	@Test
	public void testRetrieve()
	{
		EntityB entityB = cache.getObject(EntityB.class, 11);
		assertNotNull(entityB);
		assertNotNull(entityB.getEntityA());
	}

	@Test
	public void testUpdateParent()
	{
		EntityB entityB = cache.getObject(EntityB.class, 11);
		entityB.setName(entityB.getName() + ".2");
		relationsService.save(entityB);
	}

	@Test
	public void testCreateRelated()
	{
		EntityB entityB = cache.getObject(EntityB.class, 11);

		EntityA entityA = new EntityA();
		entityA.setName("new EntityA");
		entityB.setEntityA(entityA);
		relationsService.save(entityB);

		assertTrue(entityA.getId() != 0);

		EntityA cachedEntityA = cache.getObject(EntityA.class, entityA.getId());
		assertNotNull(cachedEntityA);
	}

	@Test
	public void testAddRelated()
	{
		EntityB entityB = cache.getObject(EntityB.class, 11);

		EntityA entityA = new EntityA();
		entityA.setName("new EntityA");
		relationsService.save(entityA);

		assertTrue(entityA.getId() != 0);

		entityB.setEntityA(entityA);
		relationsService.save(entityB);

		EntityA cachedEntityA = cache.getObject(EntityA.class, entityA.getId());
		assertNotNull(cachedEntityA);
	}

	@Test
	public void testRemoveRelated()
	{
		EntityB entityB = cache.getObject(EntityB.class, 11);

		entityB.setEntityA(null);
		relationsService.save(entityB);

		EntityB cachedEntityB = cache.getObject(EntityB.class, 11);
		assertNull(cachedEntityB.getEntityA());
	}

	@Test
	public void testDeleteRelated()
	{
		EntityB entityB = cache.getObject(EntityB.class, 11);

		EntityA entityA = entityB.getEntityA();
		int id = entityA.getId();
		relationsService.delete(entityA);

		EntityA cachedEntityA = cache.getObject(EntityA.class, id);
		assertNull(cachedEntityA);

		EntityB cachedEntityB = cache.getObject(EntityB.class, 11);
		assertNull(cachedEntityB.getEntityA());
	}

	@Test
	public void testDeleteParent()
	{
		EntityB entityB = cache.getObject(EntityB.class, 11);

		EntityA entityA = entityB.getEntityA();
		int id = entityA.getId();

		relationsService.delete(entityB);

		EntityA cachedEntityA = cache.getObject(EntityA.class, id);
		assertNotNull(cachedEntityA);
		EntityB cachedEntityB = cache.getObject(EntityB.class, 11);
		assertNull(cachedEntityB);
	}

	@Test
	public void testPrefetch()
	{
		String propertyName = "EntityA";
		EntityB entityB = cache.getObject(EntityB.class, 11);

		assertBeforePrefetch(entityB, propertyName);
		IPrefetchHandle prefetch = beanContext.getService(IPrefetchHelper.class).createPrefetch().add(EntityB.class, propertyName).build();
		prefetch.prefetch(entityB);

		assertAfterPrefetch(entityB, propertyName);
		Assert.assertNotNull(entityB.getEntityA().getName());
	}

	protected void assertBeforePrefetch(EntityB entityB, String propertyName)
	{
		Assert.assertTrue(Boolean.FALSE.equals(proxyHelper.isInitialized(entityB, propertyName)));
	}

	protected void assertAfterPrefetch(EntityB entityB, String propertyName)
	{
		Assert.assertTrue(Boolean.TRUE.equals(proxyHelper.isInitialized(entityB, propertyName)));
		Assert.assertNull(proxyHelper.getObjRefs(entityB, propertyName));
	}
}
