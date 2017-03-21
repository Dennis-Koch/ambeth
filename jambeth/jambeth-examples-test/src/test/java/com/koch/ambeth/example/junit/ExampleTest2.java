package com.koch.ambeth.example.junit;

/*-
 * #%L
 * jambeth-examples-test
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
