package com.koch.ambeth.cache.datachange.ioc;

import com.koch.ambeth.cache.datachange.CacheDataChangeListener;
import com.koch.ambeth.cache.datachange.DataChangeEventBatcher;
import com.koch.ambeth.cache.datachange.RevertChangesHelper;
import com.koch.ambeth.cache.datachange.RootCacheClearEventListener;
import com.koch.ambeth.cache.datachange.ServiceResultCacheDCL;
import com.koch.ambeth.cache.ioc.CacheModule;
import com.koch.ambeth.datachange.UnfilteredDataChangeListener;
import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.event.IEventBatcherExtendable;
import com.koch.ambeth.event.IEventListenerExtendable;
import com.koch.ambeth.event.IEventTargetListenerExtendable;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.IRevertChangesHelper;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;

@FrameworkModule
public class CacheDataChangeModule implements IInitializingModule
{
	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerBean("revertChangesHelper", RevertChangesHelper.class).autowireable(IRevertChangesHelper.class);

		IBeanConfiguration rootCacheClearEventListenerBC = beanContextFactory.registerBean(RootCacheClearEventListener.class).propertyRefs(
				CacheModule.COMMITTED_ROOT_CACHE);

		beanContextFactory.link(rootCacheClearEventListenerBC).to(IEventListenerExtendable.class).with(ClearAllCachesEvent.class);

		IBeanConfiguration serviceResultCacheDCL = beanContextFactory.registerBean(UnfilteredDataChangeListener.class).propertyRef(
				beanContextFactory.registerBean(ServiceResultCacheDCL.class));
		beanContextFactory.link(serviceResultCacheDCL).to(IEventListenerExtendable.class).with(IDataChange.class);

		beanContextFactory.registerBean(CacheModule.CACHE_DATA_CHANGE_LISTENER, CacheDataChangeListener.class);

		beanContextFactory.link(CacheModule.CACHE_DATA_CHANGE_LISTENER).to(IEventTargetListenerExtendable.class).with(IDataChange.class);

		beanContextFactory.registerBean("dataChangeEventBatcher", DataChangeEventBatcher.class);
		beanContextFactory.link("dataChangeEventBatcher").to(IEventBatcherExtendable.class).with(IDataChange.class);
	}
}
