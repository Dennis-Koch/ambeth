package de.osthus.ambeth.testutil;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.testutil.AmbethIocRunnerTest.MyTestModule;
import de.osthus.ambeth.util.IPrintable;

@TestPropertiesList({ @TestProperties(name = AmbethIocRunnerTest.TestPropertyName, value = "value1"),
		@TestProperties(name = AmbethIocRunnerTest.TestPropertyName, value = "value2"), @TestProperties(name = "test.string.empty", value = "") })
@TestModule(MyTestModule.class)
public class AmbethIocRunnerTest extends AbstractIocTest
{
	public static final String TestPropertyName = "test.property";

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
			// props.put(ConfigurationConstants.mappingResource, "de/osthus/ambeth/query/Query_orm.xml");
		}
	}

	protected HashMap<Object, Object> nameToValueMap = new HashMap<Object, Object>();

	protected String value;

	protected IPrintable printable;

	@Property(name = "test.string.empty")
	private String emptyString;

	@Property(name = "test.string.empty", defaultValue = "empty")
	private String emptyStringWithDefault;

	public void setPrintable(IPrintable printable)
	{
		this.printable = printable;
	}

	@Property(name = "abc", defaultValue = "abc0")
	public void setValue(String value)
	{
		this.value = value;
	}

	@Test
	public void autowiringIntoTest() throws Exception
	{
		Assert.assertNotNull(printable);
	}

	@Test
	public void testTestProperties_emptyString()
	{
		Assert.assertEquals("", emptyString);
		Assert.assertEquals("", emptyStringWithDefault);
	}
}
