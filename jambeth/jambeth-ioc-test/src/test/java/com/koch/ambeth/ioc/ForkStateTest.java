package com.koch.ambeth.ioc;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.ioc.ForkStateTest.MyModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import com.koch.ambeth.testutil.AbstractIocTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.util.state.IStateRollback;

@TestModule(MyModule.class)
public class ForkStateTest extends AbstractIocTest {

	public static class MyModule implements IInitializingModule {
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
			beanContextFactory.registerBean(MyBean.class).autowireable(MyBean.class);
		}
	}

	public static class MyBean implements IThreadLocalCleanupBean {
		protected final ThreadLocal<Boolean> valueTL = new ThreadLocal<>();

		@Override
		public void cleanupThreadLocal() {

		}
	}

	@Autowired
	protected MyBean myBean;

	@Autowired
	protected IThreadLocalCleanupController threadLocalCleanupController;

	@Test
	public void test() {
		Assert.assertNull(myBean.valueTL.get());
		IStateRollback rollback = threadLocalCleanupController.pushThreadLocalState();
		try {
			myBean.valueTL.set(Boolean.TRUE);
		}
		finally {
			rollback.rollback();
		}
		Assert.assertNull(myBean.valueTL.get());
	}
}
