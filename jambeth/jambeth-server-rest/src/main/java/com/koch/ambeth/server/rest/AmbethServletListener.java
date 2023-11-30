package com.koch.ambeth.server.rest;

import com.koch.ambeth.core.Ambeth;
import com.koch.ambeth.core.bundle.IBundleModule;
import com.koch.ambeth.core.start.IAmbethApplication;
import com.koch.ambeth.core.start.IAmbethConfiguration;
import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LoggerFactory;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.server.rest.config.WebServiceConfigurationConstants;
import com.koch.ambeth.server.start.ServletConfiguratonExtension;
import com.koch.ambeth.util.ClassLoaderUtil;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.config.IProperties;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import jakarta.ws.rs.ext.Provider;
import lombok.SneakyThrows;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.StringTokenizer;

@WebListener
@Provider
public class AmbethServletListener implements ServletContextListener, HttpSessionListener {
    /**
     * The name of the attribute in servlet context that holds an instance of IServiceContext
     */
    public static final String ATTRIBUTE_I_SERVICE_CONTEXT = "ambeth.IServiceContext";

    public static final String ATTRIBUTE_I_APPLICATION = "ambeth.Application";

    protected ServletContext servletContext;

    private ILogger log;

    @SneakyThrows
    @Override
    public void contextInitialized(ServletContextEvent event) {
        servletContext = event.getServletContext();
        Enumeration<String> initParameterNames = servletContext.getInitParameterNames();

        var properties = new Properties(Properties.getApplication());
        while (initParameterNames.hasMoreElements()) {
            var initParamName = initParameterNames.nextElement();
            var initParamValue = servletContext.getInitParameter(initParamName);
            properties.put(initParamName, initParamValue);
        }

        Properties.loadBootstrapPropertyFile(properties);
        log = LoggerFactory.getLogger(AmbethServletListener.class, properties);

        IAmbethApplication ambethApp = null;
        try {
            if (log.isInfoEnabled()) {
                log.info("Starting...");
            }

            var classpathScanning = properties.getString(WebServiceConfigurationConstants.ClasspathScanning, "true");
            boolean scanClasspath = (classpathScanning == null ? true : Boolean.parseBoolean(classpathScanning));

            var bundle = properties.getString(WebServiceConfigurationConstants.FrameworkBundle);
            if (scanClasspath && bundle != null) {
                throw new RuntimeException(WebServiceConfigurationConstants.FrameworkBundle + " must not be set if " + WebServiceConfigurationConstants.ClasspathScanning + " is set to true");
            }

            IAmbethConfiguration ambethConfiguration;
            if (scanClasspath) {
                ambethConfiguration =
                        Ambeth.createDefault().withExtension(ServletConfiguratonExtension.class).withServletContext(servletContext).withProperties(properties).withoutPropertiesFileSearch();
            } else if (bundle != null) {
                @SuppressWarnings("unchecked") Class<IBundleModule> bundleClass = (Class<IBundleModule>) findClass(bundle);
                ambethConfiguration =
                        Ambeth.createBundle(bundleClass).withExtension(ServletConfiguratonExtension.class).withServletContext(servletContext).withProperties(properties).withoutPropertiesFileSearch();
            } else {
                ambethConfiguration = Ambeth.createEmpty();
            }
            ambethConfiguration.withoutPropertiesFileSearch();

            var frameworkModules = properties.getString(WebServiceConfigurationConstants.FrameworkModules);
            var applicationModules = properties.getString(WebServiceConfigurationConstants.ApplicationModules);

            addModules(ambethConfiguration, frameworkModules, true);
            addModules(ambethConfiguration, applicationModules, false);

            ambethApp = ambethConfiguration.start();

            // store the instance of IServiceContext in servlet context
            event.getServletContext().setAttribute(ATTRIBUTE_I_SERVICE_CONTEXT, ambethApp.getApplicationContext());
            event.getServletContext().setAttribute(ATTRIBUTE_I_APPLICATION, ambethApp);

            if (log.isInfoEnabled()) {
                log.info("Start completed");
            }
        } catch (Throwable e) {
            if (log.isErrorEnabled()) {
                log.error(e);
            }
            throw e;
        } finally {
            if (ambethApp != null && ambethApp.getApplicationContext() != null) {
                IThreadLocalCleanupController threadLocalCleanupController = ambethApp.getApplicationContext().getService(IThreadLocalCleanupController.class);
                threadLocalCleanupController.cleanupThreadLocal();
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        log.info("Shutting down...");
        // remove the instance of IServiceContext in servlet context
        event.getServletContext().removeAttribute(ATTRIBUTE_I_SERVICE_CONTEXT);

        var ambethApp = (IAmbethApplication) event.getServletContext().getAttribute(ATTRIBUTE_I_APPLICATION);
        event.getServletContext().removeAttribute(ATTRIBUTE_I_APPLICATION);

        // dispose the IServiceContext
        if (ambethApp != null) {
            try {
                ambethApp.close();
            } catch (Throwable e) {
                log.error("Could not close ambeth application", e);
            }
        }
        var currentCL = Thread.currentThread().getContextClassLoader();
        var drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            var driver = drivers.nextElement();
            var driverCL = driver.getClass().getClassLoader();
            if (!ClassLoaderUtil.isParentOf(currentCL, driverCL)) {
                // this driver is not associated to the current CL
                continue;
            }
            try {
                DriverManager.deregisterDriver(driver);
            } catch (SQLException e) {
                if (log.isErrorEnabled()) {
                    log.error("Error deregistering driver " + driver, e);
                }
            }
        }
        if (log.isInfoEnabled()) {
            log.info("Shutdown completed");
        }
    }

    protected <T> T getProperty(ServletContext servletContext, Class<T> propertyType, String propertyName) {
        var value = getService(servletContext, IProperties.class).get(propertyName);
        return getService(servletContext, IConversionHelper.class).convertValueToType(propertyType, value);
    }

    protected <T> T getService(ServletContext servletContext, Class<T> serviceType) {
        return getServiceContext(servletContext).getService(serviceType);
    }

    protected <T> T getService(ServletContext servletContext, String beanName, Class<T> serviceType) {
        return getServiceContext(servletContext).getService(beanName, serviceType);
    }

    /**
     * @return The singleton IServiceContext which is stored in the context of the servlet
     */
    protected IServiceContext getServiceContext(ServletContext servletContext) {
        return (IServiceContext) servletContext.getAttribute(ATTRIBUTE_I_SERVICE_CONTEXT);
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        var beanContext = getServiceContext(servletContext);
        beanContext.getService(IEventDispatcher.class).dispatchEvent(se);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        var beanContext = getServiceContext(servletContext);
        beanContext.getService(IEventDispatcher.class).dispatchEvent(se);
    }

    // LOGIN:
    // sessionCreated => requestInitialized => authChangeActive => login => authorizationChanged => requestDestroyed
    //
    // LOGOUT
    // requestInitialized => authorizationChanged => sessionDestroyed => requestDestroyed => authorizationChanged

    private void addModules(IAmbethConfiguration ambethConfiguration, String modules, boolean framework) {
        if (modules == null) {
            return;
        }
        var st = new StringTokenizer(modules, ";");
        while (st.hasMoreTokens()) {
            @SuppressWarnings("unchecked") Class<IInitializingModule> clazz = (Class<IInitializingModule>) findClass(st.nextToken());

            if (framework) {
                ambethConfiguration.withFrameworkModules(clazz);
            } else {
                ambethConfiguration.withApplicationModules(clazz);
            }
        }
    }

    @SneakyThrows
    private Class<?> findClass(String fullQualifiedName) {
        var cl = getClass().getClassLoader();
        var clazz = cl.loadClass(fullQualifiedName);
        return clazz;
    }
}
