package com.koch.ambeth.server.rest;

import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.log.ILoggerCache;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.ILoggerHistory;
import com.koch.ambeth.merge.security.ISecurityScopeProvider;
import com.koch.ambeth.security.DefaultAuthentication;
import com.koch.ambeth.security.IAuthentication;
import com.koch.ambeth.security.IAuthorization;
import com.koch.ambeth.security.ISecurityContext;
import com.koch.ambeth.security.ISecurityContextHolder;
import com.koch.ambeth.security.PasswordType;
import com.koch.ambeth.security.StringSecurityScope;
import com.koch.ambeth.server.rest.config.WebServiceConfigurationConstants;
import com.koch.ambeth.server.webservice.IHttpSessionProvider;
import com.koch.ambeth.util.Base64;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.state.AbstractStateRollback;
import com.koch.ambeth.util.state.IStateRollback;
import com.koch.ambeth.util.state.NoOpStateRollback;

public class AmbethServletAspect {
	public static final String ATTRIBUTE_AUTHENTICATION_HANDLE = "ambeth.authentication.handle";

	public static final String ATTRIBUTE_AUTHORIZATION_HANDLE = "ambeth.authorization.handle";

	public static final String USER_NAME = "login-name";

	public static final String USER_PASS = "login-pass";

	public static final String USER_PASS_TYPE = "login-pass-type";

	protected static final Charset utfCharset = Charset.forName("UTF-8");

	protected static final Pattern basicPattern = Pattern.compile("Basic *(.+) *",
			Pattern.CASE_INSENSITIVE);

	protected static final Pattern pattern = Pattern.compile("(.+) *\\: *(.+)");

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
		return pushServletAspect(request, new IStateRollback() {
			@Override
			public void rollback() {
				IServiceContext beanContext = getServiceContext(request);
				IThreadLocalCleanupController threadLocalCleanupController = beanContext
						.getService(IThreadLocalCleanupController.class);
				threadLocalCleanupController.cleanupThreadLocal();
			}
		});
	}

	public IStateRollback pushServletAspect(ServletRequest request, IStateRollback... rollbacks) {
		HttpServletRequest httpRequest = (HttpServletRequest) request;

		HttpSession session = httpRequest.getSession();
		IServiceContext beanContext = getServiceContext(request);

		IAuthentication authentication = resolveExplicitAuthentication(httpRequest);

		ILogger log = beanContext.getService(ILoggerCache.class).getCachedLogger(beanContext,
				AmbethServletAspect.class);

		IStateRollback rollback = NoOpStateRollback.instance;
		final ISecurityContextHolder securityContextHolder = beanContext
				.getService(ISecurityContextHolder.class, false);
		if (securityContextHolder == null) {
			if (log.isInfoEnabled()) {
				ILoggerHistory loggerHistory = beanContext.getService(ILoggerHistory.class);
				loggerHistory.infoOnce(log, this,
						"No security context holder available. Skip creating security Context!");
			}
		}
		else {
			if (authentication != null) {
				session.setAttribute(ATTRIBUTE_AUTHENTICATION_HANDLE, authentication);
				ISecurityContext securityContext = securityContextHolder.getCreateContext();
				securityContext.setAuthentication(authentication);
			}
			else {
				reuseValidSessionAuthentication(securityContextHolder, httpRequest, log);
			}
			rollback = new AbstractStateRollback(rollback) {
				@Override
				protected void rollbackIntern() throws Exception {
					securityContextHolder.clearContext();
				}
			};
		}
		boolean success = false;
		try {
			// set the current http session
			final IHttpSessionProvider httpSessionProvider = beanContext
					.getService(IHttpSessionProvider.class, false);
			if (httpSessionProvider != null) {
				rollback = httpSessionProvider.pushCurrentHttpSession(session, rollback);
			}
			// set the current security scope
			final ISecurityScopeProvider securityScopeProvider = beanContext
					.getService(ISecurityScopeProvider.class);
			rollback = securityScopeProvider.pushSecurityScopes(StringSecurityScope.DEFAULT_SCOPE,
					rollback);
			success = true;
			return rollback;
		}
		finally {
			if (!success) {
				rollback.rollback();
			}
		}
	}

	protected void reuseValidSessionAuthentication(ISecurityContextHolder securityContextHolder,
			HttpServletRequest request, ILogger log) {
		HttpSession session = request.getSession();
		IAuthentication authentication = (IAuthentication) session
				.getAttribute(ATTRIBUTE_AUTHENTICATION_HANDLE);
		if (authentication == null) {
			session.removeAttribute(ATTRIBUTE_AUTHORIZATION_HANDLE);
			return;
		}
		ISecurityContext securityContext = securityContextHolder.getCreateContext();
		securityContext.setAuthentication(authentication);

		Number servletAuthorizationTimeToLive = getProperty(request.getServletContext(), Number.class,
				WebServiceConfigurationConstants.SessionAuthorizationTimeToLive);
		if (servletAuthorizationTimeToLive == null || servletAuthorizationTimeToLive.longValue() <= 0) {
			session.removeAttribute(ATTRIBUTE_AUTHORIZATION_HANDLE);
			return;
		}
		IAuthorization authorization = (IAuthorization) session
				.getAttribute(ATTRIBUTE_AUTHORIZATION_HANDLE);
		if (authorization == null) {
			return;
		}
		// FIXME:Bad because not always sid == username, use IUserProvider
		if (!authorization.getSID().equalsIgnoreCase(authentication.getUserName())) {
			session.removeAttribute(ATTRIBUTE_AUTHORIZATION_HANDLE);
			if (log.isInfoEnabled()) {
				log.info("User authorization '" + authorization.getSID()
						+ "' has been invalidated because of authentication '" + authentication.getUserName()
						+ "' has been bound to session");
			}
			authorization = null;
			session.invalidate();
			return;
		}
		long authorizationAge = System.currentTimeMillis() - authorization.getAuthorizationTime();
		if (authorizationAge <= servletAuthorizationTimeToLive.longValue()) {
			securityContext.setAuthorization(authorization);
		}
	}

	protected IAuthentication resolveExplicitAuthentication(HttpServletRequest request) {
		String userName = request.getHeader(USER_NAME);
		String userPass = request.getHeader(USER_PASS);
		String passwordType = request.getHeader(USER_PASS_TYPE);

		if (userName == null && userPass == null) {
			// default BASIC AUTH
			String basicAuth = request.getHeader("Authorization");
			if (basicAuth != null) {
				Matcher basicMatcher = basicPattern.matcher(basicAuth);
				if (!basicMatcher.matches()) {
					throw new IllegalStateException(basicAuth);
				}
				String group = basicMatcher.group(1);
				byte[] decodedAuthorization = Base64.decodeBase64(group.getBytes(utfCharset));

				String decodedValue = new String(decodedAuthorization, utfCharset);

				Matcher matcher = pattern.matcher(decodedValue);
				if (!matcher.matches()) {
					throw new IllegalStateException(decodedValue);
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
		PasswordType passwordTypeEnum = passwordType != null ? PasswordType.valueOf(passwordType)
				: PasswordType.PLAIN;
		return new DefaultAuthentication(userName, userPass != null ? userPass.toCharArray() : null,
				passwordTypeEnum);
	}

	// LOGIN:
	// sessionCreated => requestInitialized => authChangeActive => login => authorizationChanged 5 =>
	// requestDestroyed
	//
	// LOGOUT
	// requestInitialized => authorizationChanged => sessionDestroyed => requestDestroyed =>
	// authorizationChanged

	protected <T> T getProperty(ServletContext servletContext, Class<T> propertyType,
			String propertyName) {
		Object value = getService(servletContext, IProperties.class).get(propertyName);
		return getService(servletContext, IConversionHelper.class).convertValueToType(propertyType,
				value);
	}

	protected <T> T getService(ServletContext servletContext, Class<T> serviceType) {
		return getServiceContext(servletContext).getService(serviceType);
	}

	protected <T> T getService(ServletContext servletContext, String beanName, Class<T> serviceType) {
		return getServiceContext(servletContext).getService(beanName, serviceType);
	}

	/**
	 *
	 * @return The singleton IServiceContext which is stored in the context of the servlet
	 */
	protected IServiceContext getServiceContext(ServletContext servletContext) {
		return (IServiceContext) servletContext
				.getAttribute(AmbethServletListener.ATTRIBUTE_I_SERVICE_CONTEXT);
	}
}
