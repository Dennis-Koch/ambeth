package de.osthus.ambeth.orm20.independent.a3b2a;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.orm20.independent.AbstractIndependentOrm20Test;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;

/**
 * Test for one uni- and one bi-directional one-to-one relation between the same entities
 */
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/orm20/independent/a3b2a/orm20.xml")
public class Orm20A3B2ATest extends AbstractIndependentOrm20Test
{
	@Test
	public void testRelationMembersA()
	{
		IEntityMetaData metaData = retrieveMetaData(EntityA.class);
		IRelationInfoItem[] relationMembers = metaData.getRelationMembers();
		assertEquals(2, relationMembers.length);

		int indexB1 = metaData.getIndexByRelationName("B1");
		IRelationInfoItem relationMember = relationMembers[indexB1];
		assertEquals(relationMember, metaData.getMemberByName(relationMember.getName()));
		assertEquals(EntityB.class, relationMember.getRealType());

		int indexB2 = metaData.getIndexByRelationName("B2");
		relationMember = relationMembers[indexB2];
		assertEquals(relationMember, metaData.getMemberByName(relationMember.getName()));
		assertEquals(EntityB.class, relationMember.getRealType());
	}

	@Test
	public void testRelationMembersB()
	{
		IEntityMetaData metaData = retrieveMetaData(EntityB.class);
		IRelationInfoItem[] relationMembers = metaData.getRelationMembers();
		assertEquals(1, relationMembers.length);

		IRelationInfoItem relationMember = relationMembers[0];
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
