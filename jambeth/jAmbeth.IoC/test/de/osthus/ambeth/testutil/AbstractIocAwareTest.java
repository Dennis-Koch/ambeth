package de.osthus.ambeth.testutil;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;

import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.config.UtilConfigurationConstants;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.factory.BeanContextFactory;
import de.osthus.ambeth.log.Logger;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.util.ModuleUtil;

/**
 * Deprecated: Please use <src>de.osthus.ambeth.test.AbstractIocTest</src> instead.
 */
@Deprecated
public class AbstractIocAwareTest
{
	public static IServiceContext bootstrapContext;

	public static IServiceContext beanContext;

	private static final List<IPropertiesProvider> propProviders = new ArrayList<IPropertiesProvider>();

	private static final List<IPropertiesProvider> finalPropProviders = new ArrayList<IPropertiesProvider>();

	protected static void addPropertiesProvider(IPropertiesProvider propertiesProvider)
	{
		propProviders.add(propertiesProvider);
	}

	protected static void addFinalPropertiesProvider(IPropertiesProvider propertiesProvider)
	{
		finalPropProviders.add(propertiesProvider);
	}

	protected static void setUpBeforeClassIoc(Class<?>[] moduleTypes) throws Exception
	{
		setUpBeforeClassIoc(moduleTypes, null);
	}

	protected static void setUpBeforeClassIoc(Class<?>[] moduleTypes, Class<?>[] childModuleTypes) throws Exception
	{
		if (bootstrapContext != null)
		{
			throw new IllegalStateException();
		}
		Properties.resetApplication();
		Properties baseProps = Properties.getApplication();

		if (propProviders.size() > 0)
		{
			// Providers are registered in inverse order as they have to be processed
			for (int a = propProviders.size(); a-- > 0;)
			{
				IPropertiesProvider propProvider = propProviders.get(a);
				propProvider.fillProperties(baseProps);
			}
		}
		// Overwrite all properties again with the external loaded property file
		String bootstrapPropertyFile = Properties.getSystem().getString(UtilConfigurationConstants.BootstrapPropertyFile);
		if (bootstrapPropertyFile != null)
		{
			baseProps.load(bootstrapPropertyFile);
		}
		if (finalPropProviders.size() > 0)
		{
			// Providers are registered in inverse order as they have to be processed
			for (int a = finalPropProviders.size(); a-- > 0;)
			{
				IPropertiesProvider propProvider = finalPropProviders.get(a);
				propProvider.fillProperties(baseProps);
			}
		}
		bootstrapContext = BeanContextFactory.createBootstrap(baseProps);
		if (Logger.objectCollector == null)
		{
			Logger.objectCollector = bootstrapContext.getService(IThreadLocalObjectCollector.class);
		}
		beanContext = bootstrapContext.createService(moduleTypes);
		if (childModuleTypes != null && childModuleTypes.length > 0)
		{
			beanContext = beanContext.createService(childModuleTypes);
		}
	}

	@AfterClass
	public static void tearDownAfterClassIoc() throws Exception
	{
		try
		{
			Logger.objectCollector = null;
			if (beanContext != null)
			{
				beanContext.dispose();
				beanContext = null;
			}
		}
		finally
		{
			try
			{
				if (bootstrapContext != null)
				{
					bootstrapContext.dispose();
					bootstrapContext = null;
				}
			}
			finally
			{
				propProviders.clear();
			}
		}
	}

	protected static Class<?>[] mergeModules(Class<?>[] leftModules, Class<?>[] rightModules)
	{
		return ModuleUtil.mergeModules(leftModules, rightModules);
	}

}
