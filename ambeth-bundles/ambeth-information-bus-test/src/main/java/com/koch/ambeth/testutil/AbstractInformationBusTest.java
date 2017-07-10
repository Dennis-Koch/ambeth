package com.koch.ambeth.testutil;

/*-
 * #%L
 * jambeth-information-bus-test
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

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
import com.koch.ambeth.mapping.ioc.MappingModule;
import com.koch.ambeth.merge.bytecode.ioc.MergeBytecodeModule;
import com.koch.ambeth.merge.ioc.ChangeControllerModule;
import com.koch.ambeth.merge.ioc.MergeModule;
import com.koch.ambeth.merge.ioc.ObjectCopierModule;
import com.koch.ambeth.security.bytecode.ioc.SecurityBytecodeModule;
import com.koch.ambeth.security.ioc.SecurityModule;
import com.koch.ambeth.sensor.ioc.SensorModule;
import com.koch.ambeth.service.ioc.ServiceModule;
import com.koch.ambeth.stream.ioc.StreamModule;

@RunWith(AmbethInformationBusRunner.class)
@TestFrameworkModule({ BytecodeModule.class, CacheModule.class, CacheBytecodeModule.class,
		CacheDataChangeModule.class, CacheStreamModule.class, ChangeControllerModule.class,
		DataChangeModule.class, EventModule.class, EventDataChangeModule.class, ExprModule.class,
		MappingModule.class, MergeModule.class, MergeBytecodeModule.class, ObjectCopierModule.class,
		SecurityModule.class, SecurityBytecodeModule.class, SensorModule.class, ServiceModule.class,
		StreamModule.class })
public abstract class AbstractInformationBusTest extends AbstractIocTest {
}
