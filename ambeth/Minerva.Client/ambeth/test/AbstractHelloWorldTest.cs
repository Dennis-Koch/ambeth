using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Helloworld.Service;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Testutil;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace De.Osthus.Ambeth.Test
{
    [TestProperties(Name = ServiceConfigurationConstants.ServicePrefix, Value = "/helloworld")]
    [TestModule(typeof(HelloWorldTestModule))]
    [TestClass]
    public abstract class AbstractHelloWorldTest : AbstractRemoteTest
    {
        [Autowired]
        public IHelloWorldService HelloWorldService { protected get; set; }
    }
}