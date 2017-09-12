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
 * Created by {@link IGarbageProxyFactory#createGarbageProxyConstructor(Class, Class...)} to allow
 * very fast instantiation of "GarbageProxy" instances sharing the same contract (=generic type "T")
 * at runtime.
 *
 * @param <T>
 */
public abstract class IGarbageProxyConstructor<T> {
	/**
	 * Creates an instance of a "GarbageProxy" implementing the necessary contracts and forwarding all
	 * calls to the given target. This is a convience overloads for
	 * {@link #createInstance(Object, IDisposable)} where the target is also the
	 * <code>Disposable</code>.
	 *
	 * @param target The target to forward to contract calls to and also used as the
	 *        <code>Disposable</code> to call during the finalize() phase of the "GarbageProxy"
	 * @return An instance of "GarbageProxy" implementing all requested interfaces
	 */
	public abstract T createInstance(IDisposable target);

	/**
	 * Creates an instance of a "GarbageProxy" implementing the necessary contracts and forwarding all
	 * calls to the given target.
	 *
	 * @param target The target to forward to contract calls to
	 * @param disposable The <code>Disposable</code> to call during the finalize() phase of the
	 *        "GarbageProxy"
	 * @return An instance of "GarbageProxy" implementing all requested interfaces
	 */
	public abstract T createInstance(Object target, IDisposable disposable);
}
