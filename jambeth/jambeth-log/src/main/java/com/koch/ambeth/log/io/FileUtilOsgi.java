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

import java.io.InputStream;
import java.net.URL;

import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;

import com.koch.ambeth.util.collections.IdentityHashSet;
import com.koch.ambeth.util.state.AbstractStateRollback;
import com.koch.ambeth.util.state.IStateRollback;

public final class FileUtilOsgi {

	private static final ThreadLocal<Bundle> currentBundleTL = new ThreadLocal<>();

	public static final IStateRollback pushCurrentBundle(Bundle currentBundle,
			IStateRollback... rollbacks) {
		final Bundle oldCurrentBundle = currentBundleTL.get();
		currentBundleTL.set(currentBundle);
		return new AbstractStateRollback(rollbacks) {
			@Override
			protected void rollbackIntern() throws Exception {
				currentBundleTL.set(oldCurrentBundle);
			}
		};
	}

	public InputStream openFromOSGiTree(Class<?> type, String resourceName) {
		try {
			ClassLoader classLoader = type.getClassLoader();

			Bundle bundle = currentBundleTL.get();
			if (bundle == null) {
				bundle = (Bundle) classLoader.getClass().getMethod("getBundle").invoke(classLoader);
			}
			IdentityHashSet<Bundle> alreadyTriedSet = new IdentityHashSet<>();
			if (!alreadyTriedSet.add(bundle)) {
				return null;
			}
			URL url = bundle.getResource(resourceName);
			if (url != null) {
				return url.openStream();
			}
			BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
			for (BundleWire requiredWire : bundleWiring.getRequiredWires(null)) {
				BundleCapability capability = requiredWire.getCapability();
				BundleRevision revision = capability.getRevision();
				Bundle wiredBundle = revision.getBundle();
				if (!alreadyTriedSet.add(wiredBundle)) {
					continue;
				}
				url = wiredBundle.getResource(resourceName);
				if (url != null) {
					return url.openStream();
				}
			}
			return null;
		}
		catch (Throwable e) {
			return null;
		}
	}
}
