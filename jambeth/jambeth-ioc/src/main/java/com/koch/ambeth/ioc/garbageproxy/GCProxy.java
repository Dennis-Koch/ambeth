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

import java.lang.reflect.Method;

import com.koch.ambeth.ioc.exception.BeanAlreadyDisposedException;
import com.koch.ambeth.util.IDisposable;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public abstract class GCProxy implements IDisposable {
	public static final Method disposeMethod;

	static {
		try {
			disposeMethod = IDisposable.class.getDeclaredMethod("dispose");
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected Object target;

	protected IDisposable disposable;

	public GCProxy(IDisposable target) {
		this(target, target);
	}

	public GCProxy(Object target, IDisposable disposable) {
		this.target = target;
		this.disposable = disposable;
	}

	@Override
	protected final void finalize() throws Throwable {
		dispose();
	}

	@Override
	public final void dispose() {
		IDisposable disposable = this.disposable;
		if (disposable != null) {
			disposable.dispose();
			this.disposable = null;
		}
		target = null;
	}

	protected final Object resolveTarget() {
		Object target = this.target;
		if (target != null) {
			return target;
		}
		throw new BeanAlreadyDisposedException(
				"This handle has already been disposed. This seems like a memory leak in your application if you refer to illegal handles");
	}

	@Override
	public String toString() {
		Object target = this.target;
		if (target != null) {
			return target.toString();
		}
		return super.toString();
	}
}
