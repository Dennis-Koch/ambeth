package com.koch.ambeth.bytecode.ioc;

/*-
 * #%L
 * jambeth-bytecode
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

import com.koch.ambeth.bytecode.IBytecodeClassLoader;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehavior;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehaviorExtendable;
import com.koch.ambeth.bytecode.core.BytecodeClassLoader;
import com.koch.ambeth.bytecode.core.BytecodeEnhancer;
import com.koch.ambeth.event.IEventListenerExtendable;
import com.koch.ambeth.ioc.IFrameworkModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.bytecode.IBytecodeEnhancer;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.bytecode.IBytecodePrinter;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;
import io.toolisticon.spiap.api.SpiService;

@SpiService(IFrameworkModule.class)
@FrameworkModule
public class BytecodeModule implements IFrameworkModule {
    public static IBeanConfiguration addDefaultBytecodeBehavior(IBeanContextFactory beanContextFactory, Class<? extends IBytecodeBehavior> behaviorType) {
        var behaviorBC = beanContextFactory.registerBean(behaviorType);
        beanContextFactory.link(behaviorBC).to(IBytecodeBehaviorExtendable.class);
        return behaviorBC;
    }

    @Override
    public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
        beanContextFactory.registerBean("bytecodeEnhancer", BytecodeEnhancer.class).autowireable(IBytecodeEnhancer.class, IBytecodeBehaviorExtendable.class);

        var bytecodeClassLoaderBC = beanContextFactory.registerBean(BytecodeClassLoader.class).autowireable(IBytecodeClassLoader.class, IBytecodePrinter.class);
        beanContextFactory.link(bytecodeClassLoaderBC).to(IEventListenerExtendable.class).with(ClearAllCachesEvent.class).optional();
    }
}
