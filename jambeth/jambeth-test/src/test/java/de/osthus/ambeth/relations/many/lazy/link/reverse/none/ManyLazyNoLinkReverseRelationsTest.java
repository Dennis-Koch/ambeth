package de.osthus.ambeth.relations.many.lazy.link.reverse.none;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.cache.model.IObjRelationResult;
import de.osthus.ambeth.cache.transfer.ObjRelation;
import de.osthus.ambeth.ioc.CacheModule;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.relations.AbstractRelationsTest;
import de.osthus.ambeth.service.ICacheRetriever;
import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.util.IPrefetchHandle;
import de.osthus.ambeth.util.IPrefetchHelper;

@SQLData("Relations_data.sql")
@SQLStructure("Relations_structure.sql")
@TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/relations/many/lazy/link/reverse/none/orm.xml")
public class ManyLazyNoLinkReverseRelationsTest extends AbstractRelationsTest
{
	@Test
	public void testRetrieve()
	{
		EntityB entityB = cache.getObject(EntityB.class, 11);
		assertNotNull(entityB);
		assertEquals(2, entityB.getEntityAs().size());
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

		EntityA entityA = entityFactory.createEntity(EntityA.class);
		entityA.setName("new EntityA");
		entityB.getEntityAs().add(entityA);
		relationsService.save(entityB);

		assertTrue(entityA.getId() != 0);

		EntityA cachedEntityA = cache.getObject(EntityA.class, entityA.getId());
		assertNotNull(cachedEntityA);
	}

	@Test
	public void testAddRelated()
	{
		EntityB entityB = cache.getObject(EntityB.class, 11);

		EntityA entityA = entityFactory.createEntity(EntityA.class);
		entityA.setName("new EntityA");
		relationsService.save(entityA);

		assertTrue(entityA.getId() != 0);

		entityB.getEntityAs().add(entityA);
		relationsService.save(entityB);

		EntityA cachedEntityA = cache.getObject(EntityA.class, entityA.getId());
		assertNotNull(cachedEntityA);
	}

	@Test
	public void testRemoveRelated()
	{
		EntityB entityB = cache.getObject(EntityB.class, 11);

		entityB.getEntityAs().remove(0);
		relationsService.save(entityB);

		EntityB cachedEntityB = cache.getObject(EntityB.class, 11);
		assertEquals(1, cachedEntityB.getEntityAs().size());
	}

	@Test
	public void testDeleteRelated()
	{
		EntityB entityB = cache.getObject(EntityB.class, 11);

		EntityA entityA = entityB.getEntityAs().get(0);
		int id = entityA.getId();
		relationsService.delete(entityA);

		EntityA cachedEntityA = cache.getObject(EntityA.class, id);
		assertNull(cachedEntityA);

		EntityB cachedEntityB = cache.getObject(EntityB.class, 11);
		assertEquals(1, cachedEntityB.getEntityAs().size());
	}

	@Test
	public void testDeleteParent()
	{
		EntityB entityB = cache.getObject(EntityB.class, 11);

		EntityA entityA = entityB.getEntityAs().get(0);
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
		EntityB entityB = cache.getObject(EntityB.class, 12);

		String propertyName = "EntityAs";
		assertBeforePrefetch(entityB, propertyName);
		IPrefetchHandle prefetch = beanContext.getService(IPrefetchHelper.class).createPrefetch().add(EntityB.class, propertyName).build();
		prefetch.prefetch(entityB);

		assertAfterPrefetch(entityB, propertyName);
		Assert.assertEquals(0, entityB.getEntityAs().size());
	}

	@Test
	public void testGetRelations()
	{
		ICacheRetriever cacheRetriever = beanContext.getService(CacheModule.DEFAULT_CACHE_RETRIEVER, ICacheRetriever.class);

		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(EntityB.class);
		byte idIndex = metaData.getIdIndexByMemberName("Name");

		EntityB entityB = cache.getObject(EntityB.class, 11);
		IObjRef[] objRefs = { new ObjRef(EntityB.class, idIndex, entityB.getName(), entityB.getVersion()) };
		List<IObjRelation> objRelations = Collections.<IObjRelation> singletonList(new ObjRelation(objRefs, "EntityAs"));

		List<IObjRelationResult> actual = cacheRetriever.getRelations(objRelations);
		assertNotNull(actual);
	}
}
