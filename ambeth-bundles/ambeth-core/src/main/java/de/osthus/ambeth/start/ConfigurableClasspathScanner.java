package de.osthus.ambeth.start;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.config.CoreConfigurationConstants;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate;
import de.osthus.ambeth.util.IClasspathScanner;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.IDisposable;

public class ConfigurableClasspathScanner implements IClasspathScanner, IInitializingBean, IDisposable, Closeable
{
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext serviceContext;

	@Autowired
	protected IProperties properties;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Property
	protected HashMap<Class<?>, Object> autowiredInstances;

	protected IServiceContext scannerContext;

	protected IClasspathScanner classpathScanner;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		final Class<?> classpathScannerClass = getConfiguredType(CoreConfigurationConstants.ClasspathScannerClass, CoreClasspathScanner.class.getName());
		final Class<?> classpathInfoClass = getConfiguredType(CoreConfigurationConstants.ClasspathInfoClass, SystemClasspathInfo.class.getName());

		scannerContext = serviceContext.createService(new IBackgroundWorkerParamDelegate<IBeanContextFactory>()
		{

			@Override
			public void invoke(IBeanContextFactory beanContextFactory) throws Throwable
			{
				beanContextFactory.registerBean(classpathScannerClass).autowireable(IClasspathScanner.class);
				beanContextFactory.registerBean(classpathInfoClass).autowireable(IClasspathInfo.class);

				for (Entry<Class<?>, Object> autowiring : autowiredInstances)
				{
					Class<?> typeToPublish = autowiring.getKey();
					Object externalBean = autowiring.getValue();
					beanContextFactory.registerExternalBean(externalBean).autowireable(typeToPublish);
				}
			}
		});
		classpathScanner = scannerContext.getService(IClasspathScanner.class);
	}

	@Override
	public void dispose()
	{
		if (scannerContext != null)
		{
			scannerContext.dispose();

			scannerContext = null;
			classpathScanner = null;
		}
	}

	@Override
	public void close() throws IOException
	{
		dispose();
	}

	@Override
	public List<Class<?>> scanClassesAnnotatedWith(Class<?>... annotationTypes)
	{
		List<Class<?>> annotatedWith = classpathScanner.scanClassesAnnotatedWith(annotationTypes);
		return annotatedWith;
	}

	@Override
	public List<Class<?>> scanClassesImplementing(Class<?>... superTypes)
	{
		List<Class<?>> implementing = classpathScanner.scanClassesImplementing(superTypes);
		return implementing;
	}

	protected Class<?> getConfiguredType(String propertyName, String defaulValue)
	{
		String className = properties.getString(propertyName, defaulValue);
		Class<?> clazz = conversionHelper.convertValueToType(Class.class, className);

		return clazz;
	}
}
