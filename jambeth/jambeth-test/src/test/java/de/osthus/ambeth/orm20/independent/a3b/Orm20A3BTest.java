package de.osthus.ambeth.orm20.independent.a3b;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.metadata.RelationMember;
import de.osthus.ambeth.orm20.independent.AbstractIndependentOrm20Test;
import de.osthus.ambeth.testutil.TestProperties;

/**
 * Test for bi-directional one-to-one relation
 */
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/orm20/independent/a3b/orm20.xml")
public class Orm20A3BTest extends AbstractIndependentOrm20Test
{
	@Test
	public void testRelationMembersA()
	{
		IEntityMetaData metaData = retrieveMetaData(EntityA.class);
		RelationMember[] relationMembers = metaData.getRelationMembers();
		assertEquals(1, relationMembers.length);

		RelationMember relationMember = relationMembers[0];
		assertEquals("B", relationMember.getName());
		assertEquals(0, metaData.getIndexByRelationName(relationMember.getName()));
		assertEquals(relationMember, metaData.getMemberByName(relationMember.getName()));
		assertEquals(EntityB.class, relationMember.getRealType());
	}

	@Test
	public void testRelationMembersB()
	{
		IEntityMetaData metaData = retrieveMetaData(EntityB.class);
		RelationMember[] relationMembers = metaData.getRelationMembers();
		assertEquals(1, relationMembers.length);

		RelationMember relationMember = relationMembers[0];
		assertEquals("A", relationMember.getName());
		assertEquals(0, metaData.getIndexByRelationName(relationMember.getName()));
		assertEquals(relationMember, metaData.getMemberByName(relationMember.getName()));
		assertEquals(EntityA.class, relationMember.getRealType());
	}

	@Test
	public void testRelatedTypes()
	{
		IEntityMetaData metaDataA = retrieveMetaData(EntityA.class);
		Class<?>[] typesRelatingToA = metaDataA.getTypesRelatingToThis();
		assertNotNull(typesRelatingToA);
		assertEquals(1, typesRelatingToA.length);
		metaDataA.isRelatingToThis(typesRelatingToA[0]);

		IEntityMetaData metaDataB = retrieveMetaData(EntityB.class);
		Class<?>[] typesRelatingToB = metaDataB.getTypesRelatingToThis();
		assertNotNull(typesRelatingToB);
		assertEquals(1, typesRelatingToB.length);
		metaDataB.isRelatingToThis(typesRelatingToB[0]);
	}
}
