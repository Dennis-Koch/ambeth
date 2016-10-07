package de.osthus.ambeth.persistence.xml;

import java.util.Arrays;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.XmlModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.merge.IMergeProcess;
import de.osthus.ambeth.persistence.xml.model.Group;
import de.osthus.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestFrameworkModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;

@SQLStructure("/de/osthus/ambeth/persistence/xml/BidirectionalRelations_structure.sql")
@TestPropertiesList({ @TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/persistence/xml/bidirectionalorm20.xml") })
@TestFrameworkModule(XmlModule.class)
public class BiDirectionalRelations20Test extends AbstractInformationBusWithPersistenceTest
{

	@Autowired
	protected IMergeProcess mergeProcess;

	@Autowired
	protected ICache cache;

	protected ArrayList<Group> createdGroups = new ArrayList<Group>();

	@After
	public void clearGroups()
	{
		if (!createdGroups.isEmpty())
		{
			mergeProcess.process(null, createdGroups, null, null);
			createdGroups.clear();
		}
	}

	protected Group createGroup(String name)
	{
		Group g = entityFactory.createEntity(Group.class);
		g.setName(name);
		createdGroups.add(g);
		return g;
	}

	@Test
	public void testSimpleChildSave_oneMerge()
	{
		Group g1 = createGroup("g1Name");
		Group g2 = createGroup("g2Name");

		g1.getChildGroups().add(g2);

		mergeProcess.process(Arrays.asList(g1, g2), null, null, null);

		checkSimpleChildSave(g1, g2);
	}

	@Test
	public void testSimpleChildSave_update()
	{
		Group g1 = createGroup("g1Name");
		Group g2 = createGroup("g2Name");

		mergeProcess.process(Arrays.asList(g1, g2), null, null, null);

		g1.getChildGroups().add(g2);
		mergeProcess.process(g1, null, null, null);

		checkSimpleChildSave(g1, g2);
	}

	@Test
	public void testTripleChildSave_oneMerge()
	{
		Group g1 = createGroup("g1Name");
		Group g2 = createGroup("g2Name");
		Group g3 = createGroup("g3Name");

		g1.getChildGroups().add(g2);
		g1.getChildGroups().add(g3);
		g2.getChildGroups().add(g3);

		mergeProcess.process(Arrays.asList(g1, g2, g3), null, null, null);

		checkTripleChildSave(g1, g2, g3);
	}

	@Test
	public void testTripleChildSave_update()
	{
		Group g1 = createGroup("g1Name");
		Group g2 = createGroup("g2Name");
		Group g3 = createGroup("g3Name");

		mergeProcess.process(Arrays.asList(g1, g2, g3), null, null, null);

		g1.getChildGroups().add(g2);
		g1.getChildGroups().add(g3);
		g2.getChildGroups().add(g3);
		mergeProcess.process(Arrays.asList(g1, g2), null, null, null);

		checkTripleChildSave(g1, g2, g3);
	}

	private void checkSimpleChildSave(Group g1, Group g2)
	{
		g1 = cache.getObject(Group.class, g1.getId());
		Assert.assertEquals(0, g1.getParentGroups().size());
		Assert.assertEquals(1, g1.getChildGroups().size());
		Assert.assertTrue(g1.getChildGroups().contains(g2));
		g2 = cache.getObject(Group.class, g2.getId());
		Assert.assertEquals(1, g2.getParentGroups().size());
		Assert.assertTrue(g2.getParentGroups().contains(g1));
		Assert.assertEquals(0, g2.getChildGroups().size());
	}

	private void checkTripleChildSave(Group g1, Group g2, Group g3)
	{
		g1 = cache.getObject(Group.class, g1.getId());
		Assert.assertEquals(0, g1.getParentGroups().size());
		Assert.assertEquals(2, g1.getChildGroups().size());
		Assert.assertTrue(g1.getChildGroups().contains(g2));
		Assert.assertTrue(g1.getChildGroups().contains(g3));
		g2 = cache.getObject(Group.class, g2.getId());
		Assert.assertEquals(1, g2.getParentGroups().size());
		Assert.assertTrue(g2.getParentGroups().contains(g1));
		Assert.assertEquals(1, g2.getChildGroups().size());
		Assert.assertTrue(g2.getChildGroups().contains(g3));
		g3 = cache.getObject(Group.class, g3.getId());
		Assert.assertEquals(2, g3.getParentGroups().size());
		Assert.assertTrue(g3.getParentGroups().contains(g1));
		Assert.assertTrue(g3.getParentGroups().contains(g2));
		Assert.assertEquals(0, g3.getChildGroups().size());
	}
}
