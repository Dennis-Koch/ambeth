package de.osthus.ambeth.orm20.independent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.merge.model.EntityMetaData;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.metadata.PrimitiveMember;
import de.osthus.ambeth.testutil.TestProperties;

@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "de/osthus/ambeth/orm20/independent/orm20.xml")
public class Orm20IndependentMetaDataTest extends AbstractIndependentOrm20Test
{
	@Test
	public void testExistsAndPlainValuesA()
	{
		// Local entity, one alternateId configured, one primitive List member, no relation members
		testExistsAndPlainValues(EntityA.class, true);
	}

	@Test
	public void testExistsAndPlainValuesB()
	{
		// Local entity, all technical members with non-default names, no non-technical members
		testExistsAndPlainValues(EntityB.class, true);
	}

	@Test
	public void testExistsAndPlainValuesC()
	{
		// External entity, fully auto-configured, no non-technical members
		testExistsAndPlainValues(EntityC.class, false);
	}

	@Test
	public void testTechnicalMembersA()
	{
		testTechnicalMembers(EntityA.class, "");
	}

	@Test
	public void testTechnicalMembersB()
	{
		testTechnicalMembers(EntityB.class, "B");
	}

	@Test
	public void testTechnicalMembersC()
	{
		testTechnicalMembers(EntityC.class, "");
	}

	@Test
	public void testAlteranteIdsA()
	{
		int alternateIdCount = 1;

		// AlternateIdCount
		IEntityMetaData metaDataA = retrieveMetaData(EntityA.class);
		assertEquals(alternateIdCount, metaDataA.getAlternateIdCount());

		// Alternate Id members
		PrimitiveMember[] alternateIdMembers = metaDataA.getAlternateIdMembers();
		assertEquals(alternateIdCount, alternateIdMembers.length);
		PrimitiveMember alternateIdMember = alternateIdMembers[0];
		assertEquals("Name", alternateIdMember.getName());
		assertEquals(String.class, alternateIdMember.getRealType());
		assertTrue(EntityA.class.isAssignableFrom(metaDataA.getEnhancedType()));
		assertEquals(metaDataA.getEnhancedType(), alternateIdMember.getDeclaringType());
		assertTrue(metaDataA.isAlternateId(alternateIdMember));
		assertEquals((byte) 0, metaDataA.getIdIndexByMemberName(alternateIdMember.getName()));
		assertEquals(alternateIdMember, metaDataA.getIdMemberByIdIndex((byte) 0));

		// Indices in Primitives
		int[][] alternateIdMemberIndicesInPrimitives = metaDataA.getAlternateIdMemberIndicesInPrimitives();
		assertEquals(alternateIdCount, alternateIdMemberIndicesInPrimitives.length);
		int indexInPrimitives = alternateIdMemberIndicesInPrimitives[0][0];
		PrimitiveMember[] primitiveMembers = metaDataA.getPrimitiveMembers();
		assertTrue(indexInPrimitives < primitiveMembers.length);
		PrimitiveMember alternateIdMemberByIndex = primitiveMembers[indexInPrimitives];
		assertEquals(alternateIdMember, alternateIdMemberByIndex);
	}

	@Test
	public void testAlteranteIdsB()
	{
		// AlternateIdCount
		IEntityMetaData metaDataB = retrieveMetaData(EntityB.class);
		assertEquals(0, metaDataB.getAlternateIdCount());

		// Alternate Id members
		PrimitiveMember[] alternateIdMembers = metaDataB.getAlternateIdMembers();
		assertEquals(0, alternateIdMembers.length);

		// Indices in Primitives
		int[][] alternateIdMemberIndicesInPrimitives = metaDataB.getAlternateIdMemberIndicesInPrimitives();
		assertEquals(0, alternateIdMemberIndicesInPrimitives.length);
	}

	@Test
	public void testAlteranteIdsC()
	{
		// AlternateIdCount
		IEntityMetaData metaDataC = retrieveMetaData(EntityC.class);
		assertEquals(0, metaDataC.getAlternateIdCount());

		// Alternate Id members
		PrimitiveMember[] alternateIdMembers = metaDataC.getAlternateIdMembers();
		assertEquals(0, alternateIdMembers.length);

		// Indices in Primitives
		int[][] alternateIdMemberIndicesInPrimitives = metaDataC.getAlternateIdMemberIndicesInPrimitives();
		assertEquals(0, alternateIdMemberIndicesInPrimitives.length);
	}

	@Test
	public void testPrimitiveMembersA()
	{
		IEntityMetaData metaDataA = retrieveMetaData(EntityA.class);

		PrimitiveMember[] primitiveMembers = metaDataA.getPrimitiveMembers();
		assertEquals(3 + 4, primitiveMembers.length); // + 4 technical members

		Member nameMember = metaDataA.getMemberByName("Name");
		assertNotNull(nameMember);
		assertEquals("Name", nameMember.getName());
		assertEquals(String.class, nameMember.getRealType());
		assertTrue(EntityA.class.isAssignableFrom(metaDataA.getEnhancedType()));
		assertEquals(metaDataA.getEnhancedType(), nameMember.getDeclaringType());
		assertTrue(metaDataA.isAlternateId(nameMember));
		int nameIndex = metaDataA.getIndexByPrimitive(nameMember);
		assertEquals(nameMember, primitiveMembers[nameIndex]);

		Member valuesMember = metaDataA.getMemberByName("Values");
		assertNotNull(valuesMember);
		assertEquals("Values", valuesMember.getName());
		assertEquals(List.class, valuesMember.getRealType());
		assertEquals(Integer.class, valuesMember.getElementType());
		assertEquals(metaDataA.getEnhancedType(), valuesMember.getDeclaringType());
		assertFalse(metaDataA.isAlternateId(valuesMember));
		int valuesIndex = metaDataA.getIndexByPrimitive(valuesMember);
		assertEquals(valuesMember, primitiveMembers[valuesIndex]);
	}

	@Test
	public void testPrimitiveMembersB()
	{
		testZeroPrimitiveMembers(EntityB.class);
	}

	@Test
	public void testPrimitiveMembersC()
	{
		testZeroPrimitiveMembers(EntityC.class);
	}

	@Test
	public void testRelationMembersA()
	{
		testZeroRelationMembers(EntityA.class);
	}

	@Test
	public void testRelationMembersB()
	{
		testZeroRelationMembers(EntityB.class);
	}

	@Test
	public void testRelationMembersC()
	{
		testZeroRelationMembers(EntityC.class);
	}

	protected void testExistsAndPlainValues(Class<?> entityType, boolean local)
	{
		IEntityMetaData metaData = retrieveMetaData(entityType);
		assertNotNull(metaData);
		assertEquals(entityType, metaData.getEntityType());
		assertEquals(local, metaData.isLocalEntity());
	}

	protected void testTechnicalMembers(Class<?> entityType, String memberNamePostfix)
	{
		IEntityMetaData metaData = retrieveMetaData(entityType);

		PrimitiveMember idMember = metaData.getIdMember();
		testTechnicalMember(idMember, EntityMetaData.DEFAULT_NAME_ID + memberNamePostfix, metaData);

		PrimitiveMember versionMember = metaData.getVersionMember();
		testTechnicalMember(versionMember, EntityMetaData.DEFAULT_NAME_VERSION + memberNamePostfix, metaData);

		PrimitiveMember createdByMember = metaData.getCreatedByMember();
		testTechnicalMember(createdByMember, EntityMetaData.DEFAULT_NAME_CREATED_BY + memberNamePostfix, metaData);

		PrimitiveMember createdOnMember = metaData.getCreatedOnMember();
		testTechnicalMember(createdOnMember, EntityMetaData.DEFAULT_NAME_CREATED_ON + memberNamePostfix, metaData);

		PrimitiveMember updatedByMember = metaData.getUpdatedByMember();
		testTechnicalMember(updatedByMember, EntityMetaData.DEFAULT_NAME_UPDATED_BY + memberNamePostfix, metaData);

		PrimitiveMember updatedOnMember = metaData.getUpdatedOnMember();
		testTechnicalMember(updatedOnMember, EntityMetaData.DEFAULT_NAME_UPDATED_ON + memberNamePostfix, metaData);
	}

	protected void testTechnicalMember(PrimitiveMember member, String memberName, IEntityMetaData metaData)
	{
		assertNotNull("Property '" + metaData.getEntityType().getSimpleName() + "." + memberName + "' not found", member);
		assertTrue(member.isTechnicalMember());
		assertEquals(memberName, member.getName());
		assertNotNull(member.getDeclaringType());
		assertFalse(metaData.isAlternateId(member));
	}

	protected void testZeroPrimitiveMembers(Class<?> entityType)
	{
		IEntityMetaData metaData = retrieveMetaData(entityType);

		PrimitiveMember[] primitiveMembers = metaData.getPrimitiveMembers();
		assertEquals(4, primitiveMembers.length); // Just the 4 technical members
	}
}
