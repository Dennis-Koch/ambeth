package de.osthus.ambeth.orm20.independent.a22b;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.orm20.independent.AbstractIndependentOrm20Test;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;

/**
 * Test for two uni-directional one-to-one relations
 */
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/orm20/independent/a22b/orm20.xml")
public class Orm20A22BTest extends AbstractIndependentOrm20Test
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
		testZeroRelationMembers(EntityB.class);
	}

	@Test
	public void testRelatedTypes()
	{
		IEntityMetaData metaDataA = retrieveMetaData(EntityA.class);
		Class<?>[] typesRelatingToA = metaDataA.getTypesRelatingToThis();
		assertNotNull(typesRelatingToA);
		assertEquals(0, typesRelatingToA.length);

		IEntityMetaData metaDataB = retrieveMetaData(EntityB.class);
		Class<?>[] typesRelatingToB = metaDataB.getTypesRelatingToThis();
		assertNotNull(typesRelatingToB);
		assertEquals(1, typesRelatingToB.length);
		metaDataB.isRelatingToThis(typesRelatingToB[0]);
	}
}
