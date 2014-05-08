using System;
using System.Collections.Generic;
using System.Threading;
using De.Osthus.Ambeth.Cache.Config;
using De.Osthus.Ambeth.Cache.Model;
using De.Osthus.Ambeth.Cache.Transfer;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Service;

namespace De.Osthus.Ambeth.Cache
{
    public class ServiceResultCache : IServiceResultCache, IInitializingBean
    {
        protected readonly IList<ISecurityScope> EMPTY_SCOPES = new List<ISecurityScope>(0);

        protected readonly IDictionary<ServiceResultCacheKey, IServiceResult> serviceCallToResult = new Dictionary<ServiceResultCacheKey, IServiceResult>();

        protected readonly ISet<ServiceResultCacheKey> serviceCallToPendingResult = new HashSet<ServiceResultCacheKey>();

        [Property(CacheConfigurationConstants.ServiceResultCacheActive, DefaultValue = "false")]
        public bool UseResultCache { protected get; set; }

        public IServiceByNameProvider ServiceByNameProvider { protected get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(ServiceByNameProvider,"ServiceByNameProvider");
        }

        protected virtual ServiceResultCacheKey BuildKey(IServiceDescription serviceDescription)
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

        public virtual IServiceResult GetORIsOfService(IServiceDescription serviceDescription, ExecuteServiceDelegate executeServiceDelegate)
        {
            if (!UseResultCache)
            {
                return executeServiceDelegate.Invoke(serviceDescription);
            }
            ServiceResultCacheKey key = BuildKey(serviceDescription);
            IServiceResult serviceResult;
            lock (serviceCallToPendingResult)
            {
                serviceResult = DictionaryExtension.ValueOrDefault(serviceCallToResult, key);
                if (serviceResult != null)
                {
                    return CreateServiceResult(serviceResult);
                }
                while (serviceCallToPendingResult.Contains(key))
                {
                    Monitor.Wait(serviceCallToPendingResult);
                }
                serviceResult = DictionaryExtension.ValueOrDefault(serviceCallToResult, key);
                if (serviceResult != null)
                {
                    return CreateServiceResult(serviceResult);
                }
                serviceCallToPendingResult.Add(key);
            }
            bool success = false;
            try
            {
                serviceResult = executeServiceDelegate.Invoke(serviceDescription);
                success = true;
            }
            finally
            {
                lock (serviceCallToPendingResult)
                {
                    serviceCallToPendingResult.Remove(key);

                    if (success)
                    {
                        serviceCallToResult.Remove(key);
                        serviceCallToResult.Add(key, serviceResult);
                    }
                    Monitor.PulseAll(serviceCallToPendingResult);
                }
            }
            return CreateServiceResult(serviceResult);
        }

        protected IServiceResult CreateServiceResult(IServiceResult cachedServiceResult)
	    {
		    // Important to clone the ori list, because potential (user-dependent)
		    // security logic may truncate this list
            IList<IObjRef> objRefs = cachedServiceResult.ObjRefs;
            IList<IObjRef> list = new List<IObjRef>(objRefs.Count);
            for (int a = 0, size = objRefs.Count; a < size; a++)
            {
                list.Add(objRefs[a]);
            }
		    ServiceResult serviceResult = new ServiceResult();
		    serviceResult.AdditionalInformation = cachedServiceResult.AdditionalInformation;
		    serviceResult.ObjRefs = list;
		    return serviceResult;
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
