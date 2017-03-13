package com.koch.ambeth.util.util.converter;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.koch.ambeth.util.converter.ByteArrayConverter;
import com.koch.ambeth.util.converter.EncodingInformation;

public class ByteArrayConverterTest
{
	private ByteArrayConverter fixture;

	@Before
	public void setUp() throws Exception
	{
		fixture = new ByteArrayConverter();
	}

	@Test
	public void testConvertValueToType_Plain()
	{
		String expected = "Not yet implemented";
		byte[] converted = (byte[]) fixture.convertValueToType(byte[].class, String.class, expected, null);
		String actual = (String) fixture.convertValueToType(String.class, byte[].class, converted, null);
		assertEquals(expected, actual);
	}

	@Test
	public void testConvertValueToType_Base64()
	{
		String expected = "Not yet implemented";
		byte[] converted = (byte[]) fixture.convertValueToType(byte[].class, String.class, expected, EncodingInformation.SOURCE_PLAIN
				| EncodingInformation.TARGET_BASE64);
		String actual = (String) fixture.convertValueToType(String.class, byte[].class, converted, EncodingInformation.SOURCE_BASE64
				| EncodingInformation.TARGET_PLAIN);
		assertEquals(expected, actual);
	}
}
