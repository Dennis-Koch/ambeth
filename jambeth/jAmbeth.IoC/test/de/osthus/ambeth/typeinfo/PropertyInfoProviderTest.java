package de.osthus.ambeth.typeinfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import de.osthus.ambeth.testutil.AbstractIocTest;
import de.osthus.ambeth.util.ParamChecker;

public class PropertyInfoProviderTest extends AbstractIocTest
{
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
		IPropertyInfo subject = propertyInfoProvider.getProperty(MethodPropertyInfo.class, "Name");
		assertNotNull(propertyInfoProvider.getProperty(subject, "Annotations"));
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
		IPropertyInfo subject = propertyInfoProvider.getProperty(MethodPropertyInfo.class, "Name");
		isPropertyArrayOK(propertyInfoProvider.getProperties(subject));
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
		IPropertyInfo subject = propertyInfoProvider.getProperty(MethodPropertyInfo.class, "Name");
		isPropertyMapOK(propertyInfoProvider.getPropertyMap(subject));
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
		assertEquals("Wrong number of properties!", PropertyInfoTest.propNames.length, actual.length);

		List<String> propNameList = Arrays.asList(PropertyInfoTest.propNames);
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
		assertEquals("Wrong number of properties!", PropertyInfoTest.propNames.length, actual.size());

		List<String> propNameList = Arrays.asList(PropertyInfoTest.propNames);
		for (String name : actual.keySet())
		{
			assertTrue("Unknown property: " + name, propNameList.contains(name));
		}

		isPropertyArrayOK(actual.values().toArray(new MethodPropertyInfo[PropertyInfoTest.propNames.length]));
	}
}
