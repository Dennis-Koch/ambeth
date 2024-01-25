package com.koch.ambeth.core.bundle;

/*-
 * #%L
 * jambeth-core
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

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.util.function.CheckedConsumer;

/**
 * Interface for bundle modules that defines the module list for a specific bundle.
 */
public interface IBundleModule {
    Class<? extends IInitializingModule>[] EMPTY_BUNDLE_MODULES = new Class[0];

    CheckedConsumer<IBeanContextFactory>[] EMPTY_BUNDLE_MODULE_INSTANCES = new CheckedConsumer[0];

    default Class<? extends IInitializingModule>[] getBundleModules() {
        return EMPTY_BUNDLE_MODULES;
    }

    default CheckedConsumer<IBeanContextFactory>[] getBundleModuleInstances() {
        return EMPTY_BUNDLE_MODULE_INSTANCES;
    }
}
