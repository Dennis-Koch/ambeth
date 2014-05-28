package de.osthus.ambeth.ioc;

import de.osthus.ambeth.cache.IServiceResultProcessorExtendable;
import de.osthus.ambeth.cache.IServiceResultProcessorRegistry;
import de.osthus.ambeth.cache.ServiceResultProcessorRegistry;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.LoggingPostProcessor;
import de.osthus.ambeth.service.DefaultServiceUrlProvider;
import de.osthus.ambeth.service.IOfflineListenerExtendable;
import de.osthus.ambeth.service.IProcessService;
import de.osthus.ambeth.service.IServiceByNameProvider;
import de.osthus.ambeth.service.IServiceExtendable;
import de.osthus.ambeth.service.IServiceUrlProvider;
import de.osthus.ambeth.service.NoOpOfflineExtendable;
import de.osthus.ambeth.service.ProcessService;
import de.osthus.ambeth.service.ServiceByNameProvider;
import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.typeinfo.ITypeInfoProvider;
import de.osthus.ambeth.typeinfo.ITypeInfoProviderFactory;
import de.osthus.ambeth.typeinfo.TypeInfoProvider;
import de.osthus.ambeth.typeinfo.TypeInfoProviderFactory;
import de.osthus.ambeth.xml.IXmlTypeHelper;
import de.osthus.ambeth.xml.XmlTypeHelper;

@FrameworkModule
public class ServiceModule implements IInitializingModule
{
	protected boolean networkClientMode;

	protected boolean offlineModeSupported;

	protected Class<?> typeInfoProviderType;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		if (typeInfoProviderType == null)
		{
			typeInfoProviderType = TypeInfoProvider.class;
		}
		if (networkClientMode)
		{
			// beanContextFactory.registerBean("serviceFactory", ServiceFactory.class);

			// beanContextFactory.registerBean<AsyncClientServiceInterceptorBuilder>("clientServiceInterceptorBuilder").autowireable<IClientServiceInterceptorBuilder>();
			// beanContextFactory.registerBean<SyncClientServiceInterceptorBuilder>"clientServiceInterceptorBuilder".autowireable<IClientServiceInterceptorBuilder>();

			if (!offlineModeSupported)
			{
				// Register default service url provider
				beanContextFactory.registerBean("serviceUrlProvider", DefaultServiceUrlProvider.class).autowireable(IServiceUrlProvider.class,
						IOfflineListenerExtendable.class);
			}
		}
		else if (!offlineModeSupported)
		{
			beanContextFactory.registerAnonymousBean(NoOpOfflineExtendable.class).autowireable(IOfflineListenerExtendable.class);
		}
		beanContextFactory.registerBean("serviceByNameProvider", ServiceByNameProvider.class).propertyValue("ParentServiceByNameProvider", null)
				.autowireable(IServiceByNameProvider.class, IServiceExtendable.class);

		beanContextFactory.registerBean("serviceResultProcessorRegistry", ServiceResultProcessorRegistry.class).autowireable(
				IServiceResultProcessorRegistry.class, IServiceResultProcessorExtendable.class);

		beanContextFactory.registerBean("typeInfoProvider", typeInfoProviderType).autowireable(ITypeInfoProvider.class);

		beanContextFactory.registerBean("typeInfoProviderFactory", TypeInfoProviderFactory.class).propertyValue("TypeInfoProviderType", typeInfoProviderType)
				.autowireable(ITypeInfoProviderFactory.class);

		beanContextFactory.registerBean("loggingPostProcessor", LoggingPostProcessor.class);

		beanContextFactory.registerBean("processService", ProcessService.class).autowireable(IProcessService.class);

		beanContextFactory.registerBean("xmlTypeHelper", XmlTypeHelper.class).autowireable(IXmlTypeHelper.class);
	}

	@Property(name = ConfigurationConstants.NetworkClientMode, defaultValue = "false")
	public void setNetworkClientMode(boolean networkClientMode)
	{
		this.networkClientMode = networkClientMode;
	}

	@Property(name = ConfigurationConstants.OfflineModeSupported, defaultValue = "false")
	public void setOfflineModeSupported(boolean offlineModeSupported)
	{
		this.offlineModeSupported = offlineModeSupported;
	}

	@Property(name = ServiceConfigurationConstants.TypeInfoProviderType, mandatory = false)
	public void setTypeInfoProviderType(Class<?> typeInfoProviderType)
	{
		this.typeInfoProviderType = typeInfoProviderType;
	}
}
