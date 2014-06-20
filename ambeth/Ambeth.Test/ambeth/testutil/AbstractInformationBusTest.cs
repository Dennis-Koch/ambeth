using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Log;
using System;

namespace De.Osthus.Ambeth.Testutil
{
    [TestFrameworkModule(typeof(BytecodeModule), typeof(CacheModule), typeof(CacheBytecodeModule), typeof(CacheDataChangeModule), /*typeof(CacheStreamModule), */typeof(DataChangeModule),
		    typeof(EventModule), typeof(EventDataChangeModule), typeof(MappingModule), typeof(MergeModule), typeof(CompositeIdModule), typeof(ObjectCopierModule), typeof(SecurityModule),
        /*typeof(SensorModule), */typeof(ServiceModule)/*, typeof(StreamModule)*/)]
    [TestProperties(Name = ServiceConfigurationConstants.IndependentMetaData, Value = "true")]
    public abstract class AbstractInformationBusTest : AbstractIocTest
    {
	    [LogInstance]
	    public ILogger Log { private get; set; }
    }
}