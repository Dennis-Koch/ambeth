package com.koch.ambeth.security.server;

import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.IocConfigurationConstants;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.security.ISecurityActivation;
import com.koch.ambeth.merge.security.ISecurityScopeProvider;
import com.koch.ambeth.security.IAuthentication;
import com.koch.ambeth.security.IAuthenticationManager;
import com.koch.ambeth.security.IAuthenticationResult;
import com.koch.ambeth.security.IAuthorization;
import com.koch.ambeth.security.IAuthorizationExceptionFactory;
import com.koch.ambeth.security.IAuthorizationManager;
import com.koch.ambeth.security.IAuthorizationProcess;
import com.koch.ambeth.security.ISecurityContext;
import com.koch.ambeth.security.ISecurityContextHolder;
import com.koch.ambeth.security.ISidHelper;
import com.koch.ambeth.security.events.UserAuthenticatedEvent;
import com.koch.ambeth.security.exceptions.AuthenticationMissingException;
import com.koch.ambeth.security.exceptions.InvalidUserException;
import com.koch.ambeth.security.exceptions.PasswordChangeRequiredException;
import com.koch.ambeth.service.model.ISecurityScope;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.transaction.ILightweightTransaction;

public class AuthorizationProcess implements IAuthorizationProcess {
    private static final ThreadLocal<Boolean> ignoreInvalidUserTL = new ThreadLocal<>();

    public static boolean setIgnoreInvalidUser(boolean value) {
        Boolean oldValue = ignoreInvalidUserTL.get();
        if (value) {
            ignoreInvalidUserTL.set(Boolean.TRUE);
        } else {
            ignoreInvalidUserTL.set(null);
        }
        return oldValue != null ? oldValue.booleanValue() : false;
    }
    @Autowired
    protected IAuthenticationManager authenticationManager;
    @Autowired(optional = true)
    protected IAuthorizationExceptionFactory authorizationExceptionFactory;
    @Autowired
    protected IAuthorizationManager authorizationManager;
    @Autowired
    protected IEventDispatcher eventDispatcher;
    @Autowired
    protected ISecurityActivation securityActivation;
    @Autowired
    protected ISecurityContextHolder securityContextHolder;
    @Autowired
    protected ISecurityScopeProvider securityScopeProvider;
    @Autowired(optional = true)
    protected ISidHelper sidHelper;
    @Autowired(optional = true)
    protected ILightweightTransaction transaction;
    @Property(name = IocConfigurationConstants.DebugModeActive, defaultValue = "false")
    protected boolean debugModeActive;
    @LogInstance
    private ILogger log;

    @Override
    public void ensureAuthorization() {
        if (!securityActivation.isSecured()) {
            return;
        }
        try {
            ISecurityContext securityContext = securityContextHolder.getContext();
            if (securityContext == null) {
                throw new AuthenticationMissingException();
            }
            IAuthentication authentication = securityContext.getAuthentication();
            IAuthorization previousAuthorization = securityContext.getAuthorization();
            ISecurityScope[] previousSecurityScopes = null;
            boolean success = false;
            try {
                IAuthorization authorization = previousAuthorization != null ? previousAuthorization : createAuthorization(authentication);
                if (authorization == null || (!Boolean.TRUE.equals(ignoreInvalidUserTL.get()) && !authorization.isValid())) {
                    if (authorizationExceptionFactory != null) {
                        RuntimeException authorizationException = authorizationExceptionFactory.createAuthorizationException(authentication, authorization);
                        if (authorizationException != null) {
                            throw authorizationException;
                        }
                    }
                    String userName = authentication != null ? authentication.getUserName() : null;
                    String sid = authorization != null ? authorization.getSID() : null;

                    if (userName == null && sid == null) {
                        throw new AuthenticationMissingException();
                    }
                    throw new InvalidUserException("User '" + (sid != null ? sid : userName) + "' is not a valid user.");
                }
                if (authorization.isValid() && authorization.getAuthenticationResult().isChangePasswordRequired()) {
                    throw new PasswordChangeRequiredException();
                }
                securityContext.setAuthorization(authorization);
                success = true;
                eventDispatcher.dispatchEvent(new UserAuthenticatedEvent(authorization));
            } finally {
                if (!success) {
                    if (securityContext != null) {
                        securityContext.setAuthorization(previousAuthorization);
                    }
                    if (previousSecurityScopes != null) {
                        securityScopeProvider.setSecurityScopes(previousSecurityScopes);
                    }
                }
            }
        } catch (RuntimeException e) {
            if (!debugModeActive) {
                e.setStackTrace(RuntimeExceptionUtil.EMPTY_STACK_TRACE);
            }
            throw e;
        }
    }

    @Override
    public boolean tryAuthorization() {
        if (!securityActivation.isSecured()) {
            return true;
        }
        ISecurityContext securityContext = securityContextHolder.getContext();
        if (securityContext == null) {
            return false;
        }
        IAuthentication authentication = securityContext.getAuthentication();
        IAuthorization previousAuthorization = securityContext.getAuthorization();
        ISecurityScope[] previousSecurityScopes = null;
        boolean success = false;
        try {
            IAuthorization authorization = previousAuthorization != null ? previousAuthorization : createAuthorization(authentication);
            if (authorization == null || (!Boolean.TRUE.equals(ignoreInvalidUserTL.get()) && !authorization.isValid())) {
                return false;
            }
            securityContext.setAuthorization(authorization);
            success = true;
            eventDispatcher.dispatchEvent(new UserAuthenticatedEvent(authorization));
            return true;
        } finally {
            if (!success) {
                if (securityContext != null) {
                    securityContext.setAuthorization(previousAuthorization);
                }
                if (previousSecurityScopes != null) {
                    securityScopeProvider.setSecurityScopes(previousSecurityScopes);
                }
            }
        }
    }

    protected IAuthorization createAuthorization(final IAuthentication authentication) {
        if (authentication == null) {
            return null;
        }
        IAuthenticationResult authenticationResult = authenticationManager.authenticate(authentication);
        String sid = authenticationResult.getSID();
        String databaseSid = sidHelper != null ? sidHelper.convertOperatingSystemSidToFrameworkSid(sid) : sid;

        return authorizationManager.authorize(databaseSid, securityScopeProvider.getSecurityScopes(), authenticationResult);
    }
}
