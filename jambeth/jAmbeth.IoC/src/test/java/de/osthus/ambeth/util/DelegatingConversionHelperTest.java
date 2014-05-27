package de.osthus.ambeth.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.Test;

import de.osthus.ambeth.testutil.AbstractIocTest;
import de.osthus.ambeth.testutil.TestRebuildContext;
import de.osthus.ambeth.util.converter.BooleanArrayConverter;

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
