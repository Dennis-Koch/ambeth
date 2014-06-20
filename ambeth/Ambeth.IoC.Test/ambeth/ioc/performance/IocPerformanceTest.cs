using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Testutil;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;

namespace De.Osthus.Ambeth.Ioc.Performance
{
    [TestClass]
    [TestProperties(Name = IocConfigurationConstants.TrackDeclarationTrace, Value = "false")]
    public class IocPerformanceTest : AbstractIocTest
    {
        public const String count_prop = "count_prop";

        [LogInstance]
        public ILogger Log { private get; set; }

        [TestMethod]
        [TestProperties(Name = IocPerformanceTest.count_prop, Value = "50000")]
        public void performance()
        {
            using (IServiceContext childContext = BeanContext.CreateService(typeof(IocPerformanceTestModule)))
            {
                Assert.AssertEquals(50000, childContext.GetObjects<TestBean>().Count);
            }
        }
    }
}
