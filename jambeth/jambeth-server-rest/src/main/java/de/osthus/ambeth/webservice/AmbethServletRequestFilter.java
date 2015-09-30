package de.osthus.ambeth.webservice;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.ext.Provider;

import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.ILoggerCache;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.security.DefaultAuthentication;
import de.osthus.ambeth.security.IAuthentication;
import de.osthus.ambeth.security.IAuthorization;
import de.osthus.ambeth.security.ISecurityContext;
import de.osthus.ambeth.security.ISecurityContextHolder;
import de.osthus.ambeth.security.ISecurityScopeProvider;
import de.osthus.ambeth.security.PasswordType;
import de.osthus.ambeth.security.StringSecurityScope;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.webservice.config.WebServiceConfigurationConstants;

@WebFilter
@Provider
public class AmbethServletRequestFilter implements Filter
{
	public static final String ATTRIBUTE_AUTHENTICATION_HANDLE = "ambeth.authentication.handle";

	public static final String ATTRIBUTE_AUTHORIZATION_HANDLE = "ambeth.authorization.handle";

	public static final String USER_NAME = "login-name";

	public static final String USER_PASS = "login-pass";

	public static final String USER_PASS_TYPE = "login-pass-type";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException
	{
		// intended blank
	}

	@Override
	public void destroy()
	{
		// intended blank
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		String userName = request.getParameter(USER_NAME);
		String userPass = request.getParameter(USER_PASS);
		String passwordType = request.getParameter(USER_PASS_TYPE);
		HttpSession session = ((HttpServletRequest) request).getSession();
		ServletContext servletContext = request.getServletContext();
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
			beanContext.getService(IThreadLocalCleanupController.class).cleanupThreadLocal();
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

	protected <T> T getProperty(ServletContext servletContext, Class<T> propertyType, String propertyName)
	{
		Object value = getService(servletContext, IProperties.class).get(propertyName);
		return getService(servletContext, IConversionHelper.class).convertValueToType(propertyType, value);
	}

	protected void setAuthentication(ServletContext servletContext, IAuthentication authentication)
	{
		ISecurityContext securityContext = getService(servletContext, ISecurityContextHolder.class).getCreateContext();
		securityContext.setAuthentication(authentication);
	}

	protected <T> T getService(ServletContext servletContext, Class<T> serviceType)
	{
		return getServiceContext(servletContext).getService(serviceType);
	}

	protected <T> T getService(ServletContext servletContext, String beanName, Class<T> serviceType)
	{
		return getServiceContext(servletContext).getService(beanName, serviceType);
	}

	/**
	 * 
	 * @return The singleton IServiceContext which is stored in the context of the servlet
	 */
	protected IServiceContext getServiceContext(ServletContext servletContext)
	{
		return (IServiceContext) servletContext.getAttribute(AmbethServletListener.ATTRIBUTE_I_SERVICE_CONTEXT);
	}

	// LOGIN:
	// sessionCreated => requestInitialized => authChangeActive => login => authorizationChanged 5 => requestDestroyed
	//
	// LOGOUT
	// requestInitialized => authorizationChanged => sessionDestroyed => requestDestroyed => authorizationChanged

}
