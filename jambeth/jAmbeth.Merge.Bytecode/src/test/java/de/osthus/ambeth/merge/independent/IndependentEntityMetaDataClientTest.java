package de.osthus.ambeth.merge.independent;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import de.osthus.ambeth.ioc.BytecodeModule;
import de.osthus.ambeth.ioc.CompositeIdModule;
import de.osthus.ambeth.ioc.EventModule;
import de.osthus.ambeth.merge.IValueObjectConfig;
import de.osthus.ambeth.merge.IndependentEntityMetaDataClient;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.testutil.AbstractIocTest;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;

@TestModule({ BytecodeModule.class, CompositeIdModule.class, IndependentEntityMetaDataClientTestModule.class, EventModule.class })
@TestPropertiesList({ @TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/merge/independent/independent-orm.xml"),
		@TestProperties(name = ConfigurationConstants.valueObjectFile, value = "de/osthus/ambeth/merge/independent/independent-vo-config.xml") })
public class IndependentEntityMetaDataClientTest extends AbstractIocTest
{
	private IndependentEntityMetaDataClient fixture;

	public void setFixture(IndependentEntityMetaDataClient fixture)
	{
		this.fixture = fixture;
	}

	@Test
	public void testGetMetaDataClassOfQ()
	{
		assertNotNull(fixture.getMetaData(EntityA.class));
		assertNotNull(fixture.getMetaData(EntityB.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetMetaDataClassOfQ_Exception()
	{
		fixture.getMetaData(String.class);
	}

	@Test
	public void testGetMetaDataClassOfQBoolean()
	{
		assertNotNull(fixture.getMetaData(EntityA.class, true));
		assertNotNull(fixture.getMetaData(EntityA.class, false));
		assertNull(fixture.getMetaData(String.class, true));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetMetaDataClassOfQBoolean_Exception()
	{
		assertNull(fixture.getMetaData(String.class, false));
	}

	@Test
	public void testGetMetaDataListOfClassOfQ()
	{
		List<IEntityMetaData> actual = fixture.getMetaData(Collections.<Class<?>> emptyList());
		assertTrue(actual.isEmpty());
		actual = fixture.getMetaData(Arrays.<Class<?>> asList(EntityA.class, String.class, EntityB.class));
		assertEquals(2, actual.size());
	}

	@Test
	public void testMetaDataContent_EntityA()
	{
		IEntityMetaData actual = fixture.getMetaData(EntityA.class);

		assertEquals(EntityA.class, actual.getEntityType());
		checkTechnicalProperties(actual);

		assertEquals(0, actual.getAlternateIdCount());
		assertEquals(0, actual.getAlternateIdMemberIndicesInPrimitives().length);
		assertEquals(0, actual.getAlternateIdMembers().length);
		assertEquals(actual.getIdMember(), actual.getIdMemberByIdIndex(ObjRef.PRIMARY_KEY_INDEX));

		assertEquals(0, actual.getPrimitiveMembers().length);
		assertEquals(2, actual.getRelationMembers().length);
		assertEquals(1, actual.getTypesRelatingToThis().length);
	}

	@Test
	public void testMetaDataContent_EntityB()
	{
		IEntityMetaData actual = fixture.getMetaData(EntityB.class);

		assertEquals(EntityB.class, actual.getEntityType());
		checkTechnicalProperties(actual);

		assertEquals(1, actual.getAlternateIdCount());
		assertArrayEquals(new int[][] { { 0 } }, actual.getAlternateIdMemberIndicesInPrimitives());
		assertEquals(1, actual.getAlternateIdMembers().length);
		assertEquals("Name", actual.getAlternateIdMembers()[0].getName());
		assertEquals(0, actual.getIdIndexByMemberName("Name"));
		assertEquals(actual.getIdMember(), actual.getIdMemberByIdIndex(ObjRef.PRIMARY_KEY_INDEX));
		assertEquals(actual.getMemberByName("Name"), actual.getIdMemberByIdIndex((byte) 0));

		assertEquals(1, actual.getPrimitiveMembers().length);
		assertEquals(0, actual.getRelationMembers().length);
		assertEquals(1, actual.getTypesRelatingToThis().length);
	}

	protected void checkTechnicalProperties(IEntityMetaData actual)
	{
		assertNotNull(actual.getIdMember());
		assertEquals("Id", actual.getIdMember().getName());
		assertNotNull(actual.getVersionMember());
		assertEquals("Version", actual.getVersionMember().getName());

		assertNull(actual.getCreatedByMember());
		assertNull(actual.getCreatedOnMember());
		assertNull(actual.getUpdatedByMember());
		assertNull(actual.getUpdatedOnMember());
	}

	@Test
	public void testUnregisterValueObjectMapping()
	{
		IValueObjectConfig actual = fixture.getValueObjectConfig(EntityAType.class);
		assertNotNull(actual);

		fixture.unregisterValueObjectConfig(actual);
		assertNull(fixture.getValueObjectConfig(EntityAType.class));

		fixture.registerValueObjectConfig(actual);
		assertNotNull(fixture.getValueObjectConfig(EntityAType.class));
	}

	@Test
	public void testGetValueObjectConfig()
	{
		IValueObjectConfig actual;

		actual = fixture.getValueObjectConfig(EntityAType.class);
		assertNotNull(actual);
		assertEquals(EntityA.class, actual.getEntityType());
		assertEquals(EntityAType.class, actual.getValueType());

		actual = fixture.getValueObjectConfig(EntityBType1.class);
		assertNotNull(actual);
		assertEquals(EntityB.class, actual.getEntityType());
		assertEquals(EntityBType1.class, actual.getValueType());

		actual = fixture.getValueObjectConfig(EntityBType2.class);
		assertNotNull(actual);
		assertEquals(EntityB.class, actual.getEntityType());
		assertEquals(EntityBType2.class, actual.getValueType());

		assertNull(fixture.getValueObjectConfig(String.class));
	}
}
