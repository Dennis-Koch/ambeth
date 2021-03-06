﻿using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Event;
using De.Osthus.Ambeth.Datachange;
using De.Osthus.Ambeth.Datachange.Model;
using System.Threading;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Ioc.Annotation;

namespace De.Osthus.Ambeth.Ioc
{
    [FrameworkModule]
    public class CacheDataChangeModule : IInitializingModule
    {        
	    public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
	    {
            beanContextFactory.RegisterBean<RevertChangesHelper>("revertChangesHelper").Autowireable<IRevertChangesHelper>();

	    	IBeanConfiguration rootCacheClearEventListenerBC = beanContextFactory.RegisterBean<RootCacheClearEventListener>().PropertyRefs(CacheModule.COMMITTED_ROOT_CACHE);
		    beanContextFactory.Link(rootCacheClearEventListenerBC).To<IEventListenerExtendable>().With(typeof(ClearAllCachesEvent));

		    IBeanConfiguration serviceResultCacheDCL = beanContextFactory.RegisterBean<UnfilteredDataChangeListener>().PropertyRef(
				    beanContextFactory.RegisterBean<ServiceResultCacheDCL>());
            beanContextFactory.Link(serviceResultCacheDCL).To<IEventListenerExtendable>().With(typeof(IDataChange));

		    IBeanConfiguration cacheDCListener = beanContextFactory.RegisterBean<CacheDataChangeListener>("cacheDataChangeListener");
            beanContextFactory.Link(cacheDCListener).To<IEventListenerExtendable>().With(typeof(IDataChange));

            beanContextFactory.RegisterBean<DataChangeEventBatcher>("dataChangeEventBatcher");
            beanContextFactory.Link("dataChangeEventBatcher").To<IEventBatcherExtendable>().With(typeof(IDataChange));
	    }
    }
}
