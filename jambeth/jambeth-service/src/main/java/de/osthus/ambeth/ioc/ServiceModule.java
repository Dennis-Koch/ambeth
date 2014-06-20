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
import de.osthus.ambeth.typeinfo.ITypeInfoProvider;
import de.osthus.ambeth.typeinfo.ITypeInfoProviderFactory;
import de.osthus.ambeth.typeinfo.TypeInfoProvider;
import de.osthus.ambeth.typeinfo.TypeInfoProviderFactory;
import de.osthus.ambeth.xml.IXmlTypeHelper;
import de.osthus.ambeth.xml.XmlTypeHelper;

@FrameworkModule
public class ServiceModule implements IInitializingModule
{
	@Property(name = ServiceConfigurationConstants.NetworkClientMode, defaultValue = "false")
	protected boolean networkClientMode;

	@Property(name = ServiceConfigurationConstants.OfflineModeSupported, defaultValue = "false")
	protected boolean offlineModeSupported;

	@Property(name = ServiceConfigurationConstants.TypeInfoProviderType, mandatory = false)
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
}
