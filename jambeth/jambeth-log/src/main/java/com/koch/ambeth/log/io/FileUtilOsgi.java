package com.koch.ambeth.log.io;

/*-
 * #%L
 * jambeth-log
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

import com.koch.ambeth.util.collections.IdentityHashSet;
import com.koch.ambeth.util.state.IStateRollback;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

import java.io.InputStream;

public final class FileUtilOsgi {

    private static final ThreadLocal<Bundle> currentBundleTL = new ThreadLocal<>();

    public static IStateRollback pushCurrentBundle(Bundle currentBundle) {
        var oldCurrentBundle = currentBundleTL.get();
        currentBundleTL.set(currentBundle);
        return () -> currentBundleTL.set(oldCurrentBundle);
    }

    public InputStream openFromOSGiTree(Class<?> type, String resourceName) {
        try {
            var classLoader = type.getClassLoader();

            var bundle = currentBundleTL.get();
            if (bundle == null) {
                bundle = (Bundle) classLoader.getClass()
                                             .getMethod("getBundle")
                                             .invoke(classLoader);
            }
            var alreadyTriedSet = new IdentityHashSet<Bundle>();
            if (!alreadyTriedSet.add(bundle)) {
                return null;
            }
            var url = bundle.getResource(resourceName);
            if (url != null) {
                return url.openStream();
            }
            var bundleWiring = bundle.adapt(BundleWiring.class);
            for (var requiredWire : bundleWiring.getRequiredWires(null)) {
                var capability = requiredWire.getCapability();
                var revision = capability.getRevision();
                var wiredBundle = revision.getBundle();
                if (!alreadyTriedSet.add(wiredBundle)) {
                    continue;
                }
                url = wiredBundle.getResource(resourceName);
                if (url != null) {
                    return url.openStream();
                }
            }
            return null;
        } catch (Throwable e) {
            return null;
        }
    }
}
