package com.koch.ambeth.xml.util;

/*-
 * #%L
 * jambeth-xml
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

import com.koch.ambeth.ioc.IModuleProvider;
import com.koch.ambeth.ioc.annotation.ApplicationModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.IClasspathScanner;
import com.koch.ambeth.util.collections.ArrayList;

import java.util.Collections;
import java.util.Comparator;

public class ModuleScanner implements IModuleProvider {
    @Autowired
    protected IClasspathScanner classpathScanner;
    @LogInstance
    private ILogger log;

    @Override
    public Class<?>[] getFrameworkModules() {
        return getModules(true);
    }

    @Override
    public Class<?>[] getApplicationModules() {
        return getModules(false);
    }

    protected Class<?>[] getModules(boolean scanForFrameworkModule) {
        if (log.isInfoEnabled()) {
            log.info("Looking for " + (scanForFrameworkModule ? "Ambeth" : "Application") + " bootstrap modules in classpath...");
        }
        var applicationOrFrameworkModules = classpathScanner.scanClassesAnnotatedWith(scanForFrameworkModule ? FrameworkModule.class : ApplicationModule.class);

        var applicationModules = new ArrayList<Class<?>>(applicationOrFrameworkModules.size());

        for (var applicationOrFrameworkModule : applicationOrFrameworkModules) {
            if (scanForFrameworkModule && applicationOrFrameworkModule.isAnnotationPresent(FrameworkModule.class)) {
                applicationModules.add(applicationOrFrameworkModule);
            } else if (applicationOrFrameworkModule.isAnnotationPresent(ApplicationModule.class) && !applicationOrFrameworkModule.isAnnotationPresent(FrameworkModule.class)) {
                applicationModules.add(applicationOrFrameworkModule);
            }
        }
        if (log.isInfoEnabled()) {
            log.info("Found " + applicationModules.size() + (scanForFrameworkModule ? " Ambeth" : " Application") + " modules in classpath to include in bootstrap...");
            Collections.sort(applicationModules, new Comparator<Class<?>>() {
                @Override
                public int compare(Class<?> o1, Class<?> o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            for (int a = 0, size = applicationModules.size(); a < size; a++) {
                var applicationModule = applicationModules.get(a);
                log.info("Including " + applicationModule.getName());
            }
        }
        return applicationModules.toArray(new Class<?>[applicationModules.size()]);
    }
}
