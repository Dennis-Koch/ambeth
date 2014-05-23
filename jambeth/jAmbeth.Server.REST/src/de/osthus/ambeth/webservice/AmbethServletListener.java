package de.osthus.ambeth.webservice;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.ws.rs.ext.Provider;

import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.ioc.BootstrapScannerModule;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LoggerFactory;
import de.osthus.ambeth.platform.IAmbethPlatformContext;
import de.osthus.ambeth.platform.IPlatformContextConfiguration;
import de.osthus.ambeth.platform.PlatformContextConfiguration;

@Provider
public class AmbethServletListener implements ServletContextListener
{
	private ILogger log;

	/**
	 * The name of the attribute in servlet context that holds an instance of IServiceContext
	 */
	public static final String ATTRIBUTE_I_SERVICE_CONTEXT = "IServiceContext";
	public static final String ATTRIBUTE_I_PLATFORM_CONTEXT = "PlatformContext";

	@Override
	public void contextInitialized(ServletContextEvent event)
	{
		final ServletContext servletContext = event.getServletContext();
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
			pcc.addProviderModule(new IInitializingModule()
			{
				@Override
				public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
				{
					beanContextFactory.registerExternalBean("servletContext", servletContext).autowireable(ServletContext.class);
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
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements())
		{
			Driver driver = drivers.nextElement();
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
		for (int a = 5; a-- > 0;)
		{
			System.gc();
			Thread.yield();
		}
		if (log.isInfoEnabled())
		{
			log.info("Shutdown completed");
		}
	}
}
