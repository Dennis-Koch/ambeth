package de.osthus.ambeth.example.junit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.osthus.ambeth.testutil.AbstractIocTest;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.util.ParamChecker;

@TestProperties(name = "text.for.MyBean1", value = "Hello Test World!")
@TestModule(ExampleTestModule.class)
public class ExampleTest3 extends AbstractIocTest
{
	protected MyBean1 myBean1;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(myBean1, "myBean1");
	}

	public void setMyBean1(MyBean1 myBean1)
	{
		this.myBean1 = myBean1;
	}

	@Test
	public void test()
	{
		assertEquals("Hello Test World!", myBean1.getText());
	}
}
