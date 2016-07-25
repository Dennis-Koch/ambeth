package de.osthus.ambeth.ioc.beanruntime;

import org.junit.Test;

import de.osthus.ambeth.testutil.AbstractIocTest;

public class BeanRuntimeTest extends AbstractIocTest
{
	public static abstract class MyBean
	{
		public abstract int getValue();
	}

	protected MyBean create()
	{
		return beanContext.registerBean(MyBean.class).finish();
	}

	@Test
	public void testAbstractRuntimeInstantiation()
	{
		create();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testAbstractRuntimeInstantiationFails()
	{
		create().getValue();
	}
}
