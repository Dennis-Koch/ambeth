package de.osthus.ambeth.ioc;

import javax.servlet.ServletContext;

import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.util.ClasspathScanner;
import de.osthus.ambeth.util.IClasspathScanner;
import de.osthus.ambeth.util.ModuleScanner;

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
