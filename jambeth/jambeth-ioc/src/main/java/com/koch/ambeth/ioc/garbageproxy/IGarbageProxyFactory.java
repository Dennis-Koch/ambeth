package com.koch.ambeth.ioc.garbageproxy;

/*-
 * #%L
 * jambeth-ioc
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

import com.koch.ambeth.util.IDisposable;

/**
 * Creates instances of a so-called "GarbageProxy". That is an object proxying the a real object by
 * implementing its contract, forwarding all normal calls of the contract to the target. So nothing
 * special in this regard. The important behavior is: If the "GarbageProxy" is GCed by the VM it
 * explicitly calls the {@link IDisposable#dispose()} on the real object. This is necessary in cases
 * where the real object is never able to be GCed because of bi-directional hard references in an
 * internal object structure (e.g. if the real object is registered as a listener on an observable
 * the listener may be hard-referenced by this observable).
 */
public interface IGarbageProxyFactory {
	/**
	 * Provides an explicit constructor handle which is allowed to be "cached" on an instance field
	 * for cases of high invocation amounts for proxies of target objects.
	 *
	 * @param interfaceType The compile-safe bound contract to be implemented by the "GarbageProxy"
	 * @param additionalInterfaceTypes Any additional interfaces to be implemented by the
	 *        "GarbageProxy" (allowing to cast to it runtime)
	 * @return The constructor handle to create very fast instances of the necessary "GarbageProxy"
	 *         for any given target object
	 */
	<T> IGarbageProxyConstructor<T> createGarbageProxyConstructor(Class<T> interfaceType,
			Class<?>... additionalInterfaceTypes);

	/**
	 * Creates an instance of a "GarbageProxy" implementing the necessary contracts and forwarding all
	 * calls to the given target. This is a convience overloads for
	 * {@link #createGarbageProxy(Object, IDisposable, Class, Class...)} where the target is also the
	 * <code>Disposable</code>.
	 *
	 * @param target The target to forward to contract calls to and also used as the
	 *        <code>Disposable</code> to call during the finalize() phase of the "GarbageProxy"
	 * @param interfaceType The compile-safe bound contract to be implemented by the "GarbageProxy"
	 * @param additionalInterfaceTypes Any additional interfaces to be implemented by the
	 *        "GarbageProxy" (allowing to cast to it runtime)
	 * @return An instance of "GarbageProxy" implementing all requested interfaces
	 */
	<T> T createGarbageProxy(IDisposable target, Class<T> interfaceType,
			Class<?>... additionalInterfaceTypes);

	/**
	 * Creates an instance of a "GarbageProxy" implementing the necessary contracts and forwarding all
	 * calls to the given target.
	 *
	 * @param target The target to forward to contract calls to
	 * @param disposable The <code>Disposable</code> to call during the finalize() phase of the
	 *        "GarbageProxy"
	 * @param interfaceType The compile-safe bound contract to be implemented by the "GarbageProxy"
	 * @param additionalInterfaceTypes Any additional interfaces to be implemented by the
	 *        "GarbageProxy" (allowing to cast to it runtime)
	 * @return An instance of "GarbageProxy" implementing all requested interfaces
	 */
	<T> T createGarbageProxy(Object target, IDisposable disposable, Class<T> interfaceType,
			Class<?>... additionalInterfaceTypes);
}
