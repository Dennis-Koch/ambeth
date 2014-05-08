package de.osthus.ambeth.ioc;

import de.osthus.ambeth.CacheDataChangeListener;
import de.osthus.ambeth.cache.ClearAllCachesEvent;
import de.osthus.ambeth.cache.RevertChangesHelper;
import de.osthus.ambeth.cache.RootCacheClearEventListener;
import de.osthus.ambeth.cache.ServiceResultCacheClearEventListener;
import de.osthus.ambeth.cache.ServiceResultCacheDCL;
import de.osthus.ambeth.datachange.DataChangeEventBatcher;
import de.osthus.ambeth.datachange.UnfilteredDataChangeListener;
import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.event.IEventBatcherExtendable;
import de.osthus.ambeth.event.IEventListenerExtendable;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.merge.IRevertChangesHelper;

@FrameworkModule
public class CacheDataChangeModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("revertChangesHelper", RevertChangesHelper.class).autowireable(IRevertChangesHelper.class);

		IBeanConfiguration serviceResultCacheClearEventListenerBC = beanContextFactory.registerAnonymousBean(ServiceResultCacheClearEventListener.class);
		beanContextFactory.link(serviceResultCacheClearEventListenerBC).to(IEventListenerExtendable.class).with(ClearAllCachesEvent.class);

		IBeanConfiguration rootCacheClearEventListenerBC = beanContextFactory.registerAnonymousBean(RootCacheClearEventListener.class).propertyRefs(
				CacheModule.COMMITTED_ROOT_CACHE);

		beanContextFactory.link(rootCacheClearEventListenerBC).to(IEventListenerExtendable.class).with(ClearAllCachesEvent.class);

		IBeanConfiguration serviceResultCacheDCL = beanContextFactory.registerAnonymousBean(UnfilteredDataChangeListener.class).propertyRef(
				beanContextFactory.registerAnonymousBean(ServiceResultCacheDCL.class));
		beanContextFactory.link(serviceResultCacheDCL).to(IEventListenerExtendable.class).with(IDataChange.class);

		beanContextFactory.registerBean(CacheModule.CACHE_DATA_CHANGE_LISTENER, CacheDataChangeListener.class);

		beanContextFactory.link(CacheModule.CACHE_DATA_CHANGE_LISTENER).to(IEventListenerExtendable.class).with(IDataChange.class);

		beanContextFactory.registerBean("dataChangeEventBatcher", DataChangeEventBatcher.class);
		beanContextFactory.link("dataChangeEventBatcher").to(IEventBatcherExtendable.class).with(IDataChange.class);
	}
}
