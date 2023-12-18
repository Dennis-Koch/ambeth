package com.koch.ambeth.server.persistence;

/*-
 * #%L
 * jambeth-server-with-persistence
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

import com.koch.ambeth.core.bundle.IBundleModule;
import com.koch.ambeth.informationbus.persistence.InformationBusWithPersistence;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.server.bundle.Server;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

@SuppressWarnings("unchecked")
public class ServerWithPersistence implements IBundleModule {
    private static final Class<?>[] bundleModules = {};

    private static final Class<?>[] parentBundles = { InformationBusWithPersistence.class, Server.class };

    private static final Class<?>[] resultingBundleModules;

    static {
        try {
            ArrayList<Class<? extends IInitializingModule>> allModules = new ArrayList<>();
            allModules.addAll((Class<? extends IInitializingModule>[]) bundleModules);

            for (Class<?> parentBundleClass : parentBundles) {
                IBundleModule parentBundle = (IBundleModule) parentBundleClass.newInstance();
                Class<? extends IInitializingModule>[] parentBundleModules = parentBundle.getBundleModules();
                allModules.addAll(parentBundleModules);
            }

            resultingBundleModules = allModules.toArray(Class[]::new);
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        }
    }

    @Override
    public Class<? extends IInitializingModule>[] getBundleModules() {
        return (Class<? extends IInitializingModule>[]) resultingBundleModules;
    }
}
