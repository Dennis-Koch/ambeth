using System;
using De.Osthus.Ambeth.Security;
using De.Osthus.Ambeth.Connection;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Service
{
    //public class WCFServiceFactoryBean : IFactoryBean, IInitializingBean
    //{
    //    public static readonly Type DefaultConnectionBaseType = typeof(DefaultConnection<Object, Object>).GetGenericTypeDefinition();

    //    public DefaultConnection DefaultConnection { get; set; }

    //    public IServiceContext BeanContext { get; set; }

    //    public virtual void AfterPropertiesSet()
    //    {
    //        ParamChecker.AssertNotNull(DefaultConnection, "DefaultConnection");
    //        ParamChecker.AssertNotNull(BeanContext, "BeanContext");
    //    }

    //    public virtual Object GetObject()
    //    {

    //    }

    //    public C GetService<C>(params ISecurityScope[] securityScopes)
    //        where C : class
    //    {
    //        //TODO implement caching feature to loose network dependencies (handshaking new connection etc.)

    //        Type type = typeof(C);

    //        DefaultConnection conn = null;

    //        Type syncServiceType = AssemblyHelper.GetTypeFromAssemblies("De.Osthus.Ambeth.Service.ISyncService");
    //        Type syncClientType = AssemblyHelper.GetTypeFromAssemblies("De.Osthus.Ambeth.Service.ISyncClient");
    //        if (type.Equals(syncServiceType) || type.Equals(syncClientType))
    //        {
    //            Type syncServiceWcfType = AssemblyHelper.GetTypeFromAssemblies("De.Osthus.Ambeth.Service.ISyncServiceWCF");

    //            Type defaultConnectionType = DefaultConnectionBaseType.MakeGenericType(syncServiceWcfType, type);
    //            conn = BeanContext.MonitorObject<DefaultConnection>(defaultConnectionType);

    //            conn.MergeActive = false;
    //            conn.SecurityActive = false;
    //            conn.CacheActive = false;
    //            conn.LogActive = true;
    //        }

    //        if (conn == null)
    //        {
    //            Type defaultConnectionType = DefaultConnectionBaseType.MakeGenericType(type, type);
    //            conn = BeanContext.MonitorObject<DefaultConnection>(defaultConnectionType);
    //        }
    //        Type eventServiceType = AssemblyHelper.GetTypeFromAssemblies("De.Osthus.Ambeth.Service.IEventService");
    //        Type eventClientType = AssemblyHelper.GetTypeFromAssemblies("De.Osthus.Ambeth.Service.IEventClient");
    //        Type cacheServiceType = AssemblyHelper.GetTypeFromAssemblies("De.Osthus.Ambeth.Service.ICacheService");
    //        Type cacheClientType = AssemblyHelper.GetTypeFromAssemblies("De.Osthus.Ambeth.Service.ICacheClient");
    //        Type mergeServiceType = AssemblyHelper.GetTypeFromAssemblies("De.Osthus.Ambeth.Service.IMergeService");
    //        Type mergeClientType = AssemblyHelper.GetTypeFromAssemblies("De.Osthus.Ambeth.Service.IMergeClient");
    //        if (type.Equals(eventServiceType) || type.Equals(eventClientType)
    //            || type.Equals(cacheServiceType) || type.Equals(cacheClientType)
    //            || type.Equals(mergeServiceType) || type.Equals(mergeClientType))
    //        {
    //            conn.CacheActive = false;
    //            conn.MergeActive = false;
    //            conn.SecurityActive = false;
    //        }

    //        Type logServiceType = AssemblyHelper.GetTypeFromAssemblies("De.Osthus.Ambeth.Service.ILogService");
    //        Type logClientType = AssemblyHelper.GetTypeFromAssemblies("De.Osthus.Ambeth.Service.ILogClient");
    //        if (type.Equals(logServiceType) || type.Equals(logClientType))
    //        {
    //            conn.MergeActive = false;
    //            conn.SecurityActive = false;
    //            conn.CacheActive = false;
    //            conn.LogActive = false;
    //        }
    //        return conn.CreateClient<C>(securityScopes);
    //    }
    //}
}
