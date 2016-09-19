package de.osthus.ambeth.webservice;

import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.ws.rs.ext.Provider;

import de.osthus.ambeth.Ambeth;
import de.osthus.ambeth.bundle.IBundleModule;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.event.IEventDispatcher;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LoggerFactory;
import de.osthus.ambeth.start.IAmbethApplication;
import de.osthus.ambeth.start.IAmbethConfiguration;
import de.osthus.ambeth.start.ServletConfiguratonExtension;
import de.osthus.ambeth.util.ClassLoaderUtil;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.ImmutableTypeSet;
import de.osthus.ambeth.webservice.config.WebServiceConfigurationConstants;

@WebListener
@Provider
public class AmbethServletListener implements ServletContextListener, HttpSessionListener
{
	/**
	 * The name of the attribute in servlet context that holds an instance of IServiceContext
	 */
	public static final String ATTRIBUTE_I_SERVICE_CONTEXT = "ambeth.IServiceContext";

	public static final String ATTRIBUTE_I_APPLICATION = "ambeth.Application";

	protected final Charset utfCharset = Charset.forName("UTF-8");

	protected ServletContext servletContext;

	protected final ThreadLocal<ArrayList<Boolean>> authorizationChangeActiveTL = new ThreadLocal<ArrayList<Boolean>>();

	private ILogger log;

	@Override
	public void contextInitialized(ServletContextEvent event)
	{
		servletContext = event.getServletContext();
		Enumeration<String> initParameterNames = servletContext.getInitParameterNames();

		Properties properties = Properties.getApplication();
		while (initParameterNames.hasMoreElements())
		{
			String initParamName = initParameterNames.nextElement();
			Object initParamValue = servletContext.getInitParameter(initParamName);
			properties.put(initParamName, initParamValue);
		}
		Properties.loadBootstrapPropertyFile();
		log = LoggerFactory.getLogger(AmbethServletListener.class, properties);

		IAmbethApplication ambethApp = null;
		try
		{
			if (log.isInfoEnabled())
			{
				log.info("Starting...");
			}

			String classpathScanning = properties.getString(WebServiceConfigurationConstants.ClasspathScanning);
			boolean scanClasspath = (classpathScanning == null ? true : Boolean.parseBoolean(classpathScanning));

			String bundle = properties.getString(WebServiceConfigurationConstants.FrameworkBundle);
			if (scanClasspath && bundle != null)
			{
				throw new RuntimeException(WebServiceConfigurationConstants.FrameworkBundle + " must not be set if "
						+ WebServiceConfigurationConstants.ClasspathScanning + " is set to true");
			}

			IAmbethConfiguration ambethConfiguration = null;
			if (scanClasspath)
			{
				ambethConfiguration = Ambeth.createDefault() //
						.withExtension(ServletConfiguratonExtension.class).withServletContext(servletContext);
			}
			else if (bundle != null)
			{
				@SuppressWarnings("unchecked")
				Class<IBundleModule> bundleClass = (Class<IBundleModule>) findClass(bundle);
				ambethConfiguration = Ambeth.createBundle(bundleClass) //
						.withExtension(ServletConfiguratonExtension.class).withServletContext(servletContext);
			}
			else
			{
				ambethConfiguration = Ambeth.createEmpty();
			}
			ambethConfiguration.withoutPropertiesFileSearch();

			String frameworkModules = properties.getString(WebServiceConfigurationConstants.FrameworkModules);
			String applicationModules = properties.getString(WebServiceConfigurationConstants.ApplicationModules);

			addModules(ambethConfiguration, frameworkModules, true);
			addModules(ambethConfiguration, applicationModules, false);

			ambethApp = ambethConfiguration.start();

			// store the instance of IServiceContext in servlet context
			event.getServletContext().setAttribute(ATTRIBUTE_I_SERVICE_CONTEXT, ambethApp.getApplicationContext());
			event.getServletContext().setAttribute(ATTRIBUTE_I_APPLICATION, ambethApp);

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
			if (ambethApp != null && ambethApp.getApplicationContext() != null)
			{
				IThreadLocalCleanupController threadLocalCleanupController = ambethApp.getApplicationContext().getService(IThreadLocalCleanupController.class);
				threadLocalCleanupController.cleanupThreadLocal();
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

			IAmbethApplication ambethApp = (IAmbethApplication) event.getServletContext().getAttribute(ATTRIBUTE_I_APPLICATION);
			event.getServletContext().removeAttribute(ATTRIBUTE_I_APPLICATION);

			// dispose the IServiceContext
			if (ambethApp != null)
			{
				try
				{
					ambethApp.close();
				}
				catch (IOException e)
				{
					log.error("Could not close ambeth application", e);
				}
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

	protected <T> T getProperty(ServletContext servletContext, Class<T> propertyType, String propertyName)
	{
		Object value = getService(servletContext, IProperties.class).get(propertyName);
		return getService(servletContext, IConversionHelper.class).convertValueToType(propertyType, value);
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

	private void addModules(IAmbethConfiguration ambethConfiguration, String modules, boolean framework)
	{
		if (modules == null)
		{
			return;
		}
		StringTokenizer st = new StringTokenizer(modules, ";");
		while (st.hasMoreTokens())
		{
			@SuppressWarnings("unchecked")
			Class<IInitializingModule> clazz = (Class<IInitializingModule>) findClass(st.nextToken());

			if (framework)
			{
				ambethConfiguration.withAmbethModules(clazz);
			}
			else
			{
				ambethConfiguration.withApplicationModules(clazz);
			}
		}
	}

	private Class<?> findClass(String fullQualifiedName)
	{
		try
		{
			ClassLoader cl = this.getClass().getClassLoader();
			Class<?> clazz = cl.loadClass(fullQualifiedName);
			return clazz;
		}
		catch (ClassNotFoundException e)
		{
			RuntimeExceptionUtil.mask(e);
		}
		return null;
	}
}
