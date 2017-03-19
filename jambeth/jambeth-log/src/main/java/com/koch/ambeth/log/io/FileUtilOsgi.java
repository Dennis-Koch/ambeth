package com.koch.ambeth.log.io;

import java.io.InputStream;
import java.net.URL;

import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;

import com.koch.ambeth.util.collections.IdentityHashSet;

public final class FileUtilOsgi {

	public InputStream openFromOSGiTree(Class<?> type, String resourceName) {
		try {
			ClassLoader classLoader = type.getClassLoader();
			Bundle bundle = (Bundle) classLoader.getClass().getMethod("getBundle").invoke(classLoader);
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
