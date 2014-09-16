package de.osthus.ambeth.typeinfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

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
		PropertyInfoTest.isPropertyArrayOK(PropertyInfoTest.propNames, propertyInfoProvider.getProperties(MethodPropertyInfo.class));
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
		PropertyInfoTest.isPropertyArrayOK(PropertyInfoTest.propNames, propertyInfoProvider.getProperties(MethodPropertyInfo.class));
	}

	/**
	 * Test method for {@link de.osthus.ambeth.typeinfo.MethodPropertyInfo#getProperties(java.lang.Object)} .
	 */
	@Test
	public final void testGetPropertiesObject()
	{
		IPropertyInfo subject = propertyInfoProvider.getProperty(MethodPropertyInfo.class, "Name");
		PropertyInfoTest.isPropertyArrayOK(PropertyInfoTest.propNames, propertyInfoProvider.getProperties(subject));
	}

	/**
	 * Test method for {@link de.osthus.ambeth.typeinfo.MethodPropertyInfo#getPropertyMap(java.lang.Class)} .
	 */
	@Test
	public final void testGetPropertyMapClass()
	{
		PropertyInfoTest.isPropertyMapOK(PropertyInfoTest.propNames, propertyInfoProvider.getPropertyMap(MethodPropertyInfo.class));
	}

	/**
	 * Test method for {@link de.osthus.ambeth.typeinfo.MethodPropertyInfo#getPropertyMap(java.lang.Object)} .
	 */
	@Test
	public final void testGetPropertyMapObject()
	{
		IPropertyInfo subject = propertyInfoProvider.getProperty(MethodPropertyInfo.class, "Name");
		PropertyInfoTest.isPropertyMapOK(PropertyInfoTest.propNames, propertyInfoProvider.getPropertyMap(subject));
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
}
