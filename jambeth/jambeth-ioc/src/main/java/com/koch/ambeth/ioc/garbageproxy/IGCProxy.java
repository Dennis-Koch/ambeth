package com.koch.ambeth.ioc.garbageproxy;

import com.koch.ambeth.util.IDisposable;

/**
 * Marker interface of "GarbageProxy" instances. This interface is not meant for public use and is
 * also not meant for production use. The only purpose is to allow in testing scenarios to check
 * whether a "GarbageProxy" composition is like expected.
 */
public interface IGCProxy {
	/**
	 * Returns the proxied the target object which will be disposed via a call to
	 * {@link IDisposable#dispose()} if this object is GCed. Never store the target object anywhere on
	 * a static or instance field (neither directly nor indirectly) - otherwise the whole purpose of
	 * the "GarbageProxy" pattern is eroded. You have been warned: If you work with this interface you
	 * should know what you do.
	 *
	 * @return The proxied target object
	 */
	Object getGCProxyTarget();
}
