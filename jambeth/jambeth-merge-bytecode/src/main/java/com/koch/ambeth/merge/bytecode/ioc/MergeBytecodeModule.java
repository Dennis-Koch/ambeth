package com.koch.ambeth.merge.bytecode.ioc;

/*-
 * #%L
 * jambeth-merge-bytecode
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

import io.toolisticon.spiap.api.SpiService;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehavior;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehaviorExtendable;
import com.koch.ambeth.bytecode.ioc.BytecodeModule;
import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IFrameworkModule;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.bytecode.behavior.DelegateBehavior;
import com.koch.ambeth.merge.bytecode.behavior.EntityMetaDataMemberBehavior;
import com.koch.ambeth.merge.bytecode.behavior.ObjRefBehavior;
import com.koch.ambeth.merge.bytecode.behavior.ObjRefStoreBehavior;
import com.koch.ambeth.merge.bytecode.behavior.ObjRefTypeBehavior;
import com.koch.ambeth.merge.bytecode.compositeid.CompositeIdBehavior;
import com.koch.ambeth.merge.bytecode.compositeid.CompositeIdFactory;
import com.koch.ambeth.merge.compositeid.ICompositeIdFactory;

@SpiService(IFrameworkModule.class)
@FrameworkModule
public class MergeBytecodeModule implements IFrameworkModule {
    @Override
    public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
        beanContextFactory.registerBean("compositeIdFactory", CompositeIdFactory.class).autowireable(ICompositeIdFactory.class);

        BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, CompositeIdBehavior.class);
        BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, EntityMetaDataMemberBehavior.class);

        BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, ObjRefBehavior.class);
        BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, ObjRefStoreBehavior.class);
        BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, ObjRefTypeBehavior.class);

        // small trick: we need the DelegateBehavior as a very-early-registered extension to the
        // BytecodeEnhancer
        // in the ordinary "link" phase it is already too late
        final IBeanConfiguration delegateBehavior = beanContextFactory.registerBean(DelegateBehavior.class);
        beanContextFactory.registerWithLifecycle(new DisposeModule() {
            @Autowired
            protected IBytecodeBehaviorExtendable bytecodeBehaviorExtendable;
            private IBytecodeBehavior instance;

            @Override
            public void afterPropertiesSet() throws Throwable {
                instance = (IBytecodeBehavior) delegateBehavior.getInstance();
                bytecodeBehaviorExtendable.registerBytecodeBehavior(instance);
            }

            @Override
            public void destroy() throws Throwable {
                bytecodeBehaviorExtendable.unregisterBytecodeBehavior(instance);
            }
        });
    }

    private abstract class DisposeModule implements IInitializingBean, IDisposableBean {

    }
}
