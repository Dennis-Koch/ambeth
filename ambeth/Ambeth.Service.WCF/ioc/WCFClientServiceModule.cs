using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Service;
using System.Reflection;
using System.ServiceModel;
using De.Osthus.Ambeth.Connection;
#if !SILVERLIGHT
using Castle.DynamicProxy;
#else
using Castle.Core.Interceptor;
#endif
using De.Osthus.Ambeth.Security;
using De.Osthus.Ambeth.Service.Interceptor;
using De.Osthus.Ambeth.Log.Interceptor;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc.Factory;

namespace De.Osthus.Ambeth.Ioc
{
    public class WCFClientServiceModule : IInitializingModule
    {
        [Property(ServiceConfigurationConstants.NetworkClientMode, DefaultValue = "false")]
        public bool IsNetworkClientMode { get; set; }

        [Property(ServiceWCFConfigurationConstants.ClientServiceFactoryType, DefaultValue = "De.Osthus.Ambeth.Service.ClientServiceFactory")]
        public Type ClientServiceFactoryType { get; set; }

        [Property(ServiceConfigurationConstants.OfflineModeSupported, DefaultValue = "false")]
        public bool IsOfflineModeSupported { get; set; }

        protected ISet<Type> alreadyProcessedTypes = new HashSet<Type>();

        public virtual void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            ParamChecker.AssertNotNull(ClientServiceFactoryType, "ClientServiceFactoryType");

            if (IsNetworkClientMode)
            {
                if (IsOfflineModeSupported)
                {
                    //TODO
                    throw new NotSupportedException("Property '" + ServiceConfigurationConstants.OfflineModeSupported + "' with 'true' currently not supported");
                }
                if (!typeof(IClientServiceFactory).IsAssignableFrom(ClientServiceFactoryType))
                {
                    throw new ArgumentException("Type " + ClientServiceFactoryType.FullName + " must implement interface " + typeof(IClientServiceFactory).FullName);
                }
                beanContextFactory.RegisterBean("clientServiceFactory", ClientServiceFactoryType).Autowireable<IClientServiceFactory>();
                //CreateServices(beanContextFactory);
                //beanContextFactory.registerAutowireableBean<IServiceFactory, WCFServiceFactory>();
            }
        }

//        public virtual void CreateServices(IBeanContextFactory beanContextFactory)
//        {
//            AssemblyHelper.HandleAttributedTypesFromCurrentDomain<ServiceContractAttribute>(delegate(Type type, Object[] attributes)
//            {
//                if (type.IsInterface)
//                {
//                    Type clientType = type;
//                    if (type.Name.EndsWith("Service"))
//                    {
//                        String interfaceName = type.FullName;
//                        clientType = AssemblyHelper.GetTypeFromAssemblies(interfaceName.Substring(0, interfaceName.Length - 7) + "Client");
//                    }
//                    if (clientType != null && !clientType.Equals(type))
//                    {
//                        Object clientInterface = null;
//                        if (alreadyProcessedTypes.Add(clientType))
//                        {
//                            IInterceptor interceptor = BeanContext.CreateService<IInterceptor>(delegate(IConfigurableServiceProvider confSP)
//                            {
//                                Type typedWCFClientType = WCFClientType.MakeGenericType(clientType);

//                                confSP.registerAutowireableBean<ITargetProvider>(typedWCFClientType);
//                                TargetingInterceptor tInterceptor = confSP.registerAnonymousBean<TargetingInterceptor>();
//                                LogInterceptor lInterceptor = confSP.registerAutowireableBean<IInterceptor, LogInterceptor>();
//                                lInterceptor.Target = tInterceptor;
//                            });
//                            clientInterface = ProxyFactory.CreateProxy(clientType, interceptor);
//                            targetSP.RegisterService(clientType, clientInterface);
//#if SILVERLIGHT
//                            Object serviceInterface = targetSP.GetService(type);
//                            if (serviceInterface == null && clientType != type)
//                            {
//                                SyncCallInterceptor synchronizedInterceptor = targetSP.MonitorObject<SyncCallInterceptor>();
//                                synchronizedInterceptor.AsyncService = clientInterface;
//                                synchronizedInterceptor.AsyncServiceInterface = clientType;

//                                serviceInterface = ProxyFactory.CreateProxy(type, synchronizedInterceptor);
//                                ServiceExtendable.RegisterService(type, serviceInterface);
//                            }
//#endif
//                        }
//                    }
//#if !SILVERLIGHT
//                    if (alreadyProcessedTypes.Add(type))
//                    {
//                        IInterceptor interceptor = BeanContext.CreateService<IInterceptor>(delegate(IConfigurableServiceProvider confSP)
//                        {
//                            Type typedWCFClientType = WCFClientType.MakeGenericType(type);

//                            confSP.registerAutowireableBean<ITargetProvider>(typedWCFClientType);
//                            TargetingInterceptor tInterceptor = confSP.registerAnonymousBean<TargetingInterceptor>();
//                            LogInterceptor lInterceptor = confSP.registerAutowireableBean<IInterceptor, LogInterceptor>();
//                            lInterceptor.Target = tInterceptor;
//                        });
//                        Object serviceInterface = ProxyFactory.CreateProxy(type, interceptor);
//                        targetSP.RegisterService(type, serviceInterface);
//                    }
//#endif
//                }
//            });

//        }
    }
}
