package de.osthus.ambeth.ioc.annotation;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.IocModule;
import de.osthus.ambeth.ioc.exception.BeanContextInitException;
import de.osthus.ambeth.ioc.factory.BeanContextFactory;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.testutil.AbstractIocTest;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestRebuildContext;
import de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate;

@TestRebuildContext
public class AutowiredTest extends AbstractIocTest
{
	public static class Bean1
	{
		protected Bean2 bean2;

		@Autowired
		protected Bean2 bean2Autowired;

		protected Bean3 bean3;

		@Autowired(optional = true)
		protected Bean3 bean3Autowired;
	}

	public static class Bean2
	{
		// Intended blank
	}

	public static class Bean3
	{
		// Intended blank
	}

	public static class Bean4
	{
		@Autowired(bean1Name)
		protected Bean1 bean1Autowired;
	}

	public static class Bean5
	{
		@Autowired(value = bean1Name, fromContext = fromContextName1)
		protected Bean1 bean1AutowiredForeignContext;

		@Autowired(value = bean1Name)
		protected Bean1 bean1Autowired;
	}

	public static final String fromContextName1 = "otherContext1", fromContextName2 = "otherContext2";

	public static final String bean1Name = "bean1", bean2Name = "bean2", bean3Name = "bean3", bean4Name = "bean4";

	public static class AutowiredTestModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerBean(bean1Name, Bean1.class);
			beanContextFactory.registerBean(bean2Name, Bean2.class).autowireable(Bean2.class);
			beanContextFactory.registerBean(bean3Name, Bean3.class);

			beanContextFactory.registerBean(AutowiredTestBean.class).autowireable(AutowiredTestBean.class);
		}
	}

	public static class AutowiredTestModule2 implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerBean(bean1Name, Bean1.class);
			beanContextFactory.registerBean(bean3Name, Bean3.class);
		}
	}

	public static class AutowiredTestModule3 implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerBean(bean1Name, Bean1.class);
			beanContextFactory.registerBean(bean2Name, Bean2.class).autowireable(Bean2.class);
		}
	}

	public static class AutowiredTestModule4 implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerBean(bean1Name, Bean1.class);
			beanContextFactory.registerBean(bean2Name, Bean2.class).autowireable(Bean2.class);
			beanContextFactory.registerBean(bean4Name, Bean4.class);
		}
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Test
	@TestModule(AutowiredTestModule.class)
	public void testAutowired()
	{
		Bean1 bean1 = beanContext.getService(bean1Name, Bean1.class);
		Assert.assertNull(bean1.bean2);
		Assert.assertNotNull(bean1.bean2Autowired);
		Assert.assertNull(bean1.bean3);
		Assert.assertNull(bean1.bean3Autowired);
	}

	@Test(expected = BeanContextInitException.class)
	public void testAutowiredNotOptional()
	{
		beanContext.createService(AutowiredTestModule2.class);
	}

	@Test
	public void testAutowiredOptional()
	{
		IServiceContext beanContext = this.beanContext.createService(AutowiredTestModule3.class);
		try
		{
			Bean1 bean1 = beanContext.getService(bean1Name, Bean1.class);
			Assert.assertNull(bean1.bean2);
			Assert.assertNotNull(bean1.bean2Autowired);
			Assert.assertNull(bean1.bean3);
			Assert.assertNull(bean1.bean3Autowired);
		}
		finally
		{
			beanContext.dispose();
		}
	}

	@Test
	public void testAutowiredByName()
	{
		IServiceContext beanContext = this.beanContext.createService(AutowiredTestModule4.class);
		try
		{
			Bean1 bean1 = beanContext.getService(bean1Name, Bean1.class);
			Assert.assertNull(bean1.bean2);
			Assert.assertNotNull(bean1.bean2Autowired);
			Assert.assertNull(bean1.bean3);
			Assert.assertNull(bean1.bean3Autowired);
		}
		finally
		{
			beanContext.dispose();
		}
	}

	@Test
	@TestModule(AutowiredTestModule.class)
	public void autowiredVisibility()
	{
		AutowiredTestBean bean = beanContext.getService(AutowiredTestBean.class);
		Assert.assertSame(beanContext, bean.getBeanContextPrivate());
		Assert.assertNull(bean.getBeanContextPrivateSetter());
		Assert.assertSame(beanContext, bean.getBeanContextPrivateSetterAutowired());

		Assert.assertSame(beanContext, bean.getBeanContextProtected());
		Assert.assertNull(bean.getBeanContextProtectedSetter());
		Assert.assertSame(beanContext, bean.getBeanContextProtectedSetterAutowired());

		Assert.assertSame(beanContext, bean.getBeanContextPublic());
		Assert.assertSame(beanContext, bean.getBeanContextPublicSetter());
	}

	@Test
	@TestModule(AutowiredTestModule.class)
	public void testAutowiredFromContext()
	{
		final String bean5Name_1 = "bean5Name_1";
		final String bean5Name_2 = "bean5Name_2";

		final IServiceContext otherContext2 = BeanContextFactory.createBootstrap(IocModule.class).createService(AutowiredTestModule.class);
		try
		{
			IServiceContext otherContext = BeanContextFactory.createBootstrap(IocModule.class).createService(
					new IBackgroundWorkerParamDelegate<IBeanContextFactory>()
					{
						@Override
						public void invoke(IBeanContextFactory state) throws Throwable
						{
							state.registerExternalBean(fromContextName1, beanContext);
							state.registerExternalBean(fromContextName2, otherContext2);

							state.registerBean(bean5Name_1, Bean5.class);

							state.registerBean(bean5Name_2, Bean5.class).propertyRefFromContext("Bean1AutowiredForeignContext", fromContextName2, bean1Name);
						}
					}, AutowiredTestModule.class);
			try
			{
				Bean5 bean5_1 = otherContext.getService(bean5Name_1, Bean5.class);
				Bean5 bean5_2 = otherContext.getService(bean5Name_2, Bean5.class);

				Bean1 bean1 = beanContext.getService(bean1Name, Bean1.class);
				Bean1 otherContext1_bean1 = otherContext.getService(bean1Name, Bean1.class);
				Bean1 otherContext2_bean1 = otherContext2.getService(bean1Name, Bean1.class);

				Assert.assertNotSame(bean1, otherContext1_bean1);
				Assert.assertNotSame(bean1, otherContext2_bean1);
				Assert.assertNotSame(otherContext1_bean1, otherContext2_bean1);

				Assert.assertSame(otherContext1_bean1, bean5_1.bean1Autowired);
				Assert.assertSame(bean1, bean5_1.bean1AutowiredForeignContext);

				Assert.assertSame(otherContext1_bean1, bean5_2.bean1Autowired);
				Assert.assertNotSame(otherContext2_bean1, bean5_2.bean1Autowired);
				Assert.assertSame(otherContext2_bean1, bean5_2.bean1AutowiredForeignContext);
			}
			finally
			{
				otherContext.getRoot().dispose();
			}
		}
		finally
		{
			otherContext2.dispose();
		}
	}
}
