package de.osthus.ambeth.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.lang.annotation.Annotation;
import java.util.Collection;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;

public class FieldTest
{

	private Field fixture;

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
		Table table = new Table();
		table.setIdField(new Field());
		fixture = new Field();
		fixture.setProperties(Properties.getApplication());
		fixture.setTable(table);
		fixture.setName("Test field name");
		fixture.setFieldType(String.class);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception
	{
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Field#afterPropertiesSet()}.
	 */
	@Test
	public final void testAfterPropertiesSet()
	{
		fixture.afterPropertiesSet();
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Field#afterPropertiesSet()}.
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testAfterPropertiesSet_noTable()
	{
		fixture.setTable(null);
		fixture.afterPropertiesSet();
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Field#afterPropertiesSet()}.
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testAfterPropertiesSet_noName()
	{
		fixture.setName(null);
		fixture.afterPropertiesSet();
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Field#afterPropertiesSet()}.
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testAfterPropertiesSet_noFieldType()
	{
		fixture.setFieldType(null);
		fixture.afterPropertiesSet();
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Field#getTable()}.
	 */
	@Test
	public final void testGetTable()
	{
		assertNotNull("Returns null!", fixture.getTable());
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Field#setTable(de.osthus.ambeth.persistence.ITable)} .
	 */
	@Test
	public final void testSetTable()
	{
		ITable actual = new Table();
		assertNotSame("Should not be same object!", actual, fixture.getTable());
		fixture.setTable(actual);
		assertSame("Should be same object!", actual, fixture.getTable());
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Field#getName()}.
	 */
	@Test
	public final void testGetName()
	{
		assertNotNull("Returns null!", fixture.getName());
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Field#setName(java.lang.String)}.
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
	 * Test method for {@link de.osthus.ambeth.persistence.Field#getMember()}.
	 */
	@Test
	public final void testGetMember()
	{
		assertNull("Does not return null!", fixture.getMember());
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Field#setMember(de.osthus.ambeth.typeinfo.ITypeInfoItem)} .
	 */
	@Test
	public final void testSetMember()
	{
		ITypeInfoItem actual = new ITypeInfoItem()
		{
			@Override
			public boolean canRead()
			{
				return true;
			}

			@Override
			public boolean canWrite()
			{
				return true;
			}

			@Override
			public boolean isTechnicalMember()
			{
				return false;
			}

			@Override
			public void setTechnicalMember(boolean technicalMember)
			{
			}

			@Override
			public Class<?> getDeclaringType()
			{
				return null;
			}

			@Override
			public Object getDefaultValue()
			{
				return null;
			}

			@Override
			public Collection<?> createInstanceOfCollection()
			{
				return null;
			}

			@Override
			public Object getNullEquivalentValue()
			{
				return null;
			}

			@Override
			public Class<?> getRealType()
			{
				return null;
			}

			@Override
			public Class<?> getElementType()
			{
				return null;
			}

			@Override
			public void setValue(Object obj, Object value)
			{
			}

			@Override
			public Object getValue(Object obj)
			{
				return null;
			}

			@Override
			public Object getValue(Object obj, boolean allowNullEquivalentValue)
			{
				return null;
			}

			@Override
			public String getName()
			{
				return null;
			}

			@Override
			public String getXMLName()
			{
				return null;
			}

			@Override
			public boolean isXMLIgnore()
			{
				return false;
			}

			@Override
			public void setDefaultValue(Object defaultValue)
			{
			}

			@Override
			public void setNullEquivalentValue(Object nullEquivalentValue)
			{
			}

			@Override
			public <V extends Annotation> V getAnnotation(Class<V> annotationType)
			{
				return null;
			}
		};
		assertNotSame("Should not be same object!", actual, fixture.getMember());
		fixture.setMember(actual);
		assertSame("Should be same object!", actual, fixture.getMember());
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Field#getFieldType()} .
	 */
	@Test
	public final void testGetFieldType()
	{
		assertNotNull("Returns null!", fixture.getTable());
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Field#setFieldType(java.lang.Class)}.
	 */
	@Test
	public final void testSetFieldType()
	{
		Class<?> actual = Integer.class;
		assertNotSame("Should not be same object!", actual, fixture.getFieldType());
		fixture.setFieldType(actual);
		assertSame("Should be same object!", actual, fixture.getFieldType());
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Field#getEntityType()}.
	 */
	@Test
	public final void testGetEntityType()
	{
		assertNull("Should return null!", fixture.getEntityType());

		((Table) fixture.getTable()).setEntityType(String.class);
		assertEquals("Wrong value!", String.class, fixture.getEntityType());
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Field#findAll(java.lang.Object)}.
	 */
	@Test(expected = UnsupportedOperationException.class)
	public final void testFindAll()
	{
		fixture.findAll(null);
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Field#findSingle(java.lang.Object)}.
	 */
	@Test(expected = UnsupportedOperationException.class)
	public final void testFindSingle()
	{
		fixture.findSingle(null);
	}

	/**
	 * Test method for {@link de.osthus.ambeth.persistence.Field#toString()}.
	 */
	@Test
	public final void testToString()
	{
		assertEquals("Wrong value!", "Field: " + fixture.getName(), fixture.toString());
	}

}
