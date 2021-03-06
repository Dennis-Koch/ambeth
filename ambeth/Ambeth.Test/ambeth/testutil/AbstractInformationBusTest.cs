using De.Osthus.Ambeth.Bytecode.Config;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Log;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace De.Osthus.Ambeth.Testutil
{
    // Workaround to get MSTest.exe to copy all needed dlls and not only the directly referenced ones.
    [DeploymentItem(@"Ambeth.Bytecode.dll")]
    [DeploymentItem(@"Ambeth.Cache.dll")]
    [DeploymentItem(@"Ambeth.Cache.Bytecode.dll")]
    [DeploymentItem(@"Ambeth.Cache.DataChange.dll")]
    //[DeploymentItem(@"Ambeth.Cache.Stream.dll")]
    [DeploymentItem(@"Ambeth.DataChange.dll")]
    [DeploymentItem(@"Ambeth.Event.dll")]
    [DeploymentItem(@"Ambeth.Event.DataChange.dll")]
    [DeploymentItem(@"Ambeth.Mapping.dll")]
    [DeploymentItem(@"Ambeth.Merge.dll")]
    [DeploymentItem(@"Ambeth.Merge.Bytecode.dll")]
    [DeploymentItem(@"Ambeth.Security.dll")]
    //[DeploymentItem(@"Ambeth.Sensor.dll")]
    [DeploymentItem(@"Ambeth.Service.dll")]
    //[DeploymentItem(@"Ambeth.Stream.dll")]
    [TestFrameworkModule(typeof(BytecodeModule), typeof(CacheModule), typeof(CacheBytecodeModule), typeof(CacheDataChangeModule), /*typeof(CacheStreamModule), */typeof(DataChangeModule),
            typeof(EventModule), typeof(EventDataChangeModule), typeof(MappingModule), typeof(MergeModule), typeof(MergeBytecodeModule), typeof(ObjectCopierModule), typeof(SecurityModule), typeof(PrivilegeModule),
        /*typeof(SensorModule), */typeof(ServiceModule)/*, typeof(StreamModule)*/)]
    [TestProperties(Name = ServiceConfigurationConstants.IndependentMetaData, Value = "true")]
    [TestProperties(Name = BytecodeConfigurationConstants.EnhancementTraceDirectory, Value = "${user.home}/ambeth-temp")]
    public abstract class AbstractInformationBusTest : AbstractIocTest
    {
	    [LogInstance]
	    public new ILogger Log { private get; set; }
    }
}