package com.koch.ambeth.webservice;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.ext.Provider;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.log.ILoggerCache;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.merge.security.ISecurityScopeProvider;
import com.koch.ambeth.security.DefaultAuthentication;
import com.koch.ambeth.security.IAuthentication;
import com.koch.ambeth.security.IAuthorization;
import com.koch.ambeth.security.ISecurityContext;
import com.koch.ambeth.security.ISecurityContextHolder;
import com.koch.ambeth.security.PasswordType;
import com.koch.ambeth.security.StringSecurityScope;
import com.koch.ambeth.server.webservice.IHttpSessionProvider;
import com.koch.ambeth.service.model.ISecurityScope;
import com.koch.ambeth.webservice.config.WebServiceConfigurationConstants;

@WebFilter
@Provider
public class AmbethServletRequestFilter extends AmbethSimpleServletRequestFilter
{
	public static final String ATTRIBUTE_AUTHENTICATION_HANDLE = "ambeth.authentication.handle";

	public static final String ATTRIBUTE_AUTHORIZATION_HANDLE = "ambeth.authorization.handle";

	public static final String USER_NAME = "login-name";

	public static final String USER_PASS = "login-pass";

	public static final String USER_PASS_TYPE = "login-pass-type";

	@Override
	protected void doFilterIntern(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		String userName = request.getParameter(USER_NAME);
		String userPass = request.getParameter(USER_PASS);
		String passwordType = request.getParameter(USER_PASS_TYPE);
		HttpSession session = ((HttpServletRequest) request).getSession();
		ServletContext servletContext = session.getServletContext();
		IServiceContext beanContext = getServiceContext(servletContext);

		ILogger log = beanContext.getService(ILoggerCache.class).getCachedLogger(beanContext, AmbethServletRequestFilter.class);

		ISecurityContextHolder securityContextHolder = beanContext.getService(ISecurityContextHolder.class);
		if (userName != null)
		{
			PasswordType passwordTypeEnum = passwordType != null ? PasswordType.valueOf(passwordType) : PasswordType.PLAIN;
			DefaultAuthentication authentication = new DefaultAuthentication(userName, userPass != null ? userPass.toCharArray() : null, passwordTypeEnum);
			session.setAttribute(ATTRIBUTE_AUTHENTICATION_HANDLE, authentication);
			setAuthentication(servletContext, authentication);
		}
		else
		{
			IAuthentication authentication = (IAuthentication) session.getAttribute(ATTRIBUTE_AUTHENTICATION_HANDLE);
			if (authentication != null)
			{
				beanContext.getService(ISecurityContextHolder.class).getCreateContext();
				setAuthentication(servletContext, authentication);

				Number servletAuthorizationTimeToLive = getProperty(servletContext, Number.class,
						WebServiceConfigurationConstants.SessionAuthorizationTimeToLive);
				if (servletAuthorizationTimeToLive != null && servletAuthorizationTimeToLive.longValue() > 0)
				{
					IAuthorization authorization = (IAuthorization) session.getAttribute(ATTRIBUTE_AUTHORIZATION_HANDLE);
					// FIXME:Bad because not always sid == username, use IUserProvider
					if (authorization != null && !authorization.getSID().equalsIgnoreCase(authentication.getUserName()))
					{
						session.setAttribute(ATTRIBUTE_AUTHORIZATION_HANDLE, null);
						log.info("User authorization '" + authorization.getSID() + "' has been invalidated because of authentication '"
								+ authentication.getUserName() + "' has been bound to session");
						authorization = null;
						session.invalidate();
						return;
					}
					if (authorization != null
							&& System.currentTimeMillis() - authorization.getAuthorizationTime() <= servletAuthorizationTimeToLive.longValue())
					{
						ISecurityContext securityContext = securityContextHolder.getCreateContext();
						securityContext.setAuthorization(authorization);
					}
				}
			}
		}
		try
		{
			setHttpSession(beanContext, request, response, chain);
		}
		finally
		{
			securityContextHolder.clearContext();
		}
	}

	protected void setHttpSession(IServiceContext beanContext, ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException
	{
		HttpSession session = ((HttpServletRequest) request).getSession();
		IHttpSessionProvider httpSessionProvider = beanContext.getService(IHttpSessionProvider.class);
		HttpSession oldSession = httpSessionProvider.getCurrentHttpSession();
		httpSessionProvider.setCurrentHttpSession(session);
		try
		{
			setSecurityScope(beanContext, request, response, chain);
		}
		finally
		{
			httpSessionProvider.setCurrentHttpSession(oldSession);
		}
	}

	protected void setSecurityScope(IServiceContext beanContext, ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException
	{
		ISecurityScopeProvider securityScopeProvider = beanContext.getService(ISecurityScopeProvider.class);
		ISecurityScope[] oldSecurityScopes = securityScopeProvider.getSecurityScopes();
		securityScopeProvider.setSecurityScopes(new ISecurityScope[] { StringSecurityScope.DEFAULT_SCOPE });
		try
		{
			chain.doFilter(request, response);
		}
		finally
		{
			securityScopeProvider.setSecurityScopes(oldSecurityScopes);
		}
	}

	// LOGIN:
	// sessionCreated => requestInitialized => authChangeActive => login => authorizationChanged 5 => requestDestroyed
	//
	// LOGOUT
	// requestInitialized => authorizationChanged => sessionDestroyed => requestDestroyed => authorizationChanged

}
