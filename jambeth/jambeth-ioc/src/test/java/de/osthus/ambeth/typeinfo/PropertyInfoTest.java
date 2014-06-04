package de.osthus.ambeth.typeinfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;

import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.testutil.AbstractIocTest;
import de.osthus.ambeth.util.ParamChecker;

public class PropertyInfoTest extends AbstractIocTest
{
	static final String[] propNames = { "Annotations", "BackingField", "DeclaringType", "ElementType", "EntityType", "Getter", "Modifiers", "Name",
			"PropertyType", "Readable", "Setter", "Writable" };

	private IMap<String, IPropertyInfo> fixture;

	private IPropertyInfoProvider propertyInfoProvider;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(propertyInfoProvider, "propertyInfoProvider");
	}

	public void setPropertyInfoProvider(IPropertyInfoProvider propertyInfoProvider)
	{
		this.propertyInfoProvider = propertyInfoProvider;
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		fixture = propertyInfoProvider.getPropertyMap(MethodPropertyInfo.class);
	}

	/**
	 * Test method for {@link de.osthus.ambeth.typeinfo.MethodPropertyInfo#PropertyInfo(java.lang.reflect.Method, java.lang.reflect.Method)} .
	 */
	@Test
	public final void testPropertyInfo()
	{
		assertNotNull("Fixture is null!", this.fixture);
	}

	/**
	 * Test method for {@link de.osthus.ambeth.typeinfo.MethodPropertyInfo#PropertyInfo.init()} .
	 * 
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	@Test
	public final void testInit_onlySetter() throws SecurityException, NoSuchMethodException
	{
		new MethodPropertyInfo(Date.class, "Time", null, Date.class.getMethod("setTime", long.class));
	}

	/**
	 * Test method for {@link de.osthus.ambeth.typeinfo.MethodPropertyInfo#PropertyInfo.init()} .
	 * 
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	@Test(expected = RuntimeException.class)
	public final void testInit_noMethods() throws SecurityException, NoSuchMethodException
	{
		new MethodPropertyInfo(Date.class, "Time", null, null);
	}

	/**
	 * Test method for {@link de.osthus.ambeth.typeinfo.MethodPropertyInfo#PropertyInfo.init()} .
	 * 
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	@Test(expected = RuntimeException.class)
	public final void testInit_getterNotGetter() throws SecurityException, NoSuchMethodException
	{
		new MethodPropertyInfo(Class.class, "Dummy", Class.class.getMethod("getField", String.class), null);
	}

	/**
	 * Test method for {@link de.osthus.ambeth.typeinfo.MethodPropertyInfo#PropertyInfo.init()} .
	 * 
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	@Test(expected = RuntimeException.class)
	public final void testInit_misfittingMethods() throws SecurityException, NoSuchMethodException
	{
		new MethodPropertyInfo(Date.class, "Time", Date.class.getMethod("getDate"), Date.class.getMethod("setTime", long.class));
	}

	/**
	 * Test method for {@link de.osthus.ambeth.typeinfo.MethodPropertyInfo#getName()}.
	 */
	@Test
	public final void testGetName()
	{
		for (Entry<String, IPropertyInfo> property : this.fixture)
		{
			assertEquals("Wrong name: " + property.getKey() + " --> " + property.getValue().getName(), property.getKey(), property.getValue().getName());
		}
	}

	/**
	 * Test method for {@link de.osthus.ambeth.typeinfo.MethodPropertyInfo#getPropertyType()}.
	 */
	@Test
	public final void testGetPropertyType()
	{
		assertEquals(String.class, this.fixture.get("Name").getPropertyType());
		assertEquals(Method.class, this.fixture.get("Getter").getPropertyType());
		assertEquals(boolean.class, this.fixture.get("Writable").getPropertyType());

		Map<String, IPropertyInfo> dateProperties = propertyInfoProvider.getPropertyMap(Date.class);
		assertEquals(long.class, dateProperties.get("Time").getPropertyType());
	}

	/**
	 * Test method for {@link de.osthus.ambeth.typeinfo.MethodPropertyInfo#isWritable()}.
	 */
	@Test
	public final void testIsWritable()
	{
		assertFalse(this.fixture.get("Name").isWritable());

		Map<String, IPropertyInfo> dateProperties = propertyInfoProvider.getPropertyMap(Date.class);
		assertTrue(dateProperties.get("Time").isWritable());
	}

	/**
	 * Test method for {@link de.osthus.ambeth.typeinfo.MethodPropertyInfo#getGetter()}.
	 */
	@Test
	public final void testGetGetter()
	{
		assertNotNull("Getter is null!", ((MethodPropertyInfo) this.fixture.get("Name")).getGetter());

		Map<String, IPropertyInfo> dateProperties = propertyInfoProvider.getPropertyMap(Date.class);
		assertNotNull("Getter is null!", ((MethodPropertyInfo) dateProperties.get("Time")).getGetter());
	}

	/**
	 * Test method for {@link de.osthus.ambeth.typeinfo.MethodPropertyInfo#getSetter()}.
	 */
	@Test
	public final void testGetSetter()
	{
		assertNull("Getter is not null!", ((MethodPropertyInfo) this.fixture.get("Name")).getSetter());

		Map<String, IPropertyInfo> dateProperties = propertyInfoProvider.getPropertyMap(Date.class);
		assertNotNull("Getter is null!", ((MethodPropertyInfo) dateProperties.get("Time")).getSetter());
	}

	/**
	 * Test method for {@link de.osthus.ambeth.typeinfo.MethodPropertyInfo#getValue(java.lang.Object)} .
	 * 
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	@Test
	public final void testGetValue() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException,
			NoSuchMethodException
	{
		assertEquals(this.fixture.get("Annotations").getName(), this.fixture.get("Name").getValue(this.fixture.get("Annotations")));

		MethodPropertyInfo newFixture = new MethodPropertyInfo(Date.class, "Time", null, Date.class.getMethod("setTime", long.class));
		Date now = new Date();
		assertNull(newFixture.getValue(now));
	}

	/**
	 * Test method for {@link de.osthus.ambeth.typeinfo.MethodPropertyInfo#getValue(java.lang.Object)} .
	 * 
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	@Test
	public final void testGetValue_Exception() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		this.fixture.get("Name").getValue(this.fixture.get("Annotations"));
	}

	/**
	 * Test method for {@link de.osthus.ambeth.typeinfo.MethodPropertyInfo#setValue(java.lang.Object, java.lang.Object)} .
	 * 
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	@Test
	public final void testSetValue() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		Date now = new Date();
		long time = now.getTime();

		Map<String, IPropertyInfo> dateProperties = propertyInfoProvider.getPropertyMap(Date.class);
		dateProperties.get("Time").setValue(now, time / 2);
		assertEquals(time / 2, now.getTime());
	}

	/**
	 * Test method for {@link de.osthus.ambeth.typeinfo.MethodPropertyInfo#setValue(java.lang.Object, java.lang.Object)} .
	 * 
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	@Test(expected = UnsupportedOperationException.class)
	public final void testSetValue_Exception() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		this.fixture.get("Name").setValue(this.fixture.get("Annotations"), "test");
	}

	/**
	 * Test method for {@link de.osthus.ambeth.typeinfo.MethodPropertyInfo#setValue(java.lang.Object, java.lang.Object)} .
	 * 
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	@Test(expected = UnsupportedOperationException.class)
	public final void testSetValue_Exception2() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		this.fixture.get("Name").setValue(this.fixture.get("Annotations"), "test");
	}

	/**
	 * Test method for {@link de.osthus.ambeth.typeinfo.MethodPropertyInfo#getAnnotations()}.
	 */
	@Test
	public final void testGetAnnotations()
	{
		IPropertyInfo actual = propertyInfoProvider.getProperty(Date.class, "Date");
		assertEquals(1, actual.getAnnotations().length);

		assertEquals(0, propertyInfoProvider.getProperty(MethodPropertyInfo.class, "Annotations").getAnnotations().length);
	}

	/**
	 * Test method for {@link de.osthus.ambeth.typeinfo.MethodPropertyInfo#getAnnotation(java.lang.Class)} .
	 */
	@Test
	public final void testGetAnnotation()
	{
		assertNotNull(propertyInfoProvider.getProperty(Date.class, "Date").getAnnotation(Deprecated.class));
		assertNull(propertyInfoProvider.getProperty(MethodPropertyInfo.class, "Annotations").getAnnotation(Deprecated.class));
	}

	/**
	 * Test method for {@link de.osthus.ambeth.typeinfo.MethodPropertyInfo#getProperties(java.lang.Class, java.lang.String)} .
	 */
	@Test
	public final void testGetPropertyClassString()
	{
		isPropertyArrayOK(propertyInfoProvider.getProperties(MethodPropertyInfo.class));
	}

	/**
	 * Test method for {@link de.osthus.ambeth.typeinfo.MethodPropertyInfo#getProperties(java.lang.Object, java.lang.String)} .
	 */
	@Test
	public final void testGetPropertiesObjectString()
	{
		assertNotNull(propertyInfoProvider.getProperty(fixture.get("Name"), "Annotations"));
	}

	/**
	 * Test method for {@link de.osthus.ambeth.typeinfo.MethodPropertyInfo#getProperties(java.lang.Class)} .
	 */
	@Test
	public final void testGetPropertiesClass()
	{
		isPropertyArrayOK(propertyInfoProvider.getProperties(MethodPropertyInfo.class));
	}

	/**
	 * Test method for {@link de.osthus.ambeth.typeinfo.MethodPropertyInfo#getProperties(java.lang.Object)} .
	 */
	@Test
	public final void testGetPropertiesObject()
	{
		isPropertyArrayOK(propertyInfoProvider.getProperties(this.fixture.get("Name")));
	}

	/**
	 * Test method for {@link de.osthus.ambeth.typeinfo.MethodPropertyInfo#getPropertyMap(java.lang.Class)} .
	 */
	@Test
	public final void testGetPropertyMapClass()
	{
		isPropertyMapOK(propertyInfoProvider.getPropertyMap(MethodPropertyInfo.class));
	}

	/**
	 * Test method for {@link de.osthus.ambeth.typeinfo.MethodPropertyInfo#getPropertyMap(java.lang.Object)} .
	 */
	@Test
	public final void testGetPropertyMapObject()
	{
		isPropertyMapOK(propertyInfoProvider.getPropertyMap(this.fixture.get("Name")));
	}

	/**
	 * Test method for {@link de.osthus.ambeth.typeinfo.MethodPropertyInfo#getPropertyNameFor(java.lang.reflect.Method)} .
	 * 
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	@Test
	public final void testGetPropertyNameFor() throws SecurityException, NoSuchMethodException
	{
		Method method = MethodPropertyInfo.class.getMethod("getName");
		assertEquals("Wrong name!", "Name", propertyInfoProvider.getPropertyNameFor(method));

		method = MethodPropertyInfo.class.getMethod("isWritable");
		assertEquals("Wrong name!", "Writable", propertyInfoProvider.getPropertyNameFor(method));

		method = MethodPropertyInfo.class.getMethod("equals", Object.class);
		assertTrue("Wrong name!", propertyInfoProvider.getPropertyNameFor(method).isEmpty());

		method = MethodPropertyInfo.class.getMethod("getValue", Object.class);
		assertTrue("Wrong name!", propertyInfoProvider.getPropertyNameFor(method).isEmpty());

		method = MethodPropertyInfo.class.getMethod("setValue", Object.class, Object.class);
		assertTrue("Wrong name!", propertyInfoProvider.getPropertyNameFor(method).isEmpty());
	}

	/**
	 * If changes are made to the PropertyInfo class this might need an update.
	 * 
	 * @param actual
	 * @return
	 */
	private void isPropertyArrayOK(IPropertyInfo[] actual)
	{
		assertEquals("Wrong number of properties!", propNames.length, actual.length);

		List<String> propNameList = Arrays.asList(propNames);
		for (int i = actual.length; i-- > 0;)
		{
			assertTrue("Unknown property: " + actual[i].getName(), propNameList.contains(actual[i].getName()));
		}
	}

	/**
	 * If changes are made to the PropertyInfo class this might need an update.
	 * 
	 * @param actual
	 * @return
	 */
	private void isPropertyMapOK(Map<String, IPropertyInfo> actual)
	{
		assertEquals("Wrong number of properties!", propNames.length, actual.size());

		List<String> propNameList = Arrays.asList(propNames);
		for (String name : actual.keySet())
		{
			assertTrue("Unknown property: " + name, propNameList.contains(name));
		}

		isPropertyArrayOK(actual.values().toArray(new MethodPropertyInfo[propNames.length]));
	}
}