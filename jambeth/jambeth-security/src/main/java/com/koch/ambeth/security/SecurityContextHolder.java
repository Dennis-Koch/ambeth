package com.koch.ambeth.security;

import java.lang.reflect.Method;

import com.koch.ambeth.event.IEventDispatcher;

/*-
 * #%L
 * jambeth-security
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
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.threadlocal.Forkable;
import com.koch.ambeth.ioc.threadlocal.IForkProcessor;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.merge.config.MergeConfigurationConstants;
import com.koch.ambeth.merge.security.ILightweightSecurityContext;
import com.koch.ambeth.merge.security.ISecurityActivation;
import com.koch.ambeth.security.events.AuthorizationMissingEvent;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.state.AbstractStateRollback;
import com.koch.ambeth.util.state.IStateRollback;
import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.SensitiveThreadLocal;

public class SecurityContextHolder implements IAuthorizationChangeListenerExtendable,
		ISecurityContextHolder, IThreadLocalCleanupBean, ILightweightSecurityContext {
	public static class SecurityContextForkProcessor implements IForkProcessor {
		@Override
		public Object resolveOriginalValue(Object bean, String fieldName, ThreadLocal<?> fieldValueTL) {
			return fieldValueTL.get();
		}

		@Override
		public Object createForkedValue(Object value) {
			if (value == null) {
				return null;
			}
			SecurityContextImpl original = (SecurityContextImpl) value;
			SecurityContextImpl forkedValue = new SecurityContextImpl(original.securityContextHolder);
			forkedValue.setAuthentication(original.getAuthentication());
			forkedValue.setAuthorization(original.getAuthorization());
			return forkedValue;
		}

		@Override
		public void returnForkedValue(Object value, Object forkedValue) {
			// Intended blank
		}
	}

	@Autowired
	protected IAuthenticatedUserHolder authenticatedUserHolder;

	@Autowired(optional = true)
	protected ISecurityContextProvider securityContextProvider;

	@Autowired
	protected IEventDispatcher eventDispatcher;

	@Autowired
	protected ISecurityActivation securityActivation;

	@Autowired(optional = true)
	protected ISecurityManager securityManager;

	@Property(name = MergeConfigurationConstants.SecurityActive, defaultValue = "false")
	protected boolean securityActive;

	protected final DefaultExtendableContainer<IAuthorizationChangeListener> authorizationChangeListeners = new DefaultExtendableContainer<>(
			IAuthorizationChangeListener.class, "authorizationChangeListener");

	@Forkable(processor = SecurityContextForkProcessor.class)
	protected final ThreadLocal<ISecurityContext> contextTL = new SensitiveThreadLocal<>();

	protected void notifyAuthorizationChangeListeners(IAuthorization authorization) {
		authenticatedUserHolder
				.setAuthenticatedSID(authorization != null ? authorization.getSID() : null);
		for (IAuthorizationChangeListener authorizationChangeListener : authorizationChangeListeners
				.getExtensions()) {
			authorizationChangeListener.authorizationChanged(authorization);
		}
	}

	@Override
	public void cleanupThreadLocal() {
		clearContext();
	}

	@Override
	public void registerAuthorizationChangeListener(
			IAuthorizationChangeListener authorizationChangeListener) {
		authorizationChangeListeners.register(authorizationChangeListener);
	}

	@Override
	public void unregisterAuthorizationChangeListener(
			IAuthorizationChangeListener authorizationChangeListener) {
		authorizationChangeListeners.unregister(authorizationChangeListener);
	}

	@Override
	public ISecurityContext getContext() {
		ISecurityContext context = getContextIntern();
		if (context == null && securityContextProvider != null) {
			context = securityContextProvider.getSecurityContext();
		}
		return context;
	}

	protected ISecurityContext getContextIntern() {
		return contextTL.get();
	}

	@Override
	public ISecurityContext getCreateContext() {
		ISecurityContext securityContext = getContext();
		if (securityContext == null) {
			securityContext = new SecurityContextImpl(this);
			contextTL.set(securityContext);
		}
		return securityContext;
	}

	protected ISecurityContext getCreateContextIntern() {
		ISecurityContext securityContext = getContextIntern();
		if (securityContext == null) {
			securityContext = new SecurityContextImpl(this);
			contextTL.set(securityContext);
		}
		return securityContext;
	}

	@Override
	public void clearContext() {
		ISecurityContext securityContext = contextTL.get();
		if (securityContext != null) {
			securityContext.setAuthentication(null);
			securityContext.setAuthorization(null);
			contextTL.remove();
		}
	}

	@Override
	public IStateRollback pushAuthentication(IAuthentication authentication,
			IStateRollback... rollbacks) {
		ISecurityContext securityContext = getContextIntern();
		boolean created = false;
		if (securityContext == null) {
			securityContext = getCreateContextIntern();
			created = true;
		}
		boolean success = false;
		final IAuthorization oldAuthorization = securityContext.getAuthorization();
		final IAuthentication oldAuthentication = securityContext.getAuthentication();
		try {
			final boolean fCreated = created;
			if (oldAuthentication == authentication) {
				IStateRollback rollback = new AbstractStateRollback(rollbacks) {
					@Override
					protected void rollbackIntern() throws Exception {
						if (fCreated) {
							clearContext();
						}
					}
				};
				success = true;
				return rollback;
			}
			try {
				securityContext.setAuthentication(authentication);
				securityContext.setAuthorization(null);
				final ISecurityContext fSecurityContext = securityContext;
				IStateRollback rollback = new AbstractStateRollback(rollbacks) {
					@Override
					protected void rollbackIntern() throws Exception {
						fSecurityContext.setAuthentication(oldAuthentication);
						fSecurityContext.setAuthorization(oldAuthorization);
						if (fCreated) {
							clearContext();
						}
					}
				};
				success = true;
				return rollback;
			}
			finally {
				if (!success) {
					securityContext.setAuthentication(oldAuthentication);
					securityContext.setAuthorization(oldAuthorization);
				}
			}
		}
		finally {
			if (!success && created) {
				clearContext();
			}
		}
	}

	@Override
	public boolean isAuthenticated() {
		ISecurityContext securityContext = getContext();
		if (securityContext == null) {
			return false;
		}
		return securityContext.getAuthorization() != null;
	}

	@Override
	public void withAuthenticated(IBackgroundWorkerDelegate delegate) {
		ensureAuthenticated();
		try {
			delegate.invoke();
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public <R> R withAuthenticated(IResultingBackgroundWorkerDelegate<R> delegate) {
		ensureAuthenticated();
		try {
			return delegate.invoke();
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void withAuthorized(Method method, IBackgroundWorkerDelegate delegate) {
		IAuthorization authorization = ensureAuthenticated();
		if (securityManager != null) {
			securityManager.checkMethodAccess(method, new Object[0], SecurityContextType.AUTHORIZED,
					authorization);
		}
		try {
			delegate.invoke();
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public <R> R withAuthorized(Method method, IResultingBackgroundWorkerDelegate<R> delegate) {
		IAuthorization authorization = ensureAuthenticated();
		if (securityManager != null) {
			securityManager.checkMethodAccess(method, new Object[0], SecurityContextType.AUTHORIZED,
					authorization);
		}
		try {
			R result = delegate.invoke();
			if (securityManager != null) {
				result = securityManager.filterValue(result);
			}
			return result;
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected IAuthorization ensureAuthenticated() {
		if (!securityActive) {
			return null;
		}
		ISecurityContext securityContext = getContext();
		IAuthorization authorization = securityContext != null ? securityContext.getAuthorization()
				: null;
		if (authorization != null) {
			if (!authorization.isValid()) {
				throw new SecurityException("Authorization invalid");
			}
			return null;
		}
		eventDispatcher.dispatchEvent(AuthorizationMissingEvent.getInstance());
		securityContext = getContext();
		authorization = securityContext != null ? securityContext.getAuthorization() : null;
		if (authorization == null || !authorization.isValid()) {
			throw new SecurityException("Authorization invalid");
		}
		return authorization;
	}
}
