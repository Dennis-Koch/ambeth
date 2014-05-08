package de.osthus.ambeth.relations.many.lazy.fk.forward.none;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import de.osthus.ambeth.relations.AbstractRelationsTest;
import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.util.IPrefetchHandle;
import de.osthus.ambeth.util.IPrefetchHelper;

@SQLData("Relations_data.sql")
@SQLStructure("Relations_structure.sql")
@TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/relations/many/lazy/fk/forward/none/orm.xml")
public class ManyLazyFkForwardNoneRelationsTest extends AbstractRelationsTest
{
	@Test
	public void testRetrieve()
	{
		EntityB entityB11 = cache.getObject(EntityB.class, 11);
		assertNotNull(entityB11);
		assertNotNull(entityB11.getEntityA());

		EntityB entityB12 = cache.getObject(EntityB.class, 12);
		assertNotNull(entityB12);
		assertNull(entityB12.getEntityA());
	}

	@Test
	public void testUpdateParent()
	{
		EntityB entityB = cache.getObject(EntityB.class, 11);
		entityB.setName(entityB.getName() + ".2");
		relationsService.save(entityB);
	}

	@Test
	public void testUpdateChild()
	{
		EntityB entityB = cache.getObject(EntityB.class, 11);
		EntityA entityA = entityB.getEntityA();
		String newName = entityA.getName() + ".2";
		entityA.setName(newName);
		relationsService.save(entityB);

		entityA = cache.getObject(EntityA.class, entityA.getId());
		assertEquals(newName, entityA.getName());
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
	public void testPrefetchLazy()
	{
		EntityB entityB = cache.getObject(EntityB.class, 11);

		String propertyName = "EntityA";
		assertBeforePrefetch(entityB, propertyName);
		IPrefetchHandle prefetch = beanContext.getService(IPrefetchHelper.class).createPrefetch().add(EntityB.class, propertyName).build();
		prefetch.prefetch(entityB);

		assertAfterPrefetch(entityB, propertyName);
		Assert.assertNotNull(entityB.getEntityA().getName());
	}

	@Test
	public void testMultipleAdd()
	{
		List<EntityB> entityBs = cache.getObjects(EntityB.class, 12, 13);
		EntityB entityB11 = cache.getObject(EntityB.class, 11);
		EntityA entityA = entityB11.getEntityA();

		for (EntityB entityB : entityBs)
		{
			entityB.setEntityA(entityA);
			relationsService.save(entityB);
		}
	}
}
