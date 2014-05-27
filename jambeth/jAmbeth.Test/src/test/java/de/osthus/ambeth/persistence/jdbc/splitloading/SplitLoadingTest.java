package de.osthus.ambeth.persistence.jdbc.splitloading;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;

@SQLData("SplitLoading_data.sql")
@SQLStructure("SplitLoading_structure.sql")
@TestModule(TestServicesModule.class)
@TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/persistence/jdbc/splitloading/orm.xml")
public class SplitLoadingTest extends AbstractPersistenceTest
{
	@Test
	public void testDataSetup()
	{
		IRefEntityService service = beanContext.getService(IRefEntityService.class);
		ICache cache = beanContext.getService(ICache.class);

		int entityCount = 1001;

		List<RefEntity> entities = new ArrayList<RefEntity>(entityCount);
		RefEntity toSetLast = entityFactory.createEntity(RefEntity.class);
		RefEntity last = toSetLast;
		for (int i = 1; i < entityCount; i++)
		{
			RefEntity entity = entityFactory.createEntity(RefEntity.class);
			entity.setOther(last);
			entities.add(entity);
			last = entity;
		}
		toSetLast.setOther(last);
		entities.add(toSetLast);

		service.save(entities);

		List<Integer> ids = new ArrayList<Integer>(entityCount);
		for (RefEntity entity : entities)
		{
			ids.add(entity.getId());
		}

		List<RefEntity> actuals = cache.getObjects(RefEntity.class, ids);
		assertEquals(ids.size(), actuals.size());
		for (RefEntity actual : actuals)
		{
			assertNotNull(actual);
		}
	}
}
