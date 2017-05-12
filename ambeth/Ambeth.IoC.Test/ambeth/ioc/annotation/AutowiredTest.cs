using De.Osthus.Ambeth.Ioc.Exceptions;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Testutil;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;

namespace De.Osthus.Ambeth.Ioc.Annotation
{
    [TestClass]
    public class AutowiredTest : AbstractIocTest
    {
        public class Bean1
        {
            public Bean2 bean2;

            [Autowired]
            public Bean2 bean2Autowired;

            public Bean3 bean3;

            [Autowired(Optional = true)]
            public Bean3 bean3Autowired;
        }

        public class Bean2
        {
            // Intended blank
        }

        public class Bean3
        {
            // Intended blank
        }

        public class Bean4
        {
            [Autowired(AutowiredTest.bean1Name)]
            public Bean1 bean1Autowired;
        }

	    public const String bean1Name = "bean1", bean2Name = "bean2", bean3Name = "bean3", bean4Name = "bean4";

	    public class AutowiredTestModule : IInitializingModule
	    {
		    public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
		    {
			    beanContextFactory.RegisterBean(bean1Name, typeof(Bean1));
			    beanContextFactory.RegisterBean(bean2Name, typeof(Bean2)).Autowireable<Bean2>();
			    beanContextFactory.RegisterBean(bean3Name, typeof(Bean3));
				
				beanContextFactory.RegisterBean<AutowiredTestBean>().Autowireable<AutowiredTestBean>();
		    }
	    }

	    public class AutowiredTestModule2 : IInitializingModule
	    {
		    public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
		    {
			    beanContextFactory.RegisterBean(bean1Name, typeof(Bean1));
			    beanContextFactory.RegisterBean(bean3Name, typeof(Bean3));
		    }
	    }

	    public class AutowiredTestModule3 : IInitializingModule
	    {
		    public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
		    {
			    beanContextFactory.RegisterBean(bean1Name, typeof(Bean1));
			    beanContextFactory.RegisterBean(bean2Name, typeof(Bean2)).Autowireable<Bean2>();
		    }
	    }

	    public class AutowiredTestModule4 : IInitializingModule
	    {
		    public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
		    {
			    beanContextFactory.RegisterBean(bean1Name, typeof(Bean1));
			    beanContextFactory.RegisterBean(bean2Name, typeof(Bean2)).Autowireable<Bean2>();
			    beanContextFactory.RegisterBean(bean4Name, typeof(Bean4));
		    }
	    }

	    [LogInstance]
	    public new ILogger Log { private get; set; }

	    [TestMethod]
	    [TestModule(typeof(AutowiredTestModule))]
	    public void testAutowired()
	    {
            InitManually(GetType());
		    Bean1 bean1 = BeanContext.GetService<Bean1>(bean1Name);
		    Assert.AssertNull(bean1.bean2);
		    Assert.AssertNotNull(bean1.bean2Autowired);
		    Assert.AssertNull(bean1.bean3);
		    Assert.AssertNull(bean1.bean3Autowired);
	    }

	    [TestMethod]
        [ExpectedException(typeof(BeanContextInitException))]
	    public void testAutowiredNotOptional()
	    {
		    IServiceContext beanContext = this.BeanContext.CreateService(typeof(AutowiredTestModule2));
		    try
		    {
			    Bean1 bean1 = beanContext.GetService<Bean1>(bean1Name);
			    Assert.AssertNull(bean1.bean2);
			    Assert.AssertNull(bean1.bean2Autowired);
			    Assert.AssertNull(bean1.bean3);
			    Assert.AssertNull(bean1.bean3Autowired);
		    }
		    finally
		    {
			    beanContext.Dispose();
		    }
	    }

	    [TestMethod]
	    public void testAutowiredOptional()
	    {
		    IServiceContext beanContext = this.BeanContext.CreateService(typeof(AutowiredTestModule3));
		    try
		    {
			    Bean1 bean1 = beanContext.GetService<Bean1>(bean1Name);
			    Assert.AssertNull(bean1.bean2);
			    Assert.AssertNotNull(bean1.bean2Autowired);
			    Assert.AssertNull(bean1.bean3);
			    Assert.AssertNull(bean1.bean3Autowired);
		    }
		    finally
		    {
			    beanContext.Dispose();
		    }
	    }

	    [TestMethod]
	    public void testAutowiredByName()
	    {
		    IServiceContext beanContext = this.BeanContext.CreateService(typeof(AutowiredTestModule4));
		    try
		    {
			    Bean1 bean1 = beanContext.GetService<Bean1>(bean1Name);
			    Assert.AssertNull(bean1.bean2);
			    Assert.AssertNotNull(bean1.bean2Autowired);
			    Assert.AssertNull(bean1.bean3);
			    Assert.AssertNull(bean1.bean3Autowired);
		    }
		    finally
		    {
			    beanContext.Dispose();
		    }
	    }

		[TestMethod]
	    [TestModule(typeof(AutowiredTestModule))]
		public void autowiredVisibility()
		{
			AutowiredTestBean bean = BeanContext.GetService<AutowiredTestBean>();
			Assert.AssertSame(BeanContext, bean.getBeanContextPrivate());
			Assert.AssertNull(bean.getBeanContextPrivateSetter());
			Assert.AssertSame(BeanContext, bean.getBeanContextPrivateSetterAutowired());

			Assert.AssertSame(BeanContext, bean.getBeanContextProtected());
			Assert.AssertNull(bean.getBeanContextProtectedSetter());
			Assert.AssertSame(BeanContext, bean.getBeanContextProtectedSetterAutowired());

			Assert.AssertSame(BeanContext, bean.getBeanContextPublic());
			Assert.AssertSame(BeanContext, bean.getBeanContextPublicSetter());
		}
    }
}