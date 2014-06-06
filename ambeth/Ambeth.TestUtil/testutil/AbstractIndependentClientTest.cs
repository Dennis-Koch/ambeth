using De.Osthus.Ambeth.Cache.Config;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace De.Osthus.Ambeth.Testutil
{
    /// <summary>
    /// Abstract test class easing usage of IOC containers with access to entity meta data in test scenarios. Isolated modules can be registered with the 
    /// <code>TestModule</code> annotation. The test itself will be registered as a bean within the IOC container. Therefore it can consume any components 
    /// for testing purpose and behave like a productively bean.
    /// 
    /// In addition to registering custom modules the environment can be constructed for specific testing purpose with the <code>TestProperties</code> annotation.
    /// Multiple properties can be wrapped using the <code>TestPropertiesList</code> annotation.
    /// 
    /// All annotations can be used on test class level as well as on test method level. In ambiguous scenarios the method annotations will gain precedence.
    /// </summary>
    [TestClass]
    [TestProperties(Name = ServiceConfigurationConstants.GenericTransferMapping, Value = "true")]
    [TestProperties(Name = ServiceConfigurationConstants.IndependentMetaData, Value = "true")]
    [TestProperties(Name = ServiceConfigurationConstants.NetworkClientMode, Value = "false")]
    [TestProperties(Name = CacheConfigurationConstants.CacheServiceRegistryActive, Value = "true")]
    [TestProperties(Name = CacheConfigurationConstants.CacheServiceBeanActive, Value = "true")]
    [TestFrameworkModule(typeof(TestUtilModule), typeof(BytecodeModule), typeof(CacheModule), typeof(CacheBytecodeModule),
        typeof(CacheDataChangeModule), typeof(CompositeIdModule), typeof(EventModule), typeof(MergeModule),
        typeof(MappingModule), typeof(ObjectCopierModule), typeof(ServiceModule))]
    public abstract class AbstractIndependentClientTest : AbstractIocTest
    {
        /// <summary>
        /// Workaround to get all needed assembly dlls copied to the test folder.
        /// </summary>
        /// <param name="context"></param>
        [AssemblyInitialize()]
        public static void AssemblyInit(TestContext context)
        {
            IInitializingModule bytecode = new BytecodeModule();
            IInitializingModule cacheBytecode = new CacheBytecodeModule();
            IInitializingModule cacheDataChange = new CacheDataChangeModule();
            IInitializingModule mapping = new MappingModule();
            IInitializingModule mergeBytecode = new CompositeIdModule();
        }
    }
}