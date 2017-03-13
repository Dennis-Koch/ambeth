package com.koch.ambeth.testutil;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.testutil.AbstractIocTest;
import com.koch.ambeth.testutil.IPropertiesProvider;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.testutil.AmbethIocRunnerTest.MyTestModule;
import com.koch.ambeth.util.IPrintable;

@TestPropertiesList({ @TestProperties(name = AmbethIocRunnerTest.TEST_PROPERTY_NAME, value = "value1"),
		@TestProperties(name = AmbethIocRunnerTest.TEST_PROPERTY_NAME, value = "value2"), @TestProperties(name = "test.string.empty", value = ""),
		@TestProperties(name = "test.novalue") })
@TestModule(MyTestModule.class)
public class AmbethIocRunnerTest extends AbstractIocTest
{
	public static final String TEST_PROPERTY_NAME = "test.property";

	public static class MyTestModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerAutowireableBean(IPrintable.class, MyPrintable.class);
		}
	}

	public static class MyPrintable implements IPrintable
	{
		@Override
		public void toString(StringBuilder sb)
		{
			sb.append(toString());
		}
	}

	public static class MyPropertiesProvider implements IPropertiesProvider
	{
		@Override
		public void fillProperties(Properties props)
		{
			// props.put(ConfigurationConstants.mappingResource, "com/koch/ambeth/query/Query_orm.xml");
		}
	}

	protected HashMap<Object, Object> nameToValueMap = new HashMap<Object, Object>();

	@Property(name = "abc", defaultValue = "abc0")
	protected String value;

	@Autowired
	protected IPrintable printable;

	@Property(name = TEST_PROPERTY_NAME)
	private String definedMultipleTimes;

	@Property(name = "test.string.empty")
	private String emptyString;

	@Property(name = "test.string.empty", defaultValue = "empty")
	private String emptyStringWithDefault;

	@Property(name = "test.string.undefined", defaultValue = "myDefault")
	private String undefinedWithDefault;

	@Property(name = "test.novalue")
	private String novalue;

	@Test
	public void autowiringIntoTest() throws Exception
	{
		Assert.assertNotNull(printable);
	}

	@Test
	public void testTestProperties_defaultValue()
	{
		Assert.assertEquals("abc0", value);
	}

	@Test
	public void testTestProperties_definedMultipleTimes()
	{
		Assert.assertEquals("value2", definedMultipleTimes);
	}

	/**
	 * Test for ticket 2756. Empty Strings were ignored, but should not.
	 */
	@Test
	public void testTestProperties_emptyString()
	{
		Assert.assertEquals("", emptyString);
		Assert.assertEquals("", emptyStringWithDefault);
	}

	@Test
	public void testTestProperties_undefinedString()
	{
		Assert.assertEquals("myDefault", undefinedWithDefault);
	}

	@Test
	public void testTestProperties_noValue()
	{
		Assert.assertEquals("", novalue);
	}
}
