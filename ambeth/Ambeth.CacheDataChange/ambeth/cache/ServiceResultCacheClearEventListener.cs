using System;
using De.Osthus.Ambeth.Event;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Cache
{
    public class ServiceResultCacheClearEventListener : IEventListener, IInitializingBean
    {
	    [LogInstance]
		public ILogger Log { private get; set; }

        public IServiceResultCache ServiceResultCache { get; set; }

	    public void AfterPropertiesSet()
	    {
            ParamChecker.AssertNotNull(ServiceResultCache, "ServiceResultCache");
	    }

        public void HandleEvent(Object eventObject, DateTime dispatchTime, long sequenceId)
	    {
		    if (eventObject is ClearAllCachesEvent)
		    {
			    ServiceResultCache.InvalidateAll();
		    }
	    }
    }
}
