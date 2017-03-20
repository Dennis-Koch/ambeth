package com.koch.ambeth.testutil;

import org.junit.runner.RunWith;

import com.koch.ambeth.bytecode.ioc.BytecodeModule;
import com.koch.ambeth.cache.bytecode.ioc.CacheBytecodeModule;
import com.koch.ambeth.cache.datachange.ioc.CacheDataChangeModule;
import com.koch.ambeth.cache.ioc.CacheModule;
import com.koch.ambeth.cache.stream.ioc.CacheStreamModule;
import com.koch.ambeth.datachange.ioc.DataChangeModule;
import com.koch.ambeth.event.datachange.ioc.EventDataChangeModule;
import com.koch.ambeth.event.ioc.EventModule;
import com.koch.ambeth.expr.ioc.ExprModule;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.mapping.ioc.MappingModule;
import com.koch.ambeth.merge.bytecode.ioc.MergeBytecodeModule;
import com.koch.ambeth.merge.ioc.MergeModule;
import com.koch.ambeth.merge.ioc.ObjectCopierModule;
import com.koch.ambeth.security.bytecode.ioc.SecurityBytecodeModule;
import com.koch.ambeth.security.ioc.SecurityModule;
import com.koch.ambeth.sensor.ioc.SensorModule;
import com.koch.ambeth.service.ioc.ServiceModule;
import com.koch.ambeth.stream.ioc.StreamModule;
import com.koch.ambeth.testutil.AbstractIocTest;
import com.koch.ambeth.testutil.TestFrameworkModule;

@RunWith(AmbethInformationBusRunner.class)
@TestFrameworkModule({ BytecodeModule.class, CacheModule.class, CacheBytecodeModule.class, CacheDataChangeModule.class, CacheStreamModule.class,
		DataChangeModule.class, EventModule.class, EventDataChangeModule.class, ExprModule.class, MappingModule.class, MergeModule.class,
		MergeBytecodeModule.class, ObjectCopierModule.class, SecurityModule.class, SecurityBytecodeModule.class, SensorModule.class, ServiceModule.class,
		StreamModule.class })
public abstract class AbstractInformationBusTest extends AbstractIocTest
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;
}
