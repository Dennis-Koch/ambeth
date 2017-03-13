package com.koch.ambeth.example.junit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.koch.ambeth.testutil.AbstractIocTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.util.ParamChecker;

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
