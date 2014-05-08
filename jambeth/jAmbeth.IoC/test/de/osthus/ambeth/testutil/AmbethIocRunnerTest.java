package de.osthus.ambeth.testutil;

import java.util.HashMap;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.testutil.AmbethIocRunnerTest.MyTestModule;
import de.osthus.ambeth.util.IPrintable;

@TestPropertiesList({ @TestProperties(name = AmbethIocRunnerTest.TestPropertyName, value = "value1"),
		@TestProperties(name = AmbethIocRunnerTest.TestPropertyName, value = "value2") })
@RunWith(AmbethIocRunner.class)
@TestModule(MyTestModule.class)
public class AmbethIocRunnerTest implements IInitializingBean
{
	public static final String TestPropertyName = "test.property";

	public static class MyTestModule implements IInitializingModule
	{
		public static class MyPrintable implements IPrintable
		{
			@Override
			public void toString(StringBuilder sb)
			{
				sb.append(toString());
			}
		}

		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerAutowireableBean(IPrintable.class, MyPrintable.class);
		}
	}

	public static class MyPropertiesProvider implements IPropertiesProvider
	{
		@Override
		public void fillProperties(Properties props)
		{
			// props.put(ConfigurationConstants.mappingResource, "de/osthus/ambeth/query/Query_orm.xml");
		}
	};

	protected HashMap<Object, Object> nameToValueMap = new HashMap<Object, Object>();

	protected String value;

	protected IPrintable printable;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
	}

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
}
