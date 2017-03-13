package com.koch.ambeth.xml.ioc;

import javax.servlet.ServletContext;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.util.IClasspathScanner;
import com.koch.ambeth.xml.util.ClasspathScanner;
import com.koch.ambeth.xml.util.ModuleScanner;

public class BootstrapScannerModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		IBeanConfiguration classpathScannerBC = beanContextFactory.registerBean(ClasspathScanner.class).autowireable(IClasspathScanner.class);

		beanContextFactory.registerBean(ModuleScanner.class);

		ServletContext servletContext = getServletContext();
		if (servletContext != null)
		{
			classpathScannerBC.propertyValue("ServletContext", servletContext);
		}
	}

	protected ServletContext getServletContext()
	{
		return null;
	}
}
