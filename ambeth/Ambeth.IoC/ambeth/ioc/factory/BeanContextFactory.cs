using System;
using System.Collections.Generic;
using System.Text;
using System.Threading;
using De.Osthus.Ambeth.Accessor;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Ioc.Link;
using De.Osthus.Ambeth.Ioc.Proxy;
using De.Osthus.Ambeth.Ioc.Threadlocal;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Garbageproxy;

namespace De.Osthus.Ambeth.Ioc.Factory
{
    public class BeanContextFactory : IBeanContextFactory, ILinkController, IDisposable
    {
        public static readonly Object[] emptyArgs = new Object[0];

        public static readonly Type[] emptyServiceModules = new Type[0];

        public static readonly ThreadLocal<IDictionary<Object, IBeanConfiguration>> pendingConfigurationMapTL = new ThreadLocal<IDictionary<Object, IBeanConfiguration>>();

        public static IServiceContext CreateBootstrap(params Type[] bootstrapModules)
        {
            return CreateBootstrap(null, bootstrapModules);
        }

        public static IServiceContext CreateBootstrap(IProperties properties, params Type[] bootstrapModules)
        {
            return CreateBootstrap(properties, bootstrapModules, emptyArgs);
        }

        public static IServiceContext CreateBootstrap(IProperties properties, Type[] bootstrapModules, params Object[] bootstrapModuleInstances)
        {
            if (properties == null)
            {
                properties = Properties.Application;
            }
            // create own sub-instance of properties
            Properties newProps = new Properties(properties);

            ThreadLocalCleanupController threadLocalCleanupController = new ThreadLocalCleanupController();

            ConversionHelper conversionHelper = new ConversionHelper();
            DelegatingConversionHelper delegatingConversionHelper = new DelegatingConversionHelper();
            LinkController linkController = new LinkController();
            LoggerHistory loggerHistory = new LoggerHistory();
            AccessorTypeProvider accessorTypeProvider = new AccessorTypeProvider();
            ExtendableRegistry extendableRegistry = new ExtendableRegistry();
			GarbageProxyFactory garbageProxyFactory = new GarbageProxyFactory();
            PropertyInfoProvider propertyInfoProvider = new PropertyInfoProvider();
            BeanContextInitializer beanContextInitializer = new BeanContextInitializer();
            CallingProxyPostProcessor callingProxyPostProcessor = new CallingProxyPostProcessor();
            ProxyFactory proxyFactory = new ProxyFactory();
            DelegateFactory delegateFactory = new DelegateFactory();
            AutoLinkPreProcessor threadLocalCleanupPreProcessor = new AutoLinkPreProcessor();

            callingProxyPostProcessor.PropertyInfoProvider = propertyInfoProvider;
            delegatingConversionHelper.DefaultConversionHelper = conversionHelper;
            linkController.ExtendableRegistry = extendableRegistry;
            linkController.Props = newProps;
            linkController.ProxyFactory = proxyFactory;
            beanContextInitializer.CallingProxyPostProcessor = callingProxyPostProcessor;
            beanContextInitializer.ConversionHelper = delegatingConversionHelper;
            beanContextInitializer.PropertyInfoProvider = propertyInfoProvider;
			garbageProxyFactory.AccessorTypeProvider = accessorTypeProvider;
            propertyInfoProvider.AccessorTypeProvider = accessorTypeProvider;
            threadLocalCleanupPreProcessor.SetExtendableRegistry(extendableRegistry);
			threadLocalCleanupPreProcessor.SetExtendableType(typeof(IThreadLocalCleanupBeanExtendable));

            LoggerInstancePreProcessor loggerInstancePreProcessor = new LoggerInstancePreProcessor();

            propertyInfoProvider.AfterPropertiesSet();

            ScanForLogInstance(loggerInstancePreProcessor, propertyInfoProvider, newProps, accessorTypeProvider);
            ScanForLogInstance(loggerInstancePreProcessor, propertyInfoProvider, newProps, callingProxyPostProcessor);
            ScanForLogInstance(loggerInstancePreProcessor, propertyInfoProvider, newProps, delegatingConversionHelper);
            ScanForLogInstance(loggerInstancePreProcessor, propertyInfoProvider, newProps, extendableRegistry);
            ScanForLogInstance(loggerInstancePreProcessor, propertyInfoProvider, newProps, linkController);
            ScanForLogInstance(loggerInstancePreProcessor, propertyInfoProvider, newProps, loggerHistory);
            ScanForLogInstance(loggerInstancePreProcessor, propertyInfoProvider, newProps, beanContextInitializer);
            ScanForLogInstance(loggerInstancePreProcessor, propertyInfoProvider, newProps, propertyInfoProvider);
            ScanForLogInstance(loggerInstancePreProcessor, propertyInfoProvider, newProps, threadLocalCleanupController);
            ScanForLogInstance(loggerInstancePreProcessor, propertyInfoProvider, newProps, threadLocalCleanupPreProcessor);

            accessorTypeProvider.AfterPropertiesSet();
            callingProxyPostProcessor.AfterPropertiesSet();
            delegatingConversionHelper.AfterPropertiesSet();
            extendableRegistry.AfterPropertiesSet();
            linkController.AfterPropertiesSet();
            loggerHistory.AfterPropertiesSet();
            beanContextInitializer.AfterPropertiesSet();
            threadLocalCleanupController.AfterPropertiesSet();
            threadLocalCleanupPreProcessor.AfterPropertiesSet();

            PropertiesPreProcessor propertiesPreProcessor = new PropertiesPreProcessor();
            propertiesPreProcessor.ConversionHelper = delegatingConversionHelper;
            propertiesPreProcessor.PropertyInfoProvider = propertyInfoProvider;
            propertiesPreProcessor.AfterPropertiesSet();

            // The DelegatingConversionHelper is functional, but has yet no properties set
            propertiesPreProcessor.PreProcessProperties(null, null, newProps, "delegatingConversionHelper", delegatingConversionHelper, typeof(DelegatingConversionHelper), null, EmptySet.Empty<String>(), null);
            delegatingConversionHelper.AfterPropertiesSet();

            BeanContextFactory parentContextFactory = new BeanContextFactory(linkController, beanContextInitializer, proxyFactory, null, newProps, null);

            parentContextFactory.RegisterWithLifecycle(loggerHistory).Autowireable<ILoggerHistory>();
            parentContextFactory.RegisterWithLifecycle(proxyFactory).Autowireable<IProxyFactory>();
            parentContextFactory.RegisterWithLifecycle(threadLocalCleanupController).Autowireable(typeof(IThreadLocalCleanupController),
                    typeof(IThreadLocalCleanupBeanExtendable));

			parentContextFactory.RegisterExternalBean(delegatingConversionHelper).Autowireable(typeof(IConversionHelper), typeof(IDedicatedConverterExtendable));

            parentContextFactory.RegisterWithLifecycle(accessorTypeProvider).Autowireable<IAccessorTypeProvider>();

            parentContextFactory.RegisterExternalBean(loggerInstancePreProcessor).Autowireable<ILoggerCache>();

            parentContextFactory.RegisterWithLifecycle(extendableRegistry).Autowireable<IExtendableRegistry>();

			parentContextFactory.RegisterWithLifecycle(garbageProxyFactory).Autowireable<IGarbageProxyFactory>();

            parentContextFactory.RegisterWithLifecycle(callingProxyPostProcessor).Autowireable<CallingProxyPostProcessor>();

            parentContextFactory.RegisterWithLifecycle(propertyInfoProvider).Autowireable<IPropertyInfoProvider>();
            
            parentContextFactory.RegisterWithLifecycle(delegateFactory).Autowireable<IDelegateFactory>();

            if (bootstrapModules != null)
            {
                for (int a = 0, size = bootstrapModules.Length; a < size; a++)
                {
                    parentContextFactory.RegisterBean(bootstrapModules[a]);
                }
            }
            if (bootstrapModuleInstances != null)
            {
                for (int a = 0, size = bootstrapModuleInstances.Length; a < size; a++)
                {
                    parentContextFactory.RegisterExternalBean(bootstrapModuleInstances[a]);
                }
            }
            List<IBeanPreProcessor> preProcessors = new List<IBeanPreProcessor>();
            preProcessors.Add(propertiesPreProcessor);
            preProcessors.Add(loggerInstancePreProcessor);
            preProcessors.Add(threadLocalCleanupPreProcessor);
            return parentContextFactory.Create("bootstrap", null, preProcessors, null);
        }

        protected static void ScanForLogInstance(IBeanPreProcessor beanPreProcessor, IPropertyInfoProvider propertyInfoProvider, IProperties properties, Object bean)
        {
            IPropertyInfo[] props = propertyInfoProvider.GetProperties(bean.GetType());
            beanPreProcessor.PreProcessProperties(null, null, properties, null, bean, bean.GetType(), null, EmptySet.Empty<String>(), props);
        }

        protected IList<IBeanConfiguration> beanConfigurations;

        protected HashMap<String, IBeanConfiguration> nameToBeanConfMap;

        protected IDictionary<String, String> aliasToBeanNameMap;

        protected IDictionary<String, IList<String>> beanNameToAliasesMap;

        protected ILinkController linkController;

        protected int anonymousCounter = 0;

        protected IBeanContextInitializer beanContextInitializer;

        protected BeanContextFactory parent;

        protected IProxyFactory proxyFactory;

        protected ITypeInfoProvider typeInfoProvider;

        protected Properties props;

        public BeanContextFactory(ILinkController linkController, IBeanContextInitializer beanContextInitializer, IProxyFactory proxyFactory, ITypeInfoProvider typeInfoProvider, Properties properties,
            BeanContextFactory parent)
        {
            ParamChecker.AssertParamNotNull(linkController, "linkController");
            ParamChecker.AssertParamNotNull(beanContextInitializer, "beanContextInitializer");
            ParamChecker.AssertParamNotNull(proxyFactory, "proxyFactory");
            ParamChecker.AssertParamNotNull(properties, "properties");

            this.linkController = linkController;
            this.beanContextInitializer = beanContextInitializer;
            this.proxyFactory = proxyFactory;
            this.typeInfoProvider = typeInfoProvider;
            this.props = properties;
            this.parent = parent;
        }

        public void Dispose()
        {
            beanConfigurations = null;
            nameToBeanConfMap = null;
            aliasToBeanNameMap = null;
            beanNameToAliasesMap = null;
            linkController = null;
            parent = null;
            beanContextInitializer = null;
            typeInfoProvider = null;
            props = null;
        }

        public IList<IBeanConfiguration> GetBeanConfigurations()
        {
            return beanConfigurations;
        }

        public IDictionary<String, String> GetAliasToBeanNameMap()
        {
            return aliasToBeanNameMap;
        }

        public IDictionary<String, IList<String>> GetBeanNameToAliasesMap()
        {
            return beanNameToAliasesMap;
        }

        public Properties GetProperties()
        {
            return props;
        }

        public IBeanContextInitializer GetBeanContextInitializer()
        {
            return beanContextInitializer;
        }

        public BeanContextFactory CreateChildContextFactory(IBeanContextInitializer beanContextInitializer, IServiceContext serviceContext)
        {
            IProxyFactory proxyFactory = serviceContext.GetService<IProxyFactory>();
            ITypeInfoProvider typeInfoProvider = serviceContext.GetService<ITypeInfoProvider>(false);
            IProperties props = serviceContext.GetService<IProperties>();
            Properties newProps = new Properties(props);
            return new BeanContextFactory(linkController, beanContextInitializer, proxyFactory, typeInfoProvider, newProps, this);
        }

        public IBeanConfiguration GetBeanConfiguration(String beanName)
        {
            if (nameToBeanConfMap == null || beanName == null)
            {
                return null;
            }
            IBeanConfiguration beanConf = nameToBeanConfMap.Get(beanName);
            if (beanConf == null)
            {
                if (aliasToBeanNameMap != null)
                {
                    String aliasName = DictionaryExtension.ValueOrDefault(aliasToBeanNameMap, beanName);
                    if (aliasName != null)
                    {
                        beanConf = nameToBeanConfMap.Get(aliasName);
                    }
                }
            }
            if (beanConf == null && parent != null)
            {
                beanConf = parent.GetBeanConfiguration(beanName);
            }
            return beanConf;
        }

        public String GenerateBeanName(Type beanType)
        {
            return StringBuilderUtil.Concat(delegate(StringBuilder sb)
            {
                int anonymousCounter = ++this.anonymousCounter;

                if (typeInfoProvider != null)
                {
                    sb.Append(typeInfoProvider.GetTypeInfo(beanType).SimpleName);
                }
                else
                {
                    sb.Append(beanType.Name);
                }
                sb.Append('#').Append(anonymousCounter);
            });
        }

        protected String GenerateUniqueContextName(String contextName, ServiceContext parent)
        {
            if (contextName == null)
            {
                contextName = "c";
            }
            int value = new Random().Next();
            if (parent != null)
            {
                return parent.Name + "/" + contextName + " " + value;
            }
            return contextName + " " + value;
        }

        public IServiceContext Create(String contextName, IBackgroundWorkerParamDelegate<IBeanContextFactory> registerPhaseDelegate, IList<IBeanPreProcessor> preProcessors,
            IList<IBeanPostProcessor> postProcessors)
        {
            return Create(contextName, registerPhaseDelegate, preProcessors, postProcessors, emptyServiceModules);
        }

        public IServiceContext Create(String contextName, IBackgroundWorkerParamDelegate<IBeanContextFactory> registerPhaseDelegate, IList<IBeanPreProcessor> preProcessors,
            IList<IBeanPostProcessor> postProcessors, Type[] serviceModuleTypes)
        {
            ServiceContext context = new ServiceContext(GenerateUniqueContextName(contextName, null), null);

            if (registerPhaseDelegate != null)
            {
                registerPhaseDelegate(this);
            }
            foreach (Type serviceModuleType in serviceModuleTypes)
            {
                RegisterBean(serviceModuleType);
            }
            if (preProcessors != null)
            {
                for (int a = 0, size = preProcessors.Count; a < size; a++)
                {
                    context.AddPreProcessor(preProcessors[a]);
                }
            }
            if (postProcessors != null)
            {
                for (int a = 0, size = postProcessors.Count; a < size; a++)
                {
                    context.AddPostProcessor(postProcessors[a]);
                }
            }
            beanContextInitializer.InitializeBeanContext(context, this);
            return context;
        }

        public IServiceContext Create(String contextName, ServiceContext parent, IBackgroundWorkerParamDelegate<IBeanContextFactory> registerPhaseDelegate)
        {
            return Create(contextName, parent, registerPhaseDelegate, emptyServiceModules);
        }

        public IServiceContext Create(String contextName, ServiceContext parent, IBackgroundWorkerParamDelegate<IBeanContextFactory> registerPhaseDelegate, Type[] serviceModuleTypes)
        {
            ServiceContext context = new ServiceContext(GenerateUniqueContextName(contextName, null), parent);

            if (registerPhaseDelegate != null)
            {
                registerPhaseDelegate(this);
            }
            foreach (Type serviceModuleType in serviceModuleTypes)
            {
                RegisterBean(serviceModuleType);
            }
            IList<IBeanPreProcessor> preProcessors = parent.GetPreProcessors();
            if (preProcessors != null)
            {
                for (int a = 0, size = preProcessors.Count; a < size; a++)
                {
                    context.AddPreProcessor(preProcessors[a]);
                }
            }
            IList<IBeanPostProcessor> postProcessors = parent.GetPostProcessors();
            if (postProcessors != null)
            {
                for (int a = 0, size = postProcessors.Count; a < size; a++)
                {
                    context.AddPostProcessor(postProcessors[a]);
                }
            }
            beanContextInitializer.InitializeBeanContext(context, this);
            return context;
        }

        protected void AddBeanConfiguration(IBeanConfiguration beanConfiguration)
        {
            String beanName = beanConfiguration.GetName();
            if (beanName != null && beanName.Length > 0)
            {
                if (nameToBeanConfMap == null)
                {
                    nameToBeanConfMap = new HashMap<String, IBeanConfiguration>();
                }
                if (aliasToBeanNameMap != null && aliasToBeanNameMap.ContainsKey(beanName))
                {
                    throw new Exception("An alias with the name '" + beanName + "' of this bean is already registered in this context");
                }
                if (!beanConfiguration.IsOverridesExisting())
                {
                    if (!nameToBeanConfMap.PutIfNotExists(beanName, beanConfiguration))
                    {
                        IBeanConfiguration existingBeanConfiguration = nameToBeanConfMap.Get(beanName);
                        if (!existingBeanConfiguration.IsOverridesExisting())
                        {
                            throw ServiceContext.CreateDuplicateBeanNameException(beanName, beanConfiguration, existingBeanConfiguration);
                        }
                        // Existing config requests precedence over every other bean config. This is no error
                        return;
                    }
                }
                else
                {
                    // Intentionally put the configuration in the map unaware of an existing entry
                    nameToBeanConfMap.Put(beanName, beanConfiguration);
                }
            }
            if (beanConfigurations == null)
            {
                beanConfigurations = new List<IBeanConfiguration>();
            }
            beanConfigurations.Add(beanConfiguration);
        }


        public void RegisterAlias(String aliasBeanName, String beanNameToCreateAliasFor)
        {
            if (aliasToBeanNameMap == null)
            {
                aliasToBeanNameMap = new Dictionary<String, String>();
                beanNameToAliasesMap = new Dictionary<String, IList<String>>();
            }
            if (aliasToBeanNameMap.ContainsKey(aliasBeanName))
            {
                throw new System.Exception("Alias '" + aliasBeanName + "' has been already specified");
            }
            aliasToBeanNameMap.Add(aliasBeanName, beanNameToCreateAliasFor);
            IList<String> aliasList = DictionaryExtension.ValueOrDefault(beanNameToAliasesMap, beanNameToCreateAliasFor);
            if (aliasList == null)
            {
                aliasList = new List<String>();
                beanNameToAliasesMap.Add(beanNameToCreateAliasFor, aliasList);
            }
            aliasList.Add(aliasBeanName);
        }

        public IBeanConfiguration RegisterBean<T>(String beanName)
        {
            return RegisterBean(beanName, typeof(T));
        }

        public IBeanConfiguration RegisterBean(String beanName, Type beanType)
        {
            ParamChecker.AssertParamNotNull(beanName, "beanName");
            ParamChecker.AssertParamNotNull(beanType, "beanType");
            BeanConfiguration beanConfiguration = new BeanConfiguration(beanType, beanName, proxyFactory, props);

            AddBeanConfiguration(beanConfiguration);
            return beanConfiguration;
        }

        public IBeanConfiguration RegisterBean(String beanName, String parentBeanName)
        {
            ParamChecker.AssertParamNotNull(beanName, "beanName");
            ParamChecker.AssertParamNotNull(parentBeanName, "parentBeanName");
            BeanConfiguration beanConfiguration = new BeanConfiguration(null, beanName, proxyFactory, props);
            beanConfiguration.Parent(parentBeanName);

            AddBeanConfiguration(beanConfiguration);
            return beanConfiguration;
        }

        [Obsolete]
        public IBeanConfiguration RegisterAnonymousBean<T>()
        {
            return RegisterBean<T>();
        }

        [Obsolete]
        public IBeanConfiguration RegisterAnonymousBean(Type beanType)
        {
            return RegisterBean(beanType);
        }

        public IBeanConfiguration RegisterBean<T>()
        {
            return RegisterBean(typeof(T));
        }

        public IBeanConfiguration RegisterBean(Type beanType)
        {
            ParamChecker.AssertParamNotNull(beanType, "beanType");

            BeanConfiguration beanConfiguration = new BeanConfiguration(beanType, GenerateBeanName(beanType), proxyFactory, props);

            AddBeanConfiguration(beanConfiguration);
            return beanConfiguration;
        }

        public IBeanConfiguration RegisterAutowireableBean<I, T>() where T : I
        {
            return RegisterAutowireableBean(typeof(T), typeof(I));
        }

        public IBeanConfiguration RegisterAutowireableBean(Type beanType, Type typeToPublish)
        {
            BeanConfiguration beanConfiguration = new BeanConfiguration(beanType, GenerateBeanName(beanType), proxyFactory, props);
            AddBeanConfiguration(beanConfiguration);
            beanConfiguration.Autowireable(typeToPublish);
            return beanConfiguration;
        }

        public IBeanConfiguration RegisterExternalBean(String beanName, Object externalBean)
        {
            ParamChecker.AssertParamNotNull(beanName, "beanName");
            ParamChecker.AssertParamNotNull(externalBean, "externalBean (" + beanName + ")");
            BeanInstanceConfiguration beanConfiguration = new BeanInstanceConfiguration(externalBean, beanName, false, props);
            AddBeanConfiguration(beanConfiguration);
            return beanConfiguration;
        }

        public IBeanConfiguration RegisterExternalBean(Object externalBean)
        {
            ParamChecker.AssertParamNotNull(externalBean, "externalBean");
            return RegisterExternalBean(GenerateBeanName(externalBean.GetType()), externalBean);
        }

        public IBeanConfiguration RegisterWithLifecycle(String beanName, Object obj)
        {
            ParamChecker.AssertParamNotNull(beanName, "beanName");
            ParamChecker.AssertParamNotNull(obj, "externalBean (" + beanName + ")");
            BeanInstanceConfiguration beanConfiguration = new BeanInstanceConfiguration(obj, beanName, true, props);
            AddBeanConfiguration(beanConfiguration);
            return beanConfiguration;
        }

        public IBeanConfiguration RegisterWithLifecycle(Object obj)
        {
            ParamChecker.AssertParamNotNull(obj, "externalBean");
            return RegisterWithLifecycle(GenerateBeanName(obj.GetType()), obj);
        }

        public void RegisterDisposable(IDisposable disposable)
        {
            ParamChecker.AssertParamNotNull(disposable, "disposable");
            RegisterWithLifecycle(new DisposableHook(disposable));
        }

        public void RegisterDisposable(IDisposableBean disposableBean)
        {
            ParamChecker.AssertParamNotNull(disposableBean, "disposableBean");
            RegisterWithLifecycle(new DisposableBeanHook(disposableBean));
        }

        public ILinkRegistryNeededConfiguration Link(String listenerBeanName)
        {
            LinkConfiguration<Object> linkConfiguration = linkController.CreateLinkConfiguration(listenerBeanName, (String)null);
            AddBeanConfiguration(linkConfiguration);
            return linkConfiguration;
        }

        public ILinkRegistryNeededConfiguration Link(String listenerBeanName, String methodName)
        {
            LinkConfiguration<Object> linkConfiguration = linkController.CreateLinkConfiguration(listenerBeanName, methodName);
            AddBeanConfiguration(linkConfiguration);
            return linkConfiguration;
        }

        public ILinkRegistryNeededConfiguration Link(IBeanConfiguration listenerBean)
        {
            LinkConfiguration<Object> linkConfiguration = linkController.CreateLinkConfiguration(listenerBean, null);
            AddBeanConfiguration(linkConfiguration);
            return linkConfiguration;
        }

        public ILinkRegistryNeededConfiguration Link(IBeanConfiguration listenerBean, String methodName)
        {
            LinkConfiguration<Object> linkConfiguration = linkController.CreateLinkConfiguration(listenerBean, methodName);
            AddBeanConfiguration(linkConfiguration);
            return linkConfiguration;
        }

        public ILinkRegistryNeededConfiguration Link(Object listener, String methodName)
        {
            LinkConfiguration<Object> linkConfiguration = linkController.CreateLinkConfiguration(listener, methodName);
            AddBeanConfiguration(linkConfiguration);
            return linkConfiguration;
        }

        public ILinkRegistryNeededConfiguration<D> Link<D>(D listener)
        {
            LinkConfiguration<D> linkConfiguration = linkController.CreateLinkConfiguration(listener);
            AddBeanConfiguration(linkConfiguration);
            return linkConfiguration;
        }

        public ILinkRegistryNeededRuntime Link(IServiceContext serviceContext, String listenerBeanName)
        {
            return linkController.Link(serviceContext, listenerBeanName);
        }

        public ILinkRegistryNeededRuntime Link(IServiceContext serviceContext, String listenerBeanName, String methodName)
        {
            return linkController.Link(serviceContext, listenerBeanName, methodName);
        }

        public ILinkRegistryNeededRuntime Link(IServiceContext serviceContext, IBeanConfiguration listenerBean)
        {
            return linkController.Link(serviceContext, listenerBean);
        }

        public ILinkRegistryNeededRuntime Link(IServiceContext serviceContext, IBeanConfiguration listenerBean, String methodName)
        {
            return linkController.Link(serviceContext, listenerBean, methodName);
        }

        public ILinkRegistryNeededRuntime Link(IServiceContext serviceContext, Object listener, String methodName)
        {
            return linkController.Link(serviceContext, listener, methodName);
        }

        public ILinkRegistryNeededRuntime<D> Link<D>(IServiceContext serviceContext, D listener)
        {
            return linkController.Link(serviceContext, listener);
        }

        public LinkConfiguration<Object> CreateLinkConfiguration(String listenerBeanName, String methodName)
        {
            return linkController.CreateLinkConfiguration(listenerBeanName, methodName);
        }

        public LinkConfiguration<Object> CreateLinkConfiguration(IBeanConfiguration listenerBean, String methodName)
        {
            return linkController.CreateLinkConfiguration(listenerBean, methodName);
        }

        public LinkConfiguration<Object> CreateLinkConfiguration(Object listener, String methodName)
        {
            return linkController.CreateLinkConfiguration(listener, methodName);
        }

        public LinkConfiguration<D> CreateLinkConfiguration<D>(D listener)
        {
            return linkController.CreateLinkConfiguration(listener);
        }

        [Obsolete]
        public void Link(IServiceContext serviceContext, String registryBeanName, String listenerBeanName, Type registryClass)
        {
            linkController.Link(serviceContext, registryBeanName, listenerBeanName, registryClass);
        }

        [Obsolete]
        public void Link(IServiceContext serviceContext, String registryBeanName, String listenerBeanName, Type registryClass, params Object[] arguments)
        {
            linkController.Link(serviceContext, registryBeanName, listenerBeanName, registryClass, arguments);
        }

        [Obsolete]
        public void Link(IServiceContext serviceContext, IBeanConfiguration listenerBean, Type autowiredRegistryClass)
        {
            linkController.Link(serviceContext, listenerBean, autowiredRegistryClass);
        }

        [Obsolete]
        public void Link(IServiceContext serviceContext, IBeanConfiguration listenerBean, Type autowiredRegistryClass, params Object[] arguments)
        {
            linkController.Link(serviceContext, listenerBean, autowiredRegistryClass, arguments);
        }

        [Obsolete]
        public void Link(IServiceContext serviceContext, String listenerBeanName, Type autowiredRegistryClass)
        {
            linkController.Link(serviceContext, listenerBeanName, autowiredRegistryClass);
        }

        [Obsolete]
        public void Link(IServiceContext serviceContext, String listenerBeanName, Type autowiredRegistryClass, params Object[] arguments)
        {
            linkController.Link(serviceContext, listenerBeanName, autowiredRegistryClass, arguments);
        }

        [Obsolete]
        public void Link<R>(IServiceContext serviceContext, String registryBeanName, String listenerBeanName)
        {
            linkController.Link<R>(serviceContext, registryBeanName, listenerBeanName);
        }

        [Obsolete]
        public void Link<R>(IServiceContext serviceContext, String registryBeanName, String listenerBeanName, params Object[] arguments)
        {
            linkController.Link<R>(serviceContext, registryBeanName, listenerBeanName, arguments);
        }

        [Obsolete]
        public void Link<R>(IServiceContext serviceContext, IBeanConfiguration listenerBean)
        {
            linkController.Link<R>(serviceContext, listenerBean);
        }

        [Obsolete]
        public void Link<R>(IServiceContext serviceContext, IBeanConfiguration listenerBean, params Object[] arguments)
        {
            linkController.Link<R>(serviceContext, listenerBean, arguments);
        }

        [Obsolete]
        public void Link<R>(IServiceContext serviceContext, String listenerBeanName)
        {
            linkController.Link<R>(serviceContext, listenerBeanName);
        }

        [Obsolete]
        public void Link<R>(IServiceContext serviceContext, String listenerBeanName, params Object[] arguments)
        {
            linkController.Link<R>(serviceContext, listenerBeanName, arguments);
        }

        [Obsolete]
        public IBeanConfiguration createLinkConfiguration(String registryBeanName, String listenerBeanName, Type registryClass)
        {
            return createLinkConfiguration(registryBeanName, listenerBeanName, registryClass, emptyArgs);
        }

        [Obsolete]
        public IBeanConfiguration createLinkConfiguration(String registryBeanName, String listenerBeanName, Type registryClass, params Object[] arguments)
        {
            return linkController.createLinkConfiguration(registryBeanName, listenerBeanName, registryClass, arguments);
        }

        [Obsolete]
        public IBeanConfiguration createLinkConfiguration(String listenerBeanName, Type autowiredRegistryClass)
        {
            return createLinkConfiguration(listenerBeanName, autowiredRegistryClass, emptyArgs);
        }

        [Obsolete]
        public IBeanConfiguration createLinkConfiguration(String listenerBeanName, Type autowiredRegistryClass, params Object[] arguments)
        {
            return linkController.createLinkConfiguration(listenerBeanName, autowiredRegistryClass, arguments);
        }

        [Obsolete]
        public void LinkToNamed(String registryBeanName, String listenerBeanName, Type registryClass)
        {
            LinkToNamed(registryBeanName, listenerBeanName, registryClass, emptyArgs);
        }

        [Obsolete]
        public void Link<R>(IBeanConfiguration listenerBean)
        {
            Link<R>(listenerBean, emptyArgs);
        }

        [Obsolete]
        public void Link<R>(IBeanConfiguration listenerBean, params Object[] arguments)
        {
            Link(listenerBean, typeof(R), arguments);
        }

        [Obsolete]
        public void Link(IBeanConfiguration listenerBean, Type autowiredRegistryClass)
        {
            Link(listenerBean, autowiredRegistryClass, emptyArgs);
        }

        [Obsolete]
        public void Link<R>(String listenerBeanName)
        {
            Link<R>(listenerBeanName, emptyArgs);
        }

        [Obsolete]
        public void Link<R>(String listenerBeanName, params Object[] arguments)
        {
            Link(listenerBeanName, typeof(R), arguments);
        }

        [Obsolete]
        public void Link(String listenerBeanName, Type autowiredRegistryClass)
        {
            Link(listenerBeanName, autowiredRegistryClass, emptyArgs);
        }

        [Obsolete]
        public void LinkToNamed<R>(String registryBeanName, String listenerBeanName)
        {
            LinkToNamed(registryBeanName, listenerBeanName, typeof(R), emptyArgs);
        }

        [Obsolete]
        public void LinkToNamed<R>(String registryBeanName, String listenerBeanName, params Object[] arguments)
        {
            LinkToNamed(registryBeanName, listenerBeanName, typeof(R), arguments);
        }

        [Obsolete]
        public void LinkToNamed(String registryBeanName, String listenerBeanName, Type registryClass, params Object[] arguments)
        {
            IBeanConfiguration beanConfiguration = createLinkConfiguration(registryBeanName, listenerBeanName, registryClass, arguments);
            AddBeanConfiguration(beanConfiguration);
        }

        [Obsolete]
        public IBeanConfiguration createEventLinkConfiguration(String eventProviderBeanName, Type eventInterface, String listenerBeanName, String methodName)
        {
            ParamChecker.AssertParamNotNull(eventProviderBeanName, "eventProviderBeanName");
            ParamChecker.AssertParamNotNull(eventInterface, "eventInterface");
            ParamChecker.AssertParamNotNull(listenerBeanName, "listenerBeanName");
            ParamChecker.AssertParamNotNull(methodName, "methodName");
            return linkController.createEventLinkConfiguration(eventProviderBeanName, eventInterface, listenerBeanName, methodName);
        }

        [Obsolete]
        public IBeanConfiguration createEventLinkConfiguration(String eventProviderBeanName, Type eventInterface, String handlerDelegateBeanName)
        {
            ParamChecker.AssertParamNotNull(eventProviderBeanName, "eventProviderBeanName");
            ParamChecker.AssertParamNotNull(eventInterface, "eventInterface");
            ParamChecker.AssertParamNotNull(handlerDelegateBeanName, "handlerDelegateBeanName");
            return linkController.createEventLinkConfiguration(eventProviderBeanName, eventInterface, handlerDelegateBeanName);
        }

        [Obsolete]
        public IBeanConfiguration createEventLinkConfiguration(String eventProviderBeanName, Type eventInterface, Delegate handlerDelegate)
        {
            ParamChecker.AssertParamNotNull(eventProviderBeanName, "eventProviderBeanName");
            ParamChecker.AssertParamNotNull(eventInterface, "eventInterface");
            ParamChecker.AssertParamNotNull(handlerDelegate, "handlerDelegate");
            return linkController.createEventLinkConfiguration(eventProviderBeanName, eventInterface, handlerDelegate);
        }

        [Obsolete]
        public IBeanConfiguration createEventLinkConfiguration<D>(String eventProviderBeanName, IEventDelegate<D> eventName, String listenerBeanName, String methodName)
        {
            return linkController.createEventLinkConfiguration(eventProviderBeanName, eventName, listenerBeanName, methodName);
        }

        [Obsolete]
        public IBeanConfiguration createEventLinkConfiguration<D>(String eventProviderBeanName, IEventDelegate<D> eventName, String handlerDelegateBeanName)
        {
            return linkController.createEventLinkConfiguration(eventProviderBeanName, eventName, handlerDelegateBeanName);
        }

        [Obsolete]
        public IBeanConfiguration createEventLinkConfiguration<D>(String eventProviderBeanName, IEventDelegate<D> eventName, D handlerDelegate)
        {
            return linkController.createEventLinkConfiguration(eventProviderBeanName, eventName, handlerDelegate);
        }

        //public void LinkToEvent(IServiceContext serviceContext, String eventProviderBeanName, String eventName, String handlerDelegateBeanName)
        //{
        //    linkController.LinkToEvent(serviceContext, eventProviderBeanName, eventName, handlerDelegateBeanName);
        //}

        //public void LinkToEvent(IServiceContext serviceContext, String eventProviderBeanName, String eventName, Delegate handlerDelegate)
        //{
        //    linkController.LinkToEvent(serviceContext, eventProviderBeanName, eventName, handlerDelegate);
        //}

        [Obsolete]
        public void LinkToEvent<R>(IServiceContext serviceContext, String eventProviderBeanName, String listenerBeanName, String methodName)
        {
            linkController.LinkToEvent<R>(serviceContext, eventProviderBeanName, listenerBeanName, methodName);
        }

        [Obsolete]
        public void LinkToEvent<R>(IServiceContext serviceContext, String eventProviderBeanName, String handlerDelegateBeanName)
        {
            linkController.LinkToEvent<R>(serviceContext, eventProviderBeanName, handlerDelegateBeanName);
        }

        [Obsolete]
        public void LinkToEvent<R>(IServiceContext serviceContext, String eventProviderBeanName, Delegate handlerDelegate)
        {
            linkController.LinkToEvent<R>(serviceContext, eventProviderBeanName, handlerDelegate);
        }

        [Obsolete]
        public void LinkToEvent<D>(String eventProviderBeanName, IEventDelegate<D> eventName, String listenerBeanName, String methodName)
        {
            IBeanConfiguration beanConfiguration = createEventLinkConfiguration(eventProviderBeanName, eventName, listenerBeanName, methodName);
            AddBeanConfiguration(beanConfiguration);
        }

        [Obsolete]
        public void LinkToEvent<D>(String eventProviderBeanName, IEventDelegate<D> eventName, String handlerDelegateBeanName)
        {
            IBeanConfiguration beanConfiguration = createEventLinkConfiguration(eventProviderBeanName, eventName, handlerDelegateBeanName);
            AddBeanConfiguration(beanConfiguration);
        }

        [Obsolete]
        public void LinkToEvent<D>(String eventProviderBeanName, IEventDelegate<D> eventName, D handlerDelegate)
        {
            IBeanConfiguration beanConfiguration = createEventLinkConfiguration(eventProviderBeanName, eventName, handlerDelegate);
            AddBeanConfiguration(beanConfiguration);
        }

        [Obsolete]
        public void LinkToEvent<D>(IServiceContext serviceContext, String eventProviderBeanName, IEventDelegate<D> eventName, String listenerBeanName, String methodName)
        {
            linkController.LinkToEvent(serviceContext, eventProviderBeanName, eventName, listenerBeanName, methodName);
        }

        [Obsolete]
        public void LinkToEvent<D>(IServiceContext serviceContext, String eventProviderBeanName, IEventDelegate<D> eventName, String handlerDelegateBeanName)
        {
            linkController.LinkToEvent(serviceContext, eventProviderBeanName, eventName, handlerDelegateBeanName);
        }

        [Obsolete]
        public void LinkToEvent<D>(IServiceContext serviceContext, String eventProviderBeanName, IEventDelegate<D> eventName, D handlerDelegate)
        {
            linkController.LinkToEvent(serviceContext, eventProviderBeanName, eventName, handlerDelegate);
        }

        [Obsolete]
        public void LinkToEvent<R>(String eventProviderBeanName, String listenerBeanName, String methodName)
        {
            IBeanConfiguration beanConfiguration = linkController.createEventLinkConfiguration(eventProviderBeanName, typeof(R), listenerBeanName, methodName);
            AddBeanConfiguration(beanConfiguration);
        }

        [Obsolete]
        public void LinkToEvent<R>(String eventProviderBeanName, String handlerDelegateBeanName)
        {
            IBeanConfiguration beanConfiguration = linkController.createEventLinkConfiguration(eventProviderBeanName, typeof(R), handlerDelegateBeanName);
            AddBeanConfiguration(beanConfiguration);
        }

        [Obsolete]
        public void LinkToEvent<R>(String eventProviderBeanName, Delegate handlerDelegate)
        {
            IBeanConfiguration beanConfiguration = linkController.createEventLinkConfiguration(eventProviderBeanName, typeof(R), handlerDelegate);
            AddBeanConfiguration(beanConfiguration);
        }

        [Obsolete]
        public void Link(IBeanConfiguration listenerBean, Type autowiredRegistryClass, params Object[] arguments)
        {
            ParamChecker.AssertParamNotNull(listenerBean, "listenerBean");
            String listenerBeanName = listenerBean.GetName();
            ParamChecker.AssertParamNotNull(listenerBeanName, "listenerBean.getName()");

            IBeanConfiguration beanConfiguration = createLinkConfiguration(listenerBeanName, autowiredRegistryClass, arguments);
            AddBeanConfiguration(beanConfiguration);
        }

        [Obsolete]
        public void Link(String listenerBeanName, Type autowiredRegistryClass, params Object[] arguments)
        {
            IBeanConfiguration beanConfiguration = createLinkConfiguration(listenerBeanName, autowiredRegistryClass, arguments);
            AddBeanConfiguration(beanConfiguration);
        }

        public static void Transfer(IServiceContext sourceContext, IBeanContextFactory targetContextFactory, Type autowireableType)
        {
            ParamChecker.AssertParamNotNull(sourceContext, "sourceContext");
            ParamChecker.AssertParamNotNull(targetContextFactory, "targetContextFactory");
            ParamChecker.AssertParamNotNull(autowireableType, "autowireableType");
            Object bean = sourceContext.GetService(autowireableType);
            if (bean == null)
            {
                throw new System.Exception("No autowired bean found for type " + autowireableType.FullName);
            }
            targetContextFactory.RegisterExternalBean(bean).Autowireable(autowireableType);
        }

        public static void Transfer(IServiceContext sourceContext, IBeanContextFactory targetContextFactory, Type[] autowireableTypes)
        {
            ParamChecker.AssertParamNotNull(sourceContext, "sourceContext");
            ParamChecker.AssertParamNotNull(targetContextFactory, "targetContextFactory");
            ParamChecker.AssertParamNotNull(autowireableTypes, "autowireableTypes");
            for (int a = autowireableTypes.Length; a-- > 0; )
            {
                Transfer(sourceContext, targetContextFactory, autowireableTypes[a]);
            }
        }

        public static void Transfer(IServiceContext sourceContext, IBeanContextFactory targetContextFactory, String beanName)
        {
            ParamChecker.AssertParamNotNull(sourceContext, "sourceContext");
            ParamChecker.AssertParamNotNull(targetContextFactory, "targetContextFactory");
            ParamChecker.AssertParamNotNull(beanName, "beanName");
            Object bean = sourceContext.GetService(beanName);
            if (bean == null)
            {
                throw new System.Exception("No bean found with name '" + beanName + "'");
            }
            targetContextFactory.RegisterExternalBean(beanName, bean);
        }

        public static void Transfer(IServiceContext sourceContext, IBeanContextFactory targetContextFactory, String[] beanNames)
        {
            ParamChecker.AssertParamNotNull(sourceContext, "sourceContext");
            ParamChecker.AssertParamNotNull(targetContextFactory, "targetContextFactory");
            ParamChecker.AssertParamNotNull(beanNames, "beanNames");
            for (int a = beanNames.Length; a-- > 0; )
            {
                Transfer(sourceContext, targetContextFactory, beanNames[a]);
            }
        }
    }
}