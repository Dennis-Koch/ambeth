package com.koch.ambeth.util.util.converter;

/*-
 * #%L
 * jambeth-util-test
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

import org.junit.Before;
import org.junit.Test;

import com.koch.ambeth.util.converter.CharArrayConverter;
import com.koch.ambeth.util.converter.EncodingInformation;

public class CharArrayConverterTest
{
	private CharArrayConverter fixture;

	@Before
	public void setUp() throws Exception
	{
		fixture = new CharArrayConverter();
	}

	@Test
	public void testConvertValueToType_Plain()
	{
		String expected = "Not yet implemented";
		char[] converted = (char[]) fixture.convertValueToType(char[].class, String.class, expected, null);
		String actual = (String) fixture.convertValueToType(String.class, char[].class, converted, null);
		assertEquals(expected, actual);
	}

	@Test
	public void testConvertValueToType_Base64()
	{
		String expected = "Not yet implemented";
		char[] converted = (char[]) fixture.convertValueToType(char[].class, String.class, expected, EncodingInformation.SOURCE_PLAIN
				| EncodingInformation.TARGET_BASE64);
		String actual = (String) fixture.convertValueToType(String.class, char[].class, converted, EncodingInformation.SOURCE_BASE64
				| EncodingInformation.TARGET_PLAIN);
		assertEquals(expected, actual);
	}
}
