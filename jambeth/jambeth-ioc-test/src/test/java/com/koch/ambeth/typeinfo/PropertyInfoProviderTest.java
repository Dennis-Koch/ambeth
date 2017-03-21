package com.koch.ambeth.typeinfo;

/*-
 * #%L
 * jambeth-ioc-test
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.junit.Test;

import com.koch.ambeth.ioc.typeinfo.MethodPropertyInfo;
import com.koch.ambeth.testutil.AbstractIocTest;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;

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
	 * Test method for {@link com.koch.ambeth.ioc.typeinfo.MethodPropertyInfo#getProperties(java.lang.Class, java.lang.String)} .
	 */
	@Test
	public final void testGetPropertyClassString()
	{
		PropertyInfoTest.isPropertyArrayOK(PropertyInfoTest.propNames, propertyInfoProvider.getProperties(MethodPropertyInfo.class));
	}

	/**
	 * Test method for {@link com.koch.ambeth.ioc.typeinfo.MethodPropertyInfo#getProperties(java.lang.Object, java.lang.String)} .
	 */
	@Test
	public final void testGetPropertiesObjectString()
	{
		IPropertyInfo subject = propertyInfoProvider.getProperty(MethodPropertyInfo.class, "Name");
		assertNotNull(propertyInfoProvider.getProperty(subject, "Annotations"));
	}

	/**
	 * Test method for {@link com.koch.ambeth.ioc.typeinfo.MethodPropertyInfo#getProperties(java.lang.Class)} .
	 */
	@Test
	public final void testGetPropertiesClass()
	{
		PropertyInfoTest.isPropertyArrayOK(PropertyInfoTest.propNames, propertyInfoProvider.getProperties(MethodPropertyInfo.class));
	}

	/**
	 * Test method for {@link com.koch.ambeth.ioc.typeinfo.MethodPropertyInfo#getProperties(java.lang.Object)} .
	 */
	@Test
	public final void testGetPropertiesObject()
	{
		IPropertyInfo subject = propertyInfoProvider.getProperty(MethodPropertyInfo.class, "Name");
		PropertyInfoTest.isPropertyArrayOK(PropertyInfoTest.propNames, propertyInfoProvider.getProperties(subject));
	}

	/**
	 * Test method for {@link com.koch.ambeth.ioc.typeinfo.MethodPropertyInfo#getPropertyMap(java.lang.Class)} .
	 */
	@Test
	public final void testGetPropertyMapClass()
	{
		PropertyInfoTest.isPropertyMapOK(PropertyInfoTest.propNames, propertyInfoProvider.getPropertyMap(MethodPropertyInfo.class));
	}

	/**
	 * Test method for {@link com.koch.ambeth.ioc.typeinfo.MethodPropertyInfo#getPropertyMap(java.lang.Object)} .
	 */
	@Test
	public final void testGetPropertyMapObject()
	{
		IPropertyInfo subject = propertyInfoProvider.getProperty(MethodPropertyInfo.class, "Name");
		PropertyInfoTest.isPropertyMapOK(PropertyInfoTest.propNames, propertyInfoProvider.getPropertyMap(subject));
	}

	/**
	 * Test method for {@link com.koch.ambeth.ioc.typeinfo.MethodPropertyInfo#getPropertyNameFor(java.lang.reflect.Method)} .
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
	 * Test method for {@link com.koch.ambeth.ioc.typeinfo.MethodPropertyInfo#getIocProperties(java.lang.Object)} .
	 */
	@Test
	public final void testGetIocProperties()
	{
		IPropertyInfo[] iocProperties = propertyInfoProvider.getIocProperties(MethodPropertyInfo.class);
		PropertyInfoTest.isPropertyArrayOK(PropertyInfoTest.iocPropNames, iocProperties);
	}

	/**
	 * Test method for caching
	 */
	@Test
	public final void testGetIocPropertiesCached()
	{
		IPropertyInfo[] iocProperties = propertyInfoProvider.getIocProperties(MethodPropertyInfo.class);
		PropertyInfoTest.isPropertyArrayOK(PropertyInfoTest.iocPropNames, iocProperties);

		iocProperties = propertyInfoProvider.getIocProperties(MethodPropertyInfo.class);
		PropertyInfoTest.isPropertyArrayOK(PropertyInfoTest.iocPropNames, iocProperties);

		iocProperties = propertyInfoProvider.getProperties(MethodPropertyInfo.class);
		PropertyInfoTest.isPropertyArrayOK(PropertyInfoTest.propNames, iocProperties);
	}

	/**
	 * Test method for caching
	 */
	@Test
	public final void testGetPropertiesCached()
	{
		IPropertyInfo[] iocProperties = propertyInfoProvider.getProperties(MethodPropertyInfo.class);
		PropertyInfoTest.isPropertyArrayOK(PropertyInfoTest.propNames, iocProperties);

		iocProperties = propertyInfoProvider.getProperties(MethodPropertyInfo.class);
		PropertyInfoTest.isPropertyArrayOK(PropertyInfoTest.propNames, iocProperties);

		iocProperties = propertyInfoProvider.getIocProperties(MethodPropertyInfo.class);
		PropertyInfoTest.isPropertyArrayOK(PropertyInfoTest.iocPropNames, iocProperties);

	}
}
