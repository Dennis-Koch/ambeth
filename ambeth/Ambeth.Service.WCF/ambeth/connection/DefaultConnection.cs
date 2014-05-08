using System;
using System.Net;
using System.ServiceModel;
using System.ServiceModel.Channels;
using System.ServiceModel.Description;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Log;
using System.Text.RegularExpressions;
using System.ComponentModel;
using De.Osthus.Ambeth.Security;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Log.Interceptor;
using De.Osthus.Ambeth.Proxy;
using System.Runtime.Remoting;
using System.Runtime.Remoting.Messaging;
using System.Collections.Generic;
using De.Osthus.Ambeth.Ioc;
#if !SILVERLIGHT
using Castle.DynamicProxy;
#else
using Castle.Core.Interceptor;
#endif
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Connection
{
    //public class DefaultConnection<WCF_I, I> : DefaultConnection where WCF_I : class
    //{
    //    private static ILogger LOG = LoggerFactory.GetLogger(typeof(DefaultConnection));

    //    public WCF_I ChannelInstance
    //    {
    //        get
    //        {
    //            return (WCF_I)channelInstance;
    //        }
    //        set
    //        {
    //            channelInstance = value;
    //        }
    //    }

    //    public DefaultConnection() : base()
    //    {
    //        ServiceName = WCFClientTargetProvider<WCF_I>.GetServiceName(typeof(I).Name);
    //    }

    //    public I CreateClient(params ISecurityScope[] securityScopes)
    //    {
    //        return CreateClient<WCF_I, I>(ChannelInstance, securityScopes);
    //    }

    //    public override I CreateClient<I>(params ISecurityScope[] securityScopes)
    //    {
    //        return CreateClient<WCF_I, I>(ChannelInstance, securityScopes);
    //    }
    //}

    //public class DefaultConnection
    //{
    //    private static ILogger LOG = LoggerFactory.GetLogger(typeof(DefaultConnection));

    //    public static Regex regex = new Regex(".*/([^/]+)");
        
    //    public IServiceContext BeanContext { get; set; }

    //    public bool SecurityActive { get; set; }

    //    public bool MergeActive { get; set; }

    //    public bool CacheActive { get; set; }

    //    public bool LogActive { get; set; }
        
    //    public String ServiceName { get; set; }

    //    protected Object channelInstance;
        
    //    public DefaultConnection()
    //    {
    //        SecurityActive = true;
    //        MergeActive = false;
    //        CacheActive = true;
    //        LogActive = true;
    //    }

    //    public virtual I CreateClient<I>(params ISecurityScope[] securityScopes)
    //    {
    //        throw new NotSupportedException();
    //    }

    //    protected I CreateClient<WCF_I, I>(WCF_I client, params ISecurityScope[] securityScopes)
    //    {                        
    //        Object interceptor = client;

    //        if (SecurityActive)
    //        {
    //            Type ssInterceptorType = AssemblyHelper.GetTypeFromAssemblies("De.Osthus.Ambeth.Security.Interceptor.SecurityScopeInterceptor");
    //            if (ssInterceptorType != null)
    //            {
    //                IInterceptor ssInterceptor = BeanContext.RegisterAnonymousBean(ssInterceptorType);

    //                ssInterceptorType.GetProperty("ServiceName").SetValue(ssInterceptor, ServiceName, null);
    //                ssInterceptorType.GetProperty("SecurityScopes").SetValue(ssInterceptor, securityScopes, null);
    //                interceptor = ssInterceptor;
    //            }
    //        }
    //        if (CacheActive)
    //        {
    //            Type cacheInterceptorType = AssemblyHelper.GetTypeFromAssemblies("De.Osthus.Ambeth.Cache.Interceptor.CacheInterceptor");
    //            if (cacheInterceptorType != null)
    //            {
    //                ICascadedInterceptor cInterceptor = BeanContext.MonitorObject<ICascadedInterceptor>(cacheInterceptorType);
    //                cacheInterceptorType.GetProperty("ServiceName").SetValue(cInterceptor, ServiceName, null);
    //                interceptor = ProxyFactory.Wrap(interceptor, cInterceptor);
    //            }
    //        }
    //        else if (MergeActive)
    //        {
    //            Type mergeInterceptorType = AssemblyHelper.GetTypeFromAssemblies("De.Osthus.Ambeth.Merge.Interceptor.MergeInterceptor");
    //            if (mergeInterceptorType != null)
    //            {
    //                ICascadedInterceptor mInterceptor = BeanContext.MonitorObject<ICascadedInterceptor>(mergeInterceptorType);
    //                interceptor = ProxyFactory.Wrap(interceptor, mInterceptor);
    //            }
    //        }
    //        //if (LogActive)
    //        //{
    //        //    interceptor = ProxyFactory.Wrap(interceptor, LogInterceptor.Create(true));
    //        //}
    //        if (interceptor is I)
    //        {
    //            return (I)interceptor;
    //        }
    //        if (!typeof(WCF_I).IsAssignableFrom(typeof(I)))
    //        {
    //            interceptor = ProxyFactory.Wrap(interceptor, WCFWrapInterceptor.Create(typeof(WCF_I)));
    //        }
    //        return ProxyFactory.CreateProxy<I>((IInterceptor)interceptor);
    //    }
    //}
}
