package com.koch.ambeth.util;

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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.koch.ambeth.testutil.AbstractIocTest;
import com.koch.ambeth.testutil.TestRebuildContext;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.IDedicatedConverter;
import com.koch.ambeth.util.IDedicatedConverterExtendable;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.converter.BooleanArrayConverter;

@TestRebuildContext
public class DelegatingConversionHelperTest extends AbstractIocTest
{
	private IDedicatedConverterExtendable fixture;

	private IConversionHelper conversionHelper;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();
		ParamChecker.assertNotNull(fixture, "fixture");
		ParamChecker.assertNotNull(conversionHelper, "conversionHelper");
	}

	public void setDedicatedConverterExtendable(IDedicatedConverterExtendable dedicatedConverterExtendable) throws Exception
	{
		fixture = dedicatedConverterExtendable;
	}

	public void setConversionHelper(IConversionHelper conversionHelper) throws Exception
	{
		this.conversionHelper = conversionHelper;
	}

	@Test
	public void testBasicConversion()
	{
		Object actual = conversionHelper.convertValueToType(Integer.class, 1.5);
		assertSame(Integer.valueOf(1), actual); // same since -128 - 127 are cached by VM
	}

	@Test
	public void testRegisteredConverter()
	{
		boolean[] input = { true, true, false, false, true, false, true };
		String expected = "1100101";
		String actual = conversionHelper.convertValueToType(String.class, input);
		assertEquals(expected, actual);
	}

	@Test
	public void testMultipleRegisteration()
	{
		IDedicatedConverter booleanArrayConverter1 = new BooleanArrayConverter();
		IDedicatedConverter booleanArrayConverter2 = new BooleanArrayConverter();
		try
		{
			fixture.registerDedicatedConverter(booleanArrayConverter1, boolean[].class, String.class);
			fixture.registerDedicatedConverter(booleanArrayConverter2, boolean[].class, String.class);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail();
		}
	}
}
