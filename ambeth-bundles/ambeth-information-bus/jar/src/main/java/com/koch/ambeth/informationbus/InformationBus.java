package com.koch.ambeth.informationbus;

/*-
 * #%L
 * jambeth-information-bus
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

import com.koch.ambeth.bytecode.ioc.BytecodeModule;
import com.koch.ambeth.cache.bytecode.ioc.CacheBytecodeModule;
import com.koch.ambeth.cache.datachange.ioc.CacheDataChangeModule;
import com.koch.ambeth.cache.ioc.CacheModule;
import com.koch.ambeth.cache.stream.ioc.CacheStreamModule;
import com.koch.ambeth.core.bundle.Core;
import com.koch.ambeth.core.bundle.IBundleModule;
import com.koch.ambeth.dot.ioc.DotModule;
import com.koch.ambeth.event.datachange.ioc.EventDataChangeModule;
import com.koch.ambeth.event.ioc.EventModule;
import com.koch.ambeth.expr.ioc.ExprModule;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.mapping.ioc.MappingModule;
import com.koch.ambeth.merge.bytecode.ioc.MergeBytecodeModule;
import com.koch.ambeth.merge.ioc.ChangeControllerModule;
import com.koch.ambeth.merge.ioc.MergeModule;
import com.koch.ambeth.merge.ioc.ObjectCopierModule;
import com.koch.ambeth.merge.util.setup.SetupModule;
import com.koch.ambeth.security.bytecode.ioc.SecurityBytecodeModule;
import com.koch.ambeth.security.ioc.PrivilegeModule;
import com.koch.ambeth.security.ioc.SecurityModule;
import com.koch.ambeth.sensor.ioc.SensorModule;
import com.koch.ambeth.service.ioc.ServiceModule;
import com.koch.ambeth.stream.ioc.StreamModule;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

@SuppressWarnings("unchecked")
public class InformationBus implements IBundleModule {
    private static final Class<?>[] bundleModules = {
            BytecodeModule.class,
            CacheBytecodeModule.class,
            CacheDataChangeModule.class,
            CacheModule.class,
            CacheStreamModule.class,
            ChangeControllerModule.class,
            DotModule.class,
            EventDataChangeModule.class,
            EventModule.class,
            ExprModule.class,
            MappingModule.class,
            MergeBytecodeModule.class,
            MergeModule.class,
            ObjectCopierModule.class,
            PrivilegeModule.class,
            SecurityBytecodeModule.class,
            SecurityModule.class,
            SensorModule.class,
            ServiceModule.class,
            SetupModule.class,
            StreamModule.class
    };

    private static final Class<?>[] parentBundles = { Core.class };

    private static final Class<?>[] resultingBundleModules;

    static {
        try {
            var allModules = new ArrayList<Class<? extends IInitializingModule>>();
            allModules.addAll((Class<? extends IInitializingModule>[]) bundleModules);

            for (var parentBundleClass : parentBundles) {
                var parentBundle = (IBundleModule) parentBundleClass.newInstance();
                var parentBundleModules = parentBundle.getBundleModules();
                allModules.addAll(parentBundleModules);
            }
            resultingBundleModules = allModules.toArray(Class.class);
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        }
    }

    @Override
    public Class<? extends IInitializingModule>[] getBundleModules() {
        return (Class<? extends IInitializingModule>[]) resultingBundleModules;
    }
}
