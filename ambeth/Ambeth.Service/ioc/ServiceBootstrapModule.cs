using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Ioc.Threadlocal;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Remote;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Xml;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Ioc
{
    [FrameworkModule]
    public class ServiceBootstrapModule : IInitializingBootstrapModule, IStartingModule
    {
        [LogInstance]
		public ILogger Log { private get; set; }

        [Property(ServiceConfigurationConstants.NetworkClientMode, DefaultValue = "false")]
        public bool IsNetworkClientMode { protected get; set; }

        [Property(ServiceConfigurationConstants.ProcessServiceBeanActive, DefaultValue = "true")]
        public virtual bool IsProcessServiceBeanActive { get; set; }

        [Property(ServiceConfigurationConstants.OfflineModeSupported, DefaultValue = "false")]
        public bool IsOfflineModeSupported { protected get; set; }

        [Property(ServiceConfigurationConstants.TypeInfoProviderType, Mandatory = false)]
        public Type TypeInfoProviderType { protected get; set; }

        [Property(ServiceConfigurationConstants.ServiceRemoteInterceptorType, Mandatory = false)]
        public Type ServiceRemoteInterceptorType { protected get; set; }

        public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            if (TypeInfoProviderType == null)
            {
                TypeInfoProviderType = typeof(TypeInfoProvider);
            }
            if (ServiceRemoteInterceptorType == null)
            {
                ServiceRemoteInterceptorType = typeof(SyncClientServiceInterceptorBuilder);
            }
            beanContextFactory.RegisterBean("clientServiceInterceptorBuilder", ServiceRemoteInterceptorType).Autowireable<IClientServiceInterceptorBuilder>();

            if (IsNetworkClientMode)
            {
                beanContextFactory.RegisterBean<ServiceFactory>("serviceFactory");

                if (!IsOfflineModeSupported)
                {
                    // Register default service url provider
                    beanContextFactory.RegisterBean<DefaultServiceUrlProvider>("serviceUrlProvider").Autowireable(typeof(IServiceUrlProvider), typeof(IOfflineListenerExtendable));
                }
            }
            else if (!IsOfflineModeSupported)
            {
                beanContextFactory.RegisterAnonymousBean<NoOpOfflineExtendable>().Autowireable<IOfflineListenerExtendable>();
            }

            beanContextFactory.RegisterBean("serviceByNameProvider", typeof(ServiceByNameProvider)).Autowireable(
                typeof(IServiceByNameProvider), typeof(IServiceExtendable));

            beanContextFactory.RegisterBean<ServiceResultProcessorRegistry>("serviceResultProcessorRegistry").Autowireable(
                typeof(IServiceResultProcessorRegistry), typeof(IServiceResultProcessorExtendable));

       		beanContextFactory.RegisterBean("typeInfoProvider", TypeInfoProviderType).Autowireable<ITypeInfoProvider>();

    		beanContextFactory.RegisterBean<TypeInfoProviderFactory>("typeInfoProviderFactory").PropertyValue("TypeInfoProviderType", TypeInfoProviderType)
				.Autowireable<ITypeInfoProviderFactory>();

            beanContextFactory.RegisterBean<LoggingPostProcessor>("loggingPostProcessor");
            
            if (IsNetworkClientMode && IsProcessServiceBeanActive)
            {
                beanContextFactory.RegisterBean<ClientServiceBean>("processServiceWCF")
                        .PropertyValue("Interface", typeof(IProcessService))
                        .PropertyValue("SyncRemoteInterface", typeof(IProcessServiceWCF))
                        .PropertyValue("AsyncRemoteInterface", typeof(IProcessClient))
                        .Autowireable<IProcessService>();
            }
       		beanContextFactory.RegisterBean<XmlTypeHelper>("xmlTypeHelper").Autowireable<IXmlTypeHelper>();
        }

        public virtual void AfterStarted(IServiceContext beanContext)
        {
            if (Log.InfoEnabled)
            {
                IEnumerable<Type> types = FullServiceModelProvider.RegisterKnownTypes(null);

#if !SILVERLIGHT
                SortedList<String, String> sortedTypes = new SortedList<String, String>();

                SortedList<String, String> sortedListTypes = new SortedList<String, String>();

                foreach (Type type in types)
                {
                    String name = LogTypesUtil.PrintType(type, true);
                    if (type.IsGenericType)
                    {
                        sortedListTypes.Add(name, name);
                    }
                    else
                    {
                        sortedTypes.Add(name, name);
                    }
                }
                Log.Info(sortedTypes.Count + " data types");
                Log.Info(sortedListTypes.Count + " collection types");
                DictionaryExtension.Loop(sortedTypes, delegate(String key, String value)
                {
                    Log.Info("Type: " + value);
                });
                DictionaryExtension.Loop(sortedListTypes, delegate(String key, String value)
                {
                    Log.Info("Type: " + value);
                });
#else
                List<String> sortedTypes = new List<String>();

                List<String> sortedListTypes = new List<String>();

                foreach (Type type in types)
                {
                    String name = LogTypesUtil.PrintType(type, true);
                    List<String> list;
                    if (type.IsGenericType)
                    {
                        list = sortedListTypes;
                    }
                    else
                    {
                        list = sortedTypes;
                    }
                    bool inserted = false;
                    for (int a = list.Count; a-- > 0; )
                    {
                        String item = list[a];
                        if (item.CompareTo(name) < 0)
                        {
                            list.Insert(a + 1, name);
                            inserted = true;
                            break;
                        }
                    }
                    if (!inserted)
                    {
                        list.Insert(0, name);
                    }
                }
                Log.Info(sortedTypes.Count + " data types");
                Log.Info(sortedListTypes.Count + " collection types");
                foreach (String value in sortedTypes)
                {
                    Log.Info("Type: " + value);
                }
                foreach (String value in sortedListTypes)
                {
                    Log.Info("Type: " + value);
                }
#endif

            }
        }
    }
}
