package com.koch.ambeth.example.junit;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.koch.ambeth.testutil.AbstractIocTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.util.ParamChecker;

@TestModule(ExampleTestModule.class)
public class ExampleTest2 extends AbstractIocTest
{
	protected MyBean1 myBean1; // Autowired Bean

	protected MyBean2 myBean2; // Benannte Bean ohne Autowiring

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(myBean1, "myBean1");

		ParamChecker.assertNull(myBean2, "myBean2");
		myBean2 = beanContext.getService("myBean2", MyBean2.class);
	}

	public void setMyBean1(MyBean1 myBean1)
	{
		this.myBean1 = myBean1;
	}

	public void setMyBean2(MyBean2 myBean2)
	{
		this.myBean2 = myBean2;
	}

	@Test
	public void test()
	{
		assertNotNull(myBean2);
	}
}
