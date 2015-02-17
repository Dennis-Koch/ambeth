package de.osthus.ambeth.testutil;

import org.junit.runner.RunWith;

import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.BytecodeModule;
import de.osthus.ambeth.ioc.CacheBytecodeModule;
import de.osthus.ambeth.ioc.CacheDataChangeModule;
import de.osthus.ambeth.ioc.CacheModule;
import de.osthus.ambeth.ioc.CacheStreamModule;
import de.osthus.ambeth.ioc.DataChangeModule;
import de.osthus.ambeth.ioc.EventDataChangeModule;
import de.osthus.ambeth.ioc.EventModule;
import de.osthus.ambeth.ioc.ExprModule;
import de.osthus.ambeth.ioc.MappingModule;
import de.osthus.ambeth.ioc.MergeBytecodeModule;
import de.osthus.ambeth.ioc.MergeModule;
import de.osthus.ambeth.ioc.ObjectCopierModule;
import de.osthus.ambeth.ioc.SecurityBytecodeModule;
import de.osthus.ambeth.ioc.SecurityModule;
import de.osthus.ambeth.ioc.SensorModule;
import de.osthus.ambeth.ioc.ServiceModule;
import de.osthus.ambeth.ioc.StreamModule;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

@RunWith(AmbethInformationBusRunner.class)
@TestFrameworkModule({ BytecodeModule.class, CacheModule.class, CacheBytecodeModule.class, CacheDataChangeModule.class, CacheStreamModule.class,
		DataChangeModule.class, EventModule.class, EventDataChangeModule.class, ExprModule.class, MappingModule.class, MergeModule.class,
		MergeBytecodeModule.class, ObjectCopierModule.class, SecurityModule.class, SecurityBytecodeModule.class, SensorModule.class, ServiceModule.class,
		StreamModule.class })
@TestProperties(name = ServiceConfigurationConstants.IndependentMetaData, value = "true")
public abstract class AbstractInformationBusTest extends AbstractIocTest
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;
}
