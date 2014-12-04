package de.osthus.ambeth.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.metadata.MemberTypeProvider;
import de.osthus.ambeth.objectcollector.NoOpObjectCollector;
import de.osthus.ambeth.util.AlreadyLinkedCache;

public class TableTest
{

	private Table fixture;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		fixture = new Table();
		fixture.setIdFields(new IField[] { new Field() });
		fixture.setVersionField(new Field());
		fixture.setName("Test table name");
		fixture.memberTypeProvider = new MemberTypeProvider();
		fixture.alreadyLinkedCache = new AlreadyLinkedCache();
		fixture.objectCollector = new NoOpObjectCollector();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception
	{
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#Table()}.
	 */
	@Test
	public final void testTable()
	{
		assertNotNull("Fixture is null!", fixture);
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#afterPropertiesSet()}.
	 */
	@Test
	public final void testAfterPropertiesSet() throws Throwable
	{
		fixture.afterPropertiesSet();
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#afterPropertiesSet()}.
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testAfterPropertiesSet_noIdField() throws Throwable
	{
		fixture.setIdFields(null);
		fixture.afterPropertiesSet();
		fixture.setEntityType(String.class);
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#afterPropertiesSet()}.
	 */
	@Test(expected = IllegalArgumentException.class)
	@Ignore
	// 2012-06-25 JH Now it is allowed to have a table without a version column
	public final void testAfterPropertiesSet_noVersionField() throws Throwable
	{
		fixture.setVersionField(null);
		fixture.afterPropertiesSet();
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#afterPropertiesSet()}.
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testAfterPropertiesSet_noName() throws Throwable
	{
		fixture.setName(null);
		fixture.afterPropertiesSet();
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#getName()}.
	 */
	@Test
	public final void testGetName()
	{
		assertNotNull("Returns null!", fixture.getName());
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#setName(java.lang.String)}.
	 */
	@Test
	public final void testSetName()
	{
		String actual = "new test name";
		assertNotSame("Should not be same object!", actual, fixture.getName());
		fixture.setName(actual);
		assertSame("Should be same object!", actual, fixture.getName());
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#getEntityType()}.
	 */
	@Test
	public final void testGetEntityType()
	{
		assertNull("Does not return null!", fixture.getEntityType());
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#setEntityType(java.lang.Class)} .
	 */
	@Test
	public final void testSetEntityType()
	{
		Class<?> actual = Long.class;
		assertNotSame("Should not be same object!", actual, fixture.getEntityType());
		fixture.setEntityType(actual);
		assertSame("Should be same object!", actual, fixture.getEntityType());
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#getIdField()}.
	 */
	@Test
	public final void testGetIdField()
	{
		assertNotNull("Returns null!", fixture.getIdField());
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#setIdFields(de.osthus.ambeth.persistence.IField)} .
	 */
	@Test
	public final void testSetIdField()
	{
		IField actual = new Field();
		assertNotSame("Should not be same object!", actual, fixture.getIdField());
		fixture.setIdFields(new IField[] { actual });
		assertSame("Should be same object!", actual, fixture.getIdField());
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#getVersionField()}.
	 */
	@Test
	public final void testGetVersionField()
	{
		assertNotNull("Returns null!", fixture.getIdField());
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#setVersionField(de.osthus.ambeth.persistence.IField)} .
	 */
	@Test
	public final void testSetVersionField()
	{
		IField actual = new Field();
		assertNotSame("Should not be same object!", actual, fixture.getVersionField());
		fixture.setVersionField(actual);
		assertSame("Should be same object!", actual, fixture.getVersionField());
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#getTypeInfoProvider()}.
	 */
	@Test
	public final void testGetTypeInfoProvider()
	{
		assertNotNull("Returns null!", fixture.getIdField());
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#getPrimitiveFields()}.
	 */
	@Test
	@Ignore
	public final void testGetPrimitiveFields()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#getAllFields()}.
	 */
	@Test
	@Ignore
	public final void testGetAllFields()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#getLinks()}.
	 */
	@Test
	@Ignore
	public final void testGetLinks()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#acquireIds(int)}.
	 */
	@Test
	@Ignore
	public final void testAcquireIds()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#configureLink(java.lang.String, java.lang.String)} .
	 */
	@Test
	@Ignore
	public final void testConfigureLink()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#insert(java.lang.Object, java.lang.Object, java.util.Map)} .
	 */
	@Test(expected = UnsupportedOperationException.class)
	public final void testInsert()
	{
		fixture.insert(null, null, (ILinkedMap<String, Object>) null);
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#mapField(de.osthus.ambeth.persistence.IField)} .
	 */
	@Test
	public final void testMapFieldIField()
	{
		assertTrue("Should be empty!", fixture.getPrimitiveFields().isEmpty());
		Field field1 = new Field();
		field1.setName("Field 1");
		fixture.mapField(field1);
		assertEquals("Should contain 1 field!", 1, fixture.getPrimitiveFields().size());
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#mapField(de.osthus.ambeth.persistence.IField)} .
	 */
	@Test(expected = RuntimeException.class)
	public final void testMapFieldIField_alreadyMapped()
	{
		assertTrue("Should be empty!", fixture.getPrimitiveFields().isEmpty());
		Field field1 = new Field();
		field1.setName("Field 1");
		fixture.mapField(field1);
		fixture.mapField(field1);
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#mapField(java.lang.String, java.lang.String)} .
	 */
	@Test
	@Ignore
	public final void testMapFieldStringString()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#mapField(java.lang.String, java.lang.String)} .
	 */
	@Test
	@Ignore
	public final void testMapFieldStringString_noMember()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#mapField(java.lang.String, java.lang.String)} .
	 */
	@Test
	@Ignore
	public final void testMapFieldStringString_noField()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#mapLink(de.osthus.ambeth.persistence.IDirectedLink)} .
	 */
	@Test
	@Ignore
	public final void testMapLinkIDirectedLink()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#mapLink(java.lang.String, java.lang.String)} .
	 */
	@Test
	@Ignore
	public final void testMapLinkStringString()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#selectVersion(java.util.List)}.
	 */
	@Test(expected = UnsupportedOperationException.class)
	public final void testSelectVersion()
	{
		fixture.selectVersion(null);
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#selectAll()}.
	 */
	@Test(expected = UnsupportedOperationException.class)
	public final void testSelectAll()
	{
		fixture.selectAll();
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#selectValues(java.util.List)}.
	 */
	@Test(expected = UnsupportedOperationException.class)
	public final void testSelectValues()
	{
		fixture.selectValues(null);
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#update(java.lang.Object, java.lang.Object, java.util.List)} .
	 */
	@Test(expected = UnsupportedOperationException.class)
	public final void testUpdate()
	{
		fixture.update(null, null, (ILinkedMap<String, Object>) null);
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#getFieldByName(java.lang.String)} .
	 */
	@Test
	@Ignore
	public final void testGetFieldByName()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#getFieldByMemberName(java.lang.String)} .
	 */
	@Test
	@Ignore
	public final void testGetFieldByMemberName()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#getLinkByName(java.lang.String)} .
	 */
	@Test
	@Ignore
	public final void testGetLinkByName()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#getLinkByMemberName(java.lang.String)} .
	 */
	@Test
	@Ignore
	public final void testGetLinkByMemberName()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#getMemberNameByLinkName(java.lang.String)} .
	 */
	@Test
	@Ignore
	public final void testGetMemberNameByLinkName()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#deleteAll()}.
	 */
	@Test(expected = UnsupportedOperationException.class)
	public final void testDeleteAll()
	{
		fixture.deleteAll();
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#deleteLinksToId(java.lang.Object)} .
	 */
	@Test
	@Ignore
	public final void testDeleteLinksToId()
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Table#toString()}.
	 */
	@Test
	public final void testToString()
	{
		assertEquals("Wrong value!", "Table: " + fixture.getName(), fixture.toString());
	}

}
