using De.Osthus.Ambeth.Cache.Config;
using De.Osthus.Ambeth.Cache.Model;
using De.Osthus.Ambeth.Cache.Transfer;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Security;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;
using System.Threading;

namespace De.Osthus.Ambeth.Cache
{
    public class ServiceResultCache : IServiceResultCache
    {
        [Autowired(Optional = true)]
        public ISecurityActivation SecurityActivation { protected get; set; }

        [Autowired(Optional = true)]
        public ISecurityManager SecurityManager { protected get; set; }

        [Autowired]
        public IServiceByNameProvider ServiceByNameProvider { protected get; set; }
        
        [Property(CacheConfigurationConstants.ServiceResultCacheActive, DefaultValue = "false")]
        public bool UseResultCache { protected get; set; }

        protected readonly IList<ISecurityScope> EMPTY_SCOPES = new List<ISecurityScope>(0);

        protected readonly HashMap<ServiceResultCacheKey, IServiceResult> serviceCallToResult = new HashMap<ServiceResultCacheKey, IServiceResult>();

        protected readonly CHashSet<ServiceResultCacheKey> serviceCallToPendingResult = new CHashSet<ServiceResultCacheKey>();

        protected ServiceResultCacheKey BuildKey(IServiceDescription serviceDescription)
        {
            Object service = ServiceByNameProvider.GetService(serviceDescription.ServiceName);

            ServiceResultCacheKey key = new ServiceResultCacheKey();
            key.Arguments = serviceDescription.Arguments;
            key.Method = serviceDescription.GetMethod(service.GetType());
            key.ServiceName = serviceDescription.ServiceName;
            if (key.Method == null || key.ServiceName == null)
            {
                throw new ArgumentException("ServiceDescription not legal " + serviceDescription);
            }
            return key;
        }

        public IServiceResult GetORIsOfService(IServiceDescription serviceDescription, ExecuteServiceDelegate executeServiceDelegate)
        {
            if (!UseResultCache)
            {
                return executeServiceDelegate.Invoke(serviceDescription);
            }
            ServiceResultCacheKey key = BuildKey(serviceDescription);
            IServiceResult serviceResult;
            lock (serviceCallToPendingResult)
            {
                serviceResult = serviceCallToResult.Get(key);
                if (serviceResult != null)
                {
                    return CreateServiceResult(serviceResult);
                }
                while (serviceCallToPendingResult.Contains(key))
                {
                    Monitor.Wait(serviceCallToPendingResult);
                }
                serviceResult = serviceCallToResult.Get(key);
                if (serviceResult != null)
                {
                    return CreateServiceResult(serviceResult);
                }
                serviceCallToPendingResult.Add(key);
            }
            bool success = false;
            try
            {
                if (SecurityActivation != null)
			    {
				    serviceResult = SecurityActivation.ExecuteWithoutFiltering(new IResultingBackgroundWorkerDelegate<IServiceResult>(delegate()
				    {
    				    return executeServiceDelegate.Invoke(serviceDescription);
				    }));
			    }
			    else
			    {
				    serviceResult = executeServiceDelegate.Invoke(serviceDescription);
			    }
                success = true;
            }
            finally
            {
                lock (serviceCallToPendingResult)
                {
                    serviceCallToPendingResult.Remove(key);

                    if (success)
                    {
                        serviceCallToResult.Put(key, serviceResult);
                    }
                    Monitor.PulseAll(serviceCallToPendingResult);
                }
            }
            return CreateServiceResult(serviceResult);
        }

        protected IServiceResult CreateServiceResult(IServiceResult cachedServiceResult)
	    {
            // Important to clone the ori list, because potential (user-dependent)
            // security logic may truncate this list (original must remain unmodified)
            IList<IObjRef> objRefs = cachedServiceResult.ObjRefs;
            IList<IObjRef> list = new List<IObjRef>(objRefs.Count);
            for (int a = 0, size = objRefs.Count; a < size; a++)
            {
                list.Add(objRefs[a]);
            }

            IList<IObjRef> filteredList;
            if (SecurityManager != null)
            {
                filteredList = SecurityManager.FilterValue(list);
            }
            else
            {
                filteredList = list;
            }
		    ServiceResult serviceResult = new ServiceResult();
		    serviceResult.AdditionalInformation = cachedServiceResult.AdditionalInformation;
            serviceResult.ObjRefs = filteredList;
		    return serviceResult;
	    }

        public void HandleClearAllCaches(ClearAllCachesEvent evnt)
        {
            InvalidateAll();
        }

        public void InvalidateAll()
        {
            lock (serviceCallToPendingResult)
            {
                serviceCallToResult.Clear();
            }
        }
    }
}
