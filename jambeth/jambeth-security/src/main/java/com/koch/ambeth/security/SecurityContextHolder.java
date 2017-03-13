package com.koch.ambeth.security;

import com.koch.ambeth.ioc.DefaultExtendableContainer;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.threadlocal.Forkable;
import com.koch.ambeth.ioc.threadlocal.IForkProcessor;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.merge.security.ILightweightSecurityContext;
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

	protected final DefaultExtendableContainer<IAuthorizationChangeListener> authorizationChangeListeners =
			new DefaultExtendableContainer<IAuthorizationChangeListener>(
					IAuthorizationChangeListener.class, "authorizationChangeListener");

	@Forkable(processor = SecurityContextForkProcessor.class)
	protected final ThreadLocal<ISecurityContext> contextTL =
			new SensitiveThreadLocal<ISecurityContext>();

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
	public <R> R setScopedAuthentication(IAuthentication authentication,
			IResultingBackgroundWorkerDelegate<R> runnableScope) throws Throwable {
		ISecurityContext securityContext = getContext();
		boolean created = false;
		if (securityContext == null) {
			securityContext = getCreateContext();
			created = true;
		}
		IAuthorization oldAuthorization = securityContext.getAuthorization();
		IAuthentication oldAuthentication = securityContext.getAuthentication();
		try {
			if (oldAuthentication == authentication) {
				return runnableScope.invoke();
			}
			try {
				securityContext.setAuthentication(authentication);
				securityContext.setAuthorization(null);
				return runnableScope.invoke();
			}
			finally {
				securityContext.setAuthentication(oldAuthentication);
				securityContext.setAuthorization(oldAuthorization);
			}
		}
		finally {
			if (created) {
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
}
