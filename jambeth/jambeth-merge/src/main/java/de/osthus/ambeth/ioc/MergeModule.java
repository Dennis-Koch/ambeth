package de.osthus.ambeth.ioc;

import de.osthus.ambeth.cache.CacheModification;
import de.osthus.ambeth.cache.ICacheModification;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.merge.CUDResultHelper;
import de.osthus.ambeth.merge.EntityMetaDataClient;
import de.osthus.ambeth.merge.ICUDResultExtendable;
import de.osthus.ambeth.merge.ICUDResultHelper;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.merge.IEntityFactoryExtensionExtendable;
import de.osthus.ambeth.merge.IMergeController;
import de.osthus.ambeth.merge.IMergeProcess;
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.MergeController;
import de.osthus.ambeth.merge.MergeProcess;
import de.osthus.ambeth.merge.ORIHelper;
import de.osthus.ambeth.merge.config.EntityMetaDataReader;
import de.osthus.ambeth.merge.config.IEntityMetaDataReader;
import de.osthus.ambeth.merge.config.IndependentEntityMetaDataReader;
import de.osthus.ambeth.merge.config.MergeConfigurationConstants;
import de.osthus.ambeth.merge.converter.EntityMetaDataConverter;
import de.osthus.ambeth.merge.model.EntityMetaData;
import de.osthus.ambeth.merge.transfer.EntityMetaDataTransfer;
import de.osthus.ambeth.proxy.EntityFactory;
import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.template.CompositeIdTemplate;
import de.osthus.ambeth.util.DedicatedConverterUtil;

@FrameworkModule
public class MergeModule implements IInitializingModule
{
	public static final String MERGE_CACHE_FACTORY = "cacheFactory.merge";

	public static final String INDEPENDENT_META_DATA_READER = "independentEntityMetaDataReader";

	protected boolean independentMetaData;

	protected EntityMetaDataClient metaDataClient;

	protected Class<?> entityFactoryType;

	@Property(name = MergeConfigurationConstants.EntityFactoryType, mandatory = false)
	public void setEntityFactoryType(Class<?> entityFactoryType)
	{
		this.entityFactoryType = entityFactoryType;
	}

	@Property(name = ConfigurationConstants.IndependentMetaData, defaultValue = "false")
	public void setIndependentMetaData(boolean independentMetaData)
	{
		this.independentMetaData = independentMetaData;
	}

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerAutowireableBean(IMergeController.class, MergeController.class);
		beanContextFactory.registerAutowireableBean(IMergeProcess.class, MergeProcess.class).propertyRefs(MERGE_CACHE_FACTORY);

		beanContextFactory.registerAutowireableBean(CompositeIdTemplate.class, CompositeIdTemplate.class);

		// if (isNetworkClientMode)
		// {
		// metaDataClient =
		// serviceExtendable.registerObject(EntityMetaDataClient.class, new
		// Object[0]);
		//
		// EntityMetaDataCache metaDataCache =
		// serviceExtendable.registerService(IEntityMetaDataProvider.class,
		// EntityMetaDataCache.class);
		// metaDataCache.setEntityMetaDataProvider(metaDataClient);
		// }
		beanContextFactory.registerBean("cacheModification", CacheModification.class).autowireable(ICacheModification.class);

		beanContextFactory.registerAutowireableBean(IObjRefHelper.class, ORIHelper.class);
		beanContextFactory.registerBean("cudResultHelper", CUDResultHelper.class).autowireable(ICUDResultHelper.class, ICUDResultExtendable.class);

		beanContextFactory.registerBean("entityMetaDataReader", EntityMetaDataReader.class).autowireable(IEntityMetaDataReader.class);

		if (!independentMetaData)
		{
			beanContextFactory.registerBean("entityMetaDataConverter", EntityMetaDataConverter.class);
			DedicatedConverterUtil.biLink(beanContextFactory, "entityMetaDataConverter", EntityMetaData.class, EntityMetaDataTransfer.class);
		}
		else
		{
			beanContextFactory.registerBean(INDEPENDENT_META_DATA_READER, IndependentEntityMetaDataReader.class);
		}

		Class<?> entityFactoryType = this.entityFactoryType;
		if (entityFactoryType == null)
		{
			entityFactoryType = EntityFactory.class;
		}
		IBeanConfiguration entityFactoryBC = beanContextFactory.registerBean("entityFactory", entityFactoryType).autowireable(IEntityFactory.class);
		if (IEntityFactoryExtensionExtendable.class.isAssignableFrom(entityFactoryType))
		{
			entityFactoryBC.autowireable(IEntityFactoryExtensionExtendable.class);
		}

		// if (isNetworkClientMode)
		// {
		// if (!independentMetaData)
		// {
		// beanContextFactory.registerBean("entityMetaDataClient", EntityMetaDataClient.class);
		//
		// IBeanConfiguration beanConfig = beanContextFactory.registerBean("entityMetaDataCache", EntityMetaDataCache.class)
		// .propertyRefs("entityMetaDataClient").autowireable(IEntityMetaDataProvider.class);
		// if (genericTransferMapping)
		// {
		// beanConfig.autowireable(IEntityMetaDataProviderExtendable.class);
		// }
		// }
		// else
		// {
		// beanContextFactory.registerBean("indipendantMetaDataProvider", IndependentEntityMetaDataClient.class).autowireable(
		// IEntityMetaDataProvider.class, IEntityMetaDataProviderExtendable.class);
		// beanContextFactory.registerBean("entityMetaDataReader", EntityMetaDataReader.class);
		// }
		// }

		// serviceExtendable.registerObject(MergePostProcessor.class);

		// ITargetProvider targetProvider =
		// ServiceExtendable.RegisterObject<TypeInfoProviderTP>();

		// TargetingInterceptor tlInterceptor =
		// ServiceExtendable.RegisterObject<TargetingInterceptor>(targetProvider);

		// ServiceExtendable.RegisterService<ITypeInfoProvider>(ProxyFactory.CreateProxy<ITypeInfoProvider>(tlInterceptor));
	}

	// @Override
	// public void afterStarted(IServiceProvider serviceProvider)
	// {
	// // if (metaDataClient != null)
	// // {
	// //
	// metaDataClient.setMergeService(serviceProvider.getService(IServiceFactory.class).getService(
	// // IMergeService.class));
	// // }
	// }
}
