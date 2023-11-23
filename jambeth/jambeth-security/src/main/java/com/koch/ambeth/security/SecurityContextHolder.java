package com.koch.ambeth.security;

import com.koch.ambeth.ioc.DefaultExtendableContainer;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.threadlocal.Forkable;
import com.koch.ambeth.ioc.threadlocal.IForkProcessor;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.merge.config.MergeConfigurationConstants;
import com.koch.ambeth.merge.security.ILightweightSecurityContext;
import com.koch.ambeth.merge.security.ISecurityActivation;
import com.koch.ambeth.util.function.CheckedRunnable;
import com.koch.ambeth.util.function.CheckedSupplier;
import com.koch.ambeth.util.state.IStateRollback;
import com.koch.ambeth.util.state.StateRollback;
import com.koch.ambeth.util.state.StateRollbackChain;
import com.koch.ambeth.util.threading.SensitiveThreadLocal;
import lombok.SneakyThrows;

import java.lang.reflect.Method;

public class SecurityContextHolder implements IAuthorizationChangeListenerExtendable, ISecurityContextHolder, IThreadLocalCleanupBean, ILightweightSecurityContext, ISecurityContextFactory {
    protected final DefaultExtendableContainer<IAuthorizationChangeListener> authorizationChangeListeners =
            new DefaultExtendableContainer<>(IAuthorizationChangeListener.class, "authorizationChangeListener");
    @Forkable(processor = SecurityContextForkProcessor.class)
    protected final ThreadLocal<ISecurityContext> contextTL = new SensitiveThreadLocal<>();
    @Autowired
    protected IAuthenticatedUserHolder authenticatedUserHolder;
    @Autowired(optional = true)
    protected IAuthorizationProcess authorizationProcess;
    @Autowired(optional = true)
    protected ISecurityContextProvider securityContextProvider;
    @Autowired
    protected ISecurityActivation securityActivation;
    @Autowired(optional = true)
    protected ISecurityManager securityManager;
    @Property(name = MergeConfigurationConstants.SecurityActive, defaultValue = "false")
    protected boolean securityActive;

    protected void notifyAuthorizationChangeListeners(IAuthorization authorization) {
        authenticatedUserHolder.setAuthenticatedSID(authorization != null ? authorization.getSID() : null);
        for (IAuthorizationChangeListener authorizationChangeListener : authorizationChangeListeners.getExtensionsShared()) {
            authorizationChangeListener.authorizationChanged(authorization);
        }
    }

    @Override
    public void cleanupThreadLocal() {
        clearContext();
    }

    @Override
    public void registerAuthorizationChangeListener(IAuthorizationChangeListener authorizationChangeListener) {
        authorizationChangeListeners.register(authorizationChangeListener);
    }

    @Override
    public void unregisterAuthorizationChangeListener(IAuthorizationChangeListener authorizationChangeListener) {
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

    /*
     * (non-Javadoc)
     * @see com.koch.ambeth.security.ISecurityContextFactory#createSecurityContext()
     */
    @Override
    public ISecurityContext createSecurityContext() {
        return new SecurityContextImpl(this);
    }

    @Override
    public ISecurityContext getCreateContext() {
        ISecurityContext securityContext = getContext();
        if (securityContext == null) {
            securityContext = createSecurityContext();
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
        var securityContext = contextTL.get();
        if (securityContext != null) {
            securityContext.setAuthentication(null);
            securityContext.setAuthorization(null);
            contextTL.remove();
        }
    }

    @Override
    public IStateRollback pushAuthentication(IAuthentication authentication) {
        return StateRollback.chain(chain -> {
            var securityContext = resolveSecurityContext(chain);
            var oldAuthentication = securityContext.getAuthentication();
            if (oldAuthentication == authentication) {
                return;
            }
            securityContext.setAuthentication(authentication);
            chain.append(() -> securityContext.setAuthentication(oldAuthentication));

            var oldAuthorization = securityContext.getAuthorization();
            securityContext.setAuthorization(null);
            chain.append(() -> securityContext.setAuthorization(oldAuthorization));
        });
    }

    private ISecurityContext resolveSecurityContext(StateRollbackChain chain) {
        var securityContext = getContextIntern();
        if (securityContext == null) {
            securityContext = getCreateContextIntern();
            chain.append(() -> clearContext());
        }
        return securityContext;
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
    public void withAuthenticated(CheckedRunnable delegate) {
        ensureAuthenticated();
        CheckedRunnable.invoke(delegate);
    }

    @Override
    public <R> R withAuthenticated(CheckedSupplier<R> delegate) {
        ensureAuthenticated();
        return CheckedSupplier.invoke(delegate);
    }

    @SneakyThrows
    @Override
    public void withAuthorized(Method method, CheckedRunnable delegate) {
        var authorization = ensureAuthenticated();
        if (securityManager != null) {
            securityManager.checkMethodAccess(method, new Object[0], SecurityContextType.AUTHORIZED, authorization);
        }
        CheckedRunnable.invoke(delegate);
    }

    @SneakyThrows
    @Override
    public <R> R withAuthorized(Method method, CheckedSupplier<R> delegate) {
        var authorization = ensureAuthenticated();
        if (securityManager != null) {
            securityManager.checkMethodAccess(method, new Object[0], SecurityContextType.AUTHORIZED, authorization);
        }
        var result = CheckedSupplier.invoke(delegate);
        if (securityManager != null) {
            result = securityManager.filterValue(result);
        }
        return result;
    }

    protected IAuthorization ensureAuthenticated() {
        if (!securityActive) {
            return null;
        }
        var securityContext = getContext();
        var authorization = securityContext != null ? securityContext.getAuthorization() : null;
        if (authorization != null) {
            if (!authorization.isValid()) {
                throw new SecurityException("Authorization invalid");
            }
            return null;
        }
        if (authorizationProcess != null) {
            authorizationProcess.ensureAuthorization();
            securityContext = getContext();
            authorization = securityContext != null ? securityContext.getAuthorization() : null;
        }
        if (authorization == null || !authorization.isValid()) {
            throw new SecurityException("Authorization invalid");
        }
        return authorization;
    }

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
            var original = (SecurityContextImpl) value;
            var forkedValue = new SecurityContextImpl(original.securityContextHolder);
            forkedValue.setAuthentication(original.getAuthentication());
            forkedValue.setAuthorization(original.getAuthorization());
            return forkedValue;
        }

        @Override
        public void returnForkedValue(Object value, Object forkedValue) {
            // Intended blank
        }
    }
}
