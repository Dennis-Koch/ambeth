package de.osthus.ambeth.relations.one.lazy.fk.forward.none;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.relations.AbstractRelationsTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.util.IPrefetchHandle;
import de.osthus.ambeth.util.IPrefetchHelper;

@SQLData("Relations_data.sql")
@SQLStructure("Relations_structure.sql")
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/relations/one/lazy/fk/forward/none/orm.xml")
public class OneLazyFkNoForwardRelationsTest extends AbstractRelationsTest
{
	protected static final int ENTITY_A_ID = 11;

	protected static final int ENTITY_B_ID = 1;

	@Test
	public void testRetrieve()
	{
		EntityB entityB = cache.getObject(EntityB.class, ENTITY_B_ID);
		assertNotNull(entityB);
		assertNotNull(entityB.getEntityA());
	}

	@Test
	public void testUpdateParent()
	{
		EntityB entityB = cache.getObject(EntityB.class, ENTITY_B_ID);
		entityB.setName(entityB.getName() + ".2");
		relationsService.save(entityB);
	}

	@Test
	public void testCreateRelated()
	{
		EntityB entityB = cache.getObject(EntityB.class, ENTITY_B_ID);

		EntityA entityA = entityFactory.createEntity(EntityA.class);
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
		EntityB entityB = cache.getObject(EntityB.class, ENTITY_B_ID);

		EntityA entityA = entityFactory.createEntity(EntityA.class);
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
		EntityB entityB = cache.getObject(EntityB.class, ENTITY_B_ID);

		entityB.setEntityA(null);
		relationsService.save(entityB);

		EntityB cachedEntityB = cache.getObject(EntityB.class, ENTITY_B_ID);
		assertNull(cachedEntityB.getEntityA());
	}

	@Test
	public void testDeleteRelated()
	{
		EntityB entityB = cache.getObject(EntityB.class, ENTITY_B_ID);

		EntityA entityA = entityB.getEntityA();
		int id = entityA.getId();
		relationsService.delete(entityA);

		EntityA cachedEntityA = cache.getObject(EntityA.class, id);
		assertNull(cachedEntityA);

		EntityB cachedEntityB = cache.getObject(EntityB.class, ENTITY_B_ID);
		assertNull(cachedEntityB.getEntityA());
	}

	@Test
	public void testDeleteParent()
	{
		EntityB entityB = cache.getObject(EntityB.class, ENTITY_B_ID);

		EntityA entityA = entityB.getEntityA();
		int id = entityA.getId();

		relationsService.delete(entityB);

		EntityA cachedEntityA = cache.getObject(EntityA.class, id);
		assertNotNull(cachedEntityA);
		EntityB cachedEntityB = cache.getObject(EntityB.class, ENTITY_B_ID);
		assertNull(cachedEntityB);
	}

	@Test
	public void testPrefetchLazy()
	{
		EntityB entityB = cache.getObject(EntityB.class, ENTITY_B_ID);

		String propertyName = "EntityA";
		assertBeforePrefetch(entityB, propertyName);
		IPrefetchHandle prefetch = beanContext.getService(IPrefetchHelper.class).createPrefetch().add(EntityB.class, propertyName).build();
		prefetch.prefetch(entityB);

		assertAfterPrefetch(entityB, propertyName);
		assertNotNull(entityB.getEntityA().getName());
	}
}
