package com.koch.ambeth.cache.bytecode.ioc;

/*-
 * #%L
 * jambeth-cache-bytecode
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
import com.koch.ambeth.bytecode.ioc.BytecodeModule;
import com.koch.ambeth.cache.bytecode.behavior.CacheMapEntryBehavior;
import com.koch.ambeth.cache.bytecode.behavior.DataObjectBehavior;
import com.koch.ambeth.cache.bytecode.behavior.DefaultPropertiesBehavior;
import com.koch.ambeth.cache.bytecode.behavior.EmbeddedTypeBehavior;
import com.koch.ambeth.cache.bytecode.behavior.EnhancedTypeBehavior;
import com.koch.ambeth.cache.bytecode.behavior.EntityEqualsBehavior;
import com.koch.ambeth.cache.bytecode.behavior.InitializeEmbeddedMemberBehavior;
import com.koch.ambeth.cache.bytecode.behavior.LazyRelationsBehavior;
import com.koch.ambeth.cache.bytecode.behavior.NotifyPropertyChangedBehavior;
import com.koch.ambeth.cache.bytecode.behavior.ParentCacheHardRefBehavior;
import com.koch.ambeth.cache.bytecode.behavior.RootCacheValueBehavior;
import com.koch.ambeth.ioc.IFrameworkModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;

@SpiService(IFrameworkModule.class)
@FrameworkModule
public class CacheBytecodeModule implements IFrameworkModule {
    @Override
    public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
        // cascade $1
        BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, EmbeddedTypeBehavior.class);
        BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, EnhancedTypeBehavior.class);
        BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, DefaultPropertiesBehavior.class);
        // cascade $2
        BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, LazyRelationsBehavior.class);

        BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, InitializeEmbeddedMemberBehavior.class);
        // cascade $3
        BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, NotifyPropertyChangedBehavior.class);
        BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, ParentCacheHardRefBehavior.class);
        BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, EntityEqualsBehavior.class);
        // BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory,
        // PublicEmbeddedConstructorBehavior.class);
        // cascade $4
        BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, DataObjectBehavior.class);

        BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, CacheMapEntryBehavior.class);
        BytecodeModule.addDefaultBytecodeBehavior(beanContextFactory, RootCacheValueBehavior.class);
    }
}
