package com.koch.ambeth.merge.security;

/*-
 * #%L
 * jambeth-merge
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

import com.koch.ambeth.ioc.DefaultExtendableContainer;
import com.koch.ambeth.ioc.threadlocal.Forkable;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.service.model.ISecurityScope;
import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerParamDelegate;
import com.koch.ambeth.util.threading.SensitiveThreadLocal;

public class SecurityScopeProvider implements IThreadLocalCleanupBean, ISecurityScopeProvider,
		ISecurityScopeChangeListenerExtendable {
	public static final ISecurityScope[] defaultSecurityScopes = new ISecurityScope[0];

	@Forkable
	protected final ThreadLocal<SecurityScopeHandle> securityScopeTL =
			new SensitiveThreadLocal<>();

	protected final DefaultExtendableContainer<ISecurityScopeChangeListener> securityScopeChangeListeners =
			new DefaultExtendableContainer<>(
					ISecurityScopeChangeListener.class, "securityScopeChangeListener");

	@Override
	public void cleanupThreadLocal() {
		securityScopeTL.remove();
	}

	@Override
	public ISecurityScope[] getSecurityScopes() {
		SecurityScopeHandle securityScopeHandle = securityScopeTL.get();
		if (securityScopeHandle == null) {
			return defaultSecurityScopes;
		}
		if (securityScopeHandle.securityScopes == null) {
			return defaultSecurityScopes;
		}
		return securityScopeHandle.securityScopes;
	}

	@Override
	public void setSecurityScopes(ISecurityScope[] securityScopes) {
		SecurityScopeHandle securityScopeHandle = securityScopeTL.get();
		if (securityScopeHandle == null) {
			securityScopeHandle = new SecurityScopeHandle();
			securityScopeTL.set(securityScopeHandle);
		}
		securityScopeHandle.securityScopes = securityScopes;
		notifySecurityScopeChangeListeners(securityScopeHandle);
	}

	@Override
	public <R, V> R executeWithSecurityScopes(IResultingBackgroundWorkerParamDelegate<R, V> runnable,
			V state, ISecurityScope... securityScopes) throws Throwable {
		ISecurityScope[] oldSecurityScopes = getSecurityScopes();
		try {
			setSecurityScopes(securityScopes);
			return runnable.invoke(state);
		}
		finally {
			setSecurityScopes(oldSecurityScopes);
		}
	}

	@Override
	public <R> R executeWithSecurityScopes(IResultingBackgroundWorkerDelegate<R> runnable,
			ISecurityScope... securityScopes) throws Throwable {
		ISecurityScope[] oldSecurityScopes = getSecurityScopes();
		try {
			setSecurityScopes(securityScopes);
			return runnable.invoke();
		}
		finally {
			setSecurityScopes(oldSecurityScopes);
		}
	}

	@Override
	public <V> void executeWithSecurityScopes(IBackgroundWorkerParamDelegate<V> runnable, V state,
			ISecurityScope... securityScopes) throws Throwable {
		ISecurityScope[] oldSecurityScopes = getSecurityScopes();
		try {
			setSecurityScopes(securityScopes);
			runnable.invoke(state);
		}
		finally {
			setSecurityScopes(oldSecurityScopes);
		}
	}

	@Override
	public void executeWithSecurityScopes(IBackgroundWorkerDelegate runnable,
			ISecurityScope... securityScopes) throws Throwable {
		ISecurityScope[] oldSecurityScopes = getSecurityScopes();
		try {
			setSecurityScopes(securityScopes);
			runnable.invoke();
		}
		finally {
			setSecurityScopes(oldSecurityScopes);
		}
	}

	protected void notifySecurityScopeChangeListeners(SecurityScopeHandle securityScopeHandle) {
		for (ISecurityScopeChangeListener securityScopeChangeListener : securityScopeChangeListeners
				.getExtensions()) {
			securityScopeChangeListener.securityScopeChanged(securityScopeHandle.securityScopes);
		}
	}

	@Override
	public void registerSecurityScopeChangeListener(
			ISecurityScopeChangeListener securityScopeChangeListener) {
		securityScopeChangeListeners.register(securityScopeChangeListener);
	}

	@Override
	public void unregisterSecurityScopeChangeListener(
			ISecurityScopeChangeListener securityScopeChangeListener) {
		securityScopeChangeListeners.unregister(securityScopeChangeListener);
	}
}
