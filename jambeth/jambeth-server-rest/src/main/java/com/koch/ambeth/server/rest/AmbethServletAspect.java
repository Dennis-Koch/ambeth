package com.koch.ambeth.server.rest;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.log.ILoggerCache;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.ILoggerHistory;
import com.koch.ambeth.merge.security.ISecurityScopeProvider;
import com.koch.ambeth.security.DefaultAuthentication;
import com.koch.ambeth.security.IAuthentication;
import com.koch.ambeth.security.IAuthorization;
import com.koch.ambeth.security.ISecurityContextHolder;
import com.koch.ambeth.security.PasswordType;
import com.koch.ambeth.security.StringSecurityScope;
import com.koch.ambeth.server.rest.config.WebServiceConfigurationConstants;
import com.koch.ambeth.server.webservice.IHttpSessionProvider;
import com.koch.ambeth.util.Base64;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.state.IStateRollback;
import com.koch.ambeth.util.state.StateRollback;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.nio.charset.Charset;
import java.util.regex.Pattern;

public class AmbethServletAspect {
    public static final String ATTRIBUTE_AUTHENTICATION_HANDLE = "ambeth.authentication.handle";

    public static final String ATTRIBUTE_AUTHORIZATION_HANDLE = "ambeth.authorization.handle";

    public static final String USER_NAME = "login-name";

    public static final String USER_PASS = "login-pass";

    public static final String USER_PASS_TYPE = "login-pass-type";

    protected static final Charset utfCharset = Charset.forName("UTF-8");

    protected static final Pattern basicPattern = Pattern.compile("Basic *(.+) *", Pattern.CASE_INSENSITIVE);

    protected static final Pattern pattern = Pattern.compile(" *(.+) *\\:(.*)?");

    protected IServiceContext beanContext;

    public IServiceContext getServiceContext(ServletRequest request) {
        if (beanContext != null) {
            return beanContext;
        }
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpSession session = httpRequest.getSession();
        ServletContext servletContext = session.getServletContext();
        return getServiceContext(servletContext);
    }

    public void setBeanContext(IServiceContext beanContext) {
        this.beanContext = beanContext;
    }

    public IStateRollback pushServletAspectWithThreadLocals(final ServletRequest request) {
        return StateRollback.chain(chain -> {
            chain.append(() -> {
                var beanContext = getServiceContext(request);
                var threadLocalCleanupController = beanContext.getService(IThreadLocalCleanupController.class);
                threadLocalCleanupController.cleanupThreadLocal();
            });
            chain.append(pushServletAspect(request));
        });
    }

    public IStateRollback pushServletAspect(final ServletRequest request) {
        var httpRequest = (HttpServletRequest) request;

        var session = httpRequest.getSession();
        var beanContext = getServiceContext(request);

        var authentication = resolveExplicitAuthentication(httpRequest);

        var log = beanContext.getService(ILoggerCache.class).getCachedLogger(beanContext, AmbethServletAspect.class);

        return StateRollback.chain(chain -> {
            var securityContextHolder = beanContext.getService(ISecurityContextHolder.class, false);
            if (securityContextHolder == null) {
                if (log.isInfoEnabled()) {
                    var loggerHistory = beanContext.getService(ILoggerHistory.class);
                    loggerHistory.infoOnce(log, this, "No security context holder available. Skip creating security context!");
                }
            } else {
                if (authentication != null) {
                    var securityContext = securityContextHolder.getCreateContext();
                    securityContext.setAuthentication(authentication);
                } else {
                    reuseValidSessionAuthentication(securityContextHolder, beanContext, httpRequest, log);
                }
                chain.append(() -> securityContextHolder.clearContext());
            }
            // set the current http session
            var httpSessionProvider = beanContext.getService(IHttpSessionProvider.class, false);
            if (httpSessionProvider != null) {
                chain.append(httpSessionProvider.pushCurrentHttpSession(session, httpRequest));
            }
            // set the current security scope
            var securityScopeProvider = beanContext.getService(ISecurityScopeProvider.class);
            chain.append(securityScopeProvider.pushSecurityScopes(StringSecurityScope.DEFAULT_SCOPE));
            chain.append(pushThreadLocalHandled(request));
        });
    }

    private IStateRollback pushThreadLocalHandled(final ServletRequest request) {
        var threadLocalHandled = (Boolean) request.getAttribute(AbstractServiceREST.THREAD_LOCAL_HANDLED);

        if (Boolean.TRUE.equals(threadLocalHandled)) {
            return StateRollback.empty();
        }
        request.setAttribute(AbstractServiceREST.THREAD_LOCAL_HANDLED, Boolean.TRUE);
        return () -> request.removeAttribute(AbstractServiceREST.THREAD_LOCAL_HANDLED);
    }

    protected void reuseValidSessionAuthentication(ISecurityContextHolder securityContextHolder, IServiceContext beanContext, HttpServletRequest request, ILogger log) {
        var session = request.getSession();
        var authentication = (IAuthentication) session.getAttribute(ATTRIBUTE_AUTHENTICATION_HANDLE);
        if (authentication == null) {
            session.removeAttribute(ATTRIBUTE_AUTHORIZATION_HANDLE);
            return;
        }
        var securityContext = securityContextHolder.getCreateContext();
        securityContext.setAuthentication(authentication);

        var servletAuthorizationTimeToLive = getProperty(beanContext, Number.class, WebServiceConfigurationConstants.SessionAuthorizationTimeToLive);
        if (servletAuthorizationTimeToLive == null || servletAuthorizationTimeToLive.longValue() <= 0) {
            session.removeAttribute(ATTRIBUTE_AUTHORIZATION_HANDLE);
            return;
        }
        var authorization = (IAuthorization) session.getAttribute(ATTRIBUTE_AUTHORIZATION_HANDLE);
        if (authorization == null) {
            return;
        }
        // FIXME:Bad because not always sid == username, use IUserProvider
        if (!authorization.getSID().equalsIgnoreCase(authentication.getUserName())) {
            session.removeAttribute(ATTRIBUTE_AUTHORIZATION_HANDLE);
            if (log.isInfoEnabled()) {
                log.info("User authorization '" + authorization.getSID() + "' has been invalidated because of authentication '" + authentication.getUserName() + "' has been bound to session");
            }
            session.invalidate();
            return;
        }
        var authorizationAge = System.currentTimeMillis() - authorization.getAuthorizationTime();
        if (authorizationAge <= servletAuthorizationTimeToLive.longValue()) {
            securityContext.setAuthorization(authorization);
        }
    }

    protected IAuthentication resolveExplicitAuthentication(HttpServletRequest request) {
        var userName = request.getHeader(USER_NAME);
        var userPass = request.getHeader(USER_PASS);
        var passwordType = request.getHeader(USER_PASS_TYPE);

        if (userName == null && userPass == null) {
            // default BASIC AUTH
            var basicAuth = request.getHeader("Authorization");
            if (basicAuth != null) {
                var basicMatcher = basicPattern.matcher(basicAuth);
                if (!basicMatcher.matches()) {
                    throw new IllegalStateException(basicAuth);
                }
                var group = basicMatcher.group(1);
                var decodedAuthorization = Base64.decodeBase64(group.getBytes(utfCharset));

                var decodedValue = new String(decodedAuthorization, utfCharset);

                var matcher = pattern.matcher(decodedValue);
                if (!matcher.matches()) {
                    throw new SecurityException("Illegal BASIC AUTH parameter (no username/password pair)");
                }
                userName = matcher.group(1);
                userPass = matcher.group(2);
            }
        }
        if (userName == null) {
            userName = request.getParameter(USER_NAME);
        }
        if (userPass == null) {
            userPass = request.getParameter(USER_PASS);
        }
        if (passwordType == null) {
            passwordType = request.getParameter(USER_PASS_TYPE);
        }
        if (userName == null) {
            return null;
        }
        var passwordTypeEnum = passwordType != null ? PasswordType.valueOf(passwordType) : PasswordType.PLAIN;
        return new DefaultAuthentication(userName.trim(), userPass != null ? userPass.toCharArray() : null, passwordTypeEnum);
    }

    // LOGIN:
    // sessionCreated => requestInitialized => authChangeActive => login => authorizationChanged 5 =>
    // requestDestroyed
    //
    // LOGOUT
    // requestInitialized => authorizationChanged => sessionDestroyed => requestDestroyed =>
    // authorizationChanged

    protected <T> T getProperty(IServiceContext beanContext, Class<T> propertyType, String propertyName) {
        var value = beanContext.getService(IProperties.class).get(propertyName);
        return beanContext.getService(IConversionHelper.class).convertValueToType(propertyType, value);
    }

    /**
     * @return The singleton IServiceContext which is stored in the context of the servlet
     */
    protected IServiceContext getServiceContext(ServletContext servletContext) {
        return (IServiceContext) servletContext.getAttribute(AmbethServletListener.ATTRIBUTE_I_SERVICE_CONTEXT);
    }
}
