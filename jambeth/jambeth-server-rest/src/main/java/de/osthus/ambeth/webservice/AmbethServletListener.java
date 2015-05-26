package de.osthus.ambeth.webservice;

import java.nio.charset.Charset;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.ws.rs.ext.Provider;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.event.IEventDispatcher;
import de.osthus.ambeth.ioc.BootstrapScannerModule;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LoggerFactory;
import de.osthus.ambeth.platform.IAmbethPlatformContext;
import de.osthus.ambeth.platform.IPlatformContextConfiguration;
import de.osthus.ambeth.platform.PlatformContextConfiguration;
import de.osthus.ambeth.security.DefaultAuthentication;
import de.osthus.ambeth.security.IAuthentication;
import de.osthus.ambeth.security.IAuthorization;
import de.osthus.ambeth.security.IAuthorizationChangeListener;
import de.osthus.ambeth.security.IAuthorizationChangeListenerExtendable;
import de.osthus.ambeth.security.ISecurityContext;
import de.osthus.ambeth.security.ISecurityContextHolder;
import de.osthus.ambeth.security.PasswordType;
import de.osthus.ambeth.util.ClassLoaderUtil;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.ImmutableTypeSet;
import de.osthus.ambeth.webservice.config.WebServiceConfigurationConstants;

@Provider
public class AmbethServletListener implements ServletContextListener, ServletRequestListener, HttpSessionListener, IAuthorizationChangeListener
{
	public static final String ATTRIBUTE_AUTHENTICATION_HANDLE = "ambeth.authentication.handle";

	public static final String ATTRIBUTE_AUTHORIZATION_HANDLE = "ambeth.authorization.handle";

	/**
	 * The name of the attribute in servlet context that holds an instance of IServiceContext
	 */
	public static final String ATTRIBUTE_I_SERVICE_CONTEXT = "ambeth.IServiceContext";

	public static final String ATTRIBUTE_I_PLATFORM_CONTEXT = "ambeth.PlatformContext";

	public static final String USER_NAME = "login-name";

	public static final String USER_PASS = "login-pass";

	public static final String USER_PASS_TYPE = "login-pass-type";

	protected final Charset utfCharset = Charset.forName("UTF-8");

	protected ServletContext servletContext;

	protected final ThreadLocal<ArrayList<Boolean>> authorizationChangeActiveTL = new ThreadLocal<ArrayList<Boolean>>();

	private ILogger log;

	@Override
	public void contextInitialized(ServletContextEvent event)
	{
		servletContext = event.getServletContext();
		@SuppressWarnings("unchecked")
		Enumeration<String> initParameterNames = servletContext.getInitParameterNames();
		while (initParameterNames.hasMoreElements())
		{
			String initParamName = initParameterNames.nextElement();
			Object initParamValue = servletContext.getInitParameter(initParamName);
			de.osthus.ambeth.config.Properties.getApplication().put(initParamName, initParamValue);
		}
		de.osthus.ambeth.config.Properties.loadBootstrapPropertyFile();
		IAmbethPlatformContext context = null;
		log = LoggerFactory.getLogger(AmbethServletListener.class, de.osthus.ambeth.config.Properties.getApplication());
		try
		{
			if (log.isInfoEnabled())
			{
				log.info("Starting...");
			}
			Properties props = new Properties(de.osthus.ambeth.config.Properties.getApplication());
			IPlatformContextConfiguration pcc = PlatformContextConfiguration.create();
			pcc.addFrameworkModule(new IInitializingModule()
			{
				@Override
				public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
				{
					beanContextFactory.registerExternalBean(servletContext).autowireable(ServletContext.class);
					beanContextFactory.registerBean(HttpSessionBean.class).ignoreProperties("CurrentHttpSession")
							.autowireable(HttpSession.class, IHttpSessionProvider.class);

					beanContextFactory.link(AmbethServletListener.this).to(IAuthorizationChangeListenerExtendable.class).optional();
				}
			});
			pcc.addProperties(props);
			// HOTFIX: Should be coded better if there is time
			pcc.addProviderModule(new BootstrapScannerModule()
			{
				@Override
				protected ServletContext getServletContext()
				{
					return servletContext;
				}
			});
			context = pcc.createPlatformContext();

			// store the instance of IServiceContext in servlet context
			event.getServletContext().setAttribute(ATTRIBUTE_I_SERVICE_CONTEXT, context.getBeanContext());
			event.getServletContext().setAttribute(ATTRIBUTE_I_PLATFORM_CONTEXT, context);

			if (log.isInfoEnabled())
			{
				log.info("Start completed");
			}
		}
		catch (RuntimeException e)
		{
			if (log.isErrorEnabled())
			{
				log.error(e);
			}
			throw e;
		}
		finally
		{
			if (context != null)
			{
				context.clearThreadLocal();
			}
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent event)
	{
		try
		{
			log.info("Shutting down...");
			// remove the instance of IServiceContext in servlet context
			event.getServletContext().removeAttribute(ATTRIBUTE_I_SERVICE_CONTEXT);

			IAmbethPlatformContext platformContext = (IAmbethPlatformContext) event.getServletContext().getAttribute(ATTRIBUTE_I_PLATFORM_CONTEXT);
			event.getServletContext().removeAttribute(ATTRIBUTE_I_PLATFORM_CONTEXT);

			// dispose the IServiceContext
			if (platformContext != null)
			{
				platformContext.dispose();
			}
			ClassLoader currentCL = Thread.currentThread().getContextClassLoader();
			Enumeration<Driver> drivers = DriverManager.getDrivers();
			while (drivers.hasMoreElements())
			{
				Driver driver = drivers.nextElement();
				ClassLoader driverCL = driver.getClass().getClassLoader();
				if (!ClassLoaderUtil.isParentOf(currentCL, driverCL))
				{
					// this driver is not associated to the current CL
					continue;
				}
				try
				{
					DriverManager.deregisterDriver(driver);
				}
				catch (SQLException e)
				{
					if (log.isErrorEnabled())
					{
						log.error("Error deregistering driver " + driver, e);
					}
				}
			}
			if (log.isInfoEnabled())
			{
				log.info("Shutdown completed");
			}
		}
		finally
		{
			ImmutableTypeSet.flushState();
		}
	}

	@Override
	public void requestInitialized(ServletRequestEvent sre)
	{
		ServletRequest servletRequest = sre.getServletRequest();
		String userName = servletRequest.getParameter(USER_NAME);
		String userPass = servletRequest.getParameter(USER_PASS);
		String passwordType = servletRequest.getParameter(USER_PASS_TYPE);
		HttpSession session = ((HttpServletRequest) servletRequest).getSession();
		ServletContext servletContext = sre.getServletContext();

		IServiceContext beanContext = getServiceContext(servletContext);
		beanContext.getService(IHttpSessionProvider.class).setCurrentHttpSession(session);

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

				Number servletAuthorizationTimeToLive = getProperty(sre.getServletContext(), Number.class,
						WebServiceConfigurationConstants.SessionAuthorizationTimeToLive);
				if (servletAuthorizationTimeToLive != null && servletAuthorizationTimeToLive.longValue() > 0)
				{
					IAuthorization authorization = (IAuthorization) session.getAttribute(ATTRIBUTE_AUTHORIZATION_HANDLE);
					// FIXME:Bad because not always sid == username, use IUserProvider
					if (authorization != null && !authorization.getSID().equalsIgnoreCase(authentication.getUserName()))
					{
						throw new IllegalStateException("Different authorization and authentication, try to figure out why sid != username");
					}

					if (authorization != null
							&& System.currentTimeMillis() - authorization.getAuthorizationTime() <= servletAuthorizationTimeToLive.longValue())
					{
						setAuthorization(servletContext, authorization);
					}
				}
			}
		}
	}

	@Override
	public void authorizationChanged(IAuthorization authorization)
	{
		if (authorization == null)
		{
			return;
		}
		IServiceContext beanContext = getServiceContext(servletContext);
		IHttpSessionProvider httpSessionProvider = beanContext.getService(IHttpSessionProvider.class);
		if (httpSessionProvider.getCurrentHttpSession() != null)
		{
			beanContext.getService(HttpSession.class).setAttribute(ATTRIBUTE_AUTHORIZATION_HANDLE, authorization);
		}
	}

	protected <T> T getProperty(ServletContext servletContext, Class<T> propertyType, String propertyName)
	{
		Object value = getService(servletContext, IProperties.class).get(propertyName);
		return getService(servletContext, IConversionHelper.class).convertValueToType(propertyType, value);
	}

	@Override
	public void requestDestroyed(ServletRequestEvent sre)
	{
		IServiceContext beanContext = getServiceContext(sre.getServletContext());
		beanContext.getService(IHttpSessionProvider.class).setCurrentHttpSession(null);
		beanContext.getService(ISecurityContextHolder.class).clearContext();
		beanContext.getService(IThreadLocalCleanupController.class).cleanupThreadLocal();
	}

	protected void setAuthentication(ServletContext servletContext, IAuthentication authentication)
	{
		ISecurityContext securityContext = getService(servletContext, ISecurityContextHolder.class).getCreateContext();
		securityContext.setAuthentication(authentication);
	}

	protected void setAuthorization(ServletContext servletContext, IAuthorization authorization)
	{
		ISecurityContext securityContext = getService(servletContext, ISecurityContextHolder.class).getCreateContext();
		securityContext.setAuthorization(authorization);
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
		return (IServiceContext) servletContext.getAttribute(ATTRIBUTE_I_SERVICE_CONTEXT);
	}

	@Override
	public void sessionCreated(HttpSessionEvent se)
	{
		// intended blank
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se)
	{
		IServiceContext beanContext = getServiceContext(servletContext);

		beanContext.getService(IEventDispatcher.class).dispatchEvent(se);
	}

	// LOGIN:
	// sessionCreated => requestInitialized => authChangeActive => login => authorizationChanged 5 => requestDestroyed
	//
	// LOGOUT
	// requestInitialized => authorizationChanged => sessionDestroyed => requestDestroyed => authorizationChanged

}
