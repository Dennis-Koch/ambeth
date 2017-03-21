package com.koch.ambeth.ioc.threadlocal;

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

import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerParamDelegate;

public class ForkState extends ReentrantLock implements IForkState {
	private static final long serialVersionUID = 3277389225453647471L;

	protected final ForkStateEntry[] forkStateEntries;

	protected final IForkedValueResolver[] forkedValueResolvers;

	protected final ArrayList<Object>[] forkedValues;

	@SuppressWarnings("unchecked")
	public ForkState(ForkStateEntry[] forkStateEntries, IForkedValueResolver[] forkedValueResolvers) {
		this.forkStateEntries = forkStateEntries;
		this.forkedValueResolvers = forkedValueResolvers;
		forkedValues = new ArrayList[forkStateEntries.length];
	}

	@SuppressWarnings("unchecked")
	protected Object[] setThreadLocals() {
		ForkStateEntry[] forkStateEntries = this.forkStateEntries;
		IForkedValueResolver[] forkedValueResolvers = this.forkedValueResolvers;
		Object[] oldValues = new Object[forkedValueResolvers.length];
		for (int a = 0, size = forkStateEntries.length; a < size; a++) {
			ThreadLocal<Object> tlHandle = (ThreadLocal<Object>) forkStateEntries[a].valueTL;
			oldValues[a] = tlHandle.get();
			Object forkedValue = forkedValueResolvers[a].createForkedValue();
			tlHandle.set(forkedValue);
		}
		return oldValues;
	}

	@SuppressWarnings("unchecked")
	protected void restoreThreadLocals(Object[] oldValues) {
		ForkStateEntry[] forkStateEntries = this.forkStateEntries;
		IForkedValueResolver[] forkedValueResolvers = this.forkedValueResolvers;
		ArrayList<Object>[] forkedValues = this.forkedValues;
		lock();
		try {
			for (int a = 0, size = forkStateEntries.length; a < size; a++) {
				ForkStateEntry forkStateEntry = forkStateEntries[a];
				ThreadLocal<Object> tlHandle = (ThreadLocal<Object>) forkStateEntry.valueTL;
				Object forkedValue = tlHandle.get();
				tlHandle.set(oldValues[a]);
				IForkedValueResolver forkedValueResolver = forkedValueResolvers[a];
				if (!(forkedValueResolver instanceof ForkProcessorValueResolver)) {
					continue;
				}
				ArrayList<Object> forkedValuesItem = forkedValues[a];
				if (forkedValuesItem == null) {
					forkedValuesItem = new ArrayList<>();
					forkedValues[a] = forkedValuesItem;
				}
				forkedValuesItem.add(forkedValue);
			}
		}
		finally {
			unlock();
		}
	}

	@Override
	public void use(Runnable runnable) {
		Object[] oldValues = setThreadLocals();
		try {
			runnable.run();
		}
		finally {
			restoreThreadLocals(oldValues);
		}
	}

	@Override
	public void use(IBackgroundWorkerDelegate runnable) {
		Object[] oldValues = setThreadLocals();
		try {
			runnable.invoke();
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		finally {
			restoreThreadLocals(oldValues);
		}
	}

	@Override
	public <V> void use(IBackgroundWorkerParamDelegate<V> runnable, V arg) {
		Object[] oldValues = setThreadLocals();
		try {
			runnable.invoke(arg);
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		finally {
			restoreThreadLocals(oldValues);
		}
	}

	@Override
	public <R> R use(IResultingBackgroundWorkerDelegate<R> runnable) {
		Object[] oldValues = setThreadLocals();
		try {
			return runnable.invoke();
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		finally {
			restoreThreadLocals(oldValues);
		}
	}

	@Override
	public <R, V> R use(IResultingBackgroundWorkerParamDelegate<R, V> runnable, V arg) {
		Object[] oldValues = setThreadLocals();
		try {
			return runnable.invoke(arg);
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		finally {
			restoreThreadLocals(oldValues);
		}
	}

	@Override
	public void reintegrateForkedValues() {
		ForkStateEntry[] forkStateEntries = this.forkStateEntries;
		IForkedValueResolver[] forkedValueResolvers = this.forkedValueResolvers;
		ArrayList<Object>[] forkedValues = this.forkedValues;
		for (int a = 0, size = forkStateEntries.length; a < size; a++) {
			ForkStateEntry forkStateEntry = forkStateEntries[a];
			ArrayList<Object> forkedValuesItem = forkedValues[a];

			if (forkedValuesItem == null) {
				// nothing to do
				continue;
			}
			Object originalValue = forkedValueResolvers[a].getOriginalValue();
			for (int b = 0, sizeB = forkedValuesItem.size(); b < sizeB; b++) {
				Object forkedValue = forkedValuesItem.get(b);
				forkStateEntry.forkProcessor.returnForkedValue(originalValue, forkedValue);
			}
		}
	}
}
