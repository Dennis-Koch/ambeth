using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Cache.Config;
using De.Osthus.Ambeth.CompositeId;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Copy;
using De.Osthus.Ambeth.Event.Config;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Testutil;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Util;
using De.Osthus.Minerva.Ioc;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace De.Osthus.Ambeth.Test
{
    [TestProperties(Name = CacheConfigurationConstants.AsyncPropertyChangeActive, Value = "true")]
    [TestProperties(Name = CacheConfigurationConstants.FirstLevelCacheType, Value = "SINGLETON")]
    [TestProperties(Name = EventConfigurationConstants.PollingActive, Value = "true")]
    [TestProperties(Name = ServiceConfigurationConstants.IndependentMetaData, Value = "false")]
    [TestProperties(Name = ServiceConfigurationConstants.GenericTransferMapping, Value = "false")]
    [TestProperties(Name = ServiceConfigurationConstants.NetworkClientMode, Value = "true")]
    [TestProperties(Name = ServiceConfigurationConstants.ServiceBaseUrl, Value = "${" + ServiceConfigurationConstants.ServiceProtocol + "}://"
                + "${" + ServiceConfigurationConstants.ServiceHostName + "}" + ":"
                + "${" + ServiceConfigurationConstants.ServiceHostPort + "}"
                + "${" + ServiceConfigurationConstants.ServicePrefix + "}")]
    [TestProperties(Name = ServiceConfigurationConstants.ServiceProtocol, Value = "http")]
    [TestProperties(Name = ServiceConfigurationConstants.ServiceHostName, Value = "localhost.")]
    [TestProperties(Name = ServiceConfigurationConstants.ServiceHostPort, Value = "9080")]
    [TestProperties(Name = ServiceWCFConfigurationConstants.TransferObjectsScope, Value = ".+")]
    [TestFrameworkModule(typeof(MinervaCoreBootstrapModule), typeof(BytecodeModule), typeof(CacheBootstrapModule), typeof(CacheBytecodeModule), typeof(CacheDataChangeBootstrapModule),
        typeof(CompositeIdModule), typeof(EventBootstrapModule), typeof(MergeBootstrapModule), typeof(ObjectCopierModule),
        typeof(RESTBootstrapModule), typeof(SecurityBootstrapModule), typeof(ServiceBootstrapModule), typeof(XmlBootstrapModule))]
    [TestModule(typeof(RemoteTestModule))]
    [TestRebuildContext]
    [TestClass]
    public abstract class AbstractRemoteTest : AbstractIocTest
    {
        [Autowired]
        public ICache Cache { protected get; set; }

        [Autowired]
        public ICacheFactory CacheFactory { protected get; set; }

        [Autowired]
        public IEntityFactory EntityFactory { protected get; set; }

        [Autowired]
        public IGuiThreadHelper GuiThreadHelper { protected get; set; }

        [Autowired]
        public IMergeController MergeController { protected get; set; }

        [Autowired]
        public IMergeProcess MergeProcess { protected get; set; }
        
        [Autowired]
        public IPrefetchHelper PrefetchHelper { protected get; set; }

        [Autowired]
        public IProxyHelper ProxyHelper { protected get; set; }

        [Autowired]
        public IRevertChangesHelper RevertChangesHelper { protected get; set; }
        
        protected void WaitForUI()
        {
            GuiThreadHelper.InvokeInGuiAndWait(delegate()
            {
                // Intended blank
            });
        }
    }
}