using System;
using System.Collections.Generic;
using System.Text;
using System.Threading;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Exceptions;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Ioc.Exceptions;
using De.Osthus.Ambeth.Ioc.Hierarchy;
using De.Osthus.Ambeth.Ioc.Link;
using De.Osthus.Ambeth.Ioc.Proxy;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System.Diagnostics;

namespace De.Osthus.Ambeth.Ioc.Factory
{

    public class BeanContextInitializer : IBeanContextInitializer, IInitializingBean
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        protected static readonly CHashSet<Type> primitiveSet = new CHashSet<Type>(0.5f);

        protected static readonly HashMap<PrecedenceType, int> precedenceOrder = new HashMap<PrecedenceType, int>(0.5f);

        protected static readonly ThreadLocal<BeanContextInit> currentBeanContextInitTL = new ThreadLocal<BeanContextInit>();

        static BeanContextInitializer()
        {
            ImmutableTypeSet.AddImmutableTypesTo(primitiveSet);
            primitiveSet.Add(typeof(Object));

            precedenceOrder.Put(PrecedenceType.LOWEST, 0);
            precedenceOrder.Put(PrecedenceType.LOWER, 1);
            precedenceOrder.Put(PrecedenceType.LOW, 2);
            precedenceOrder.Put(PrecedenceType.MEDIUM, 3);
            precedenceOrder.Put(PrecedenceType.DEFAULT, 3);
            precedenceOrder.Put(PrecedenceType.HIGH, 4);
            precedenceOrder.Put(PrecedenceType.HIGHER, 5);
            precedenceOrder.Put(PrecedenceType.HIGHEST, 6);
        }

        static void AddPrimitive(Type type)
        {
            primitiveSet.Add(type);
        }

        public CallingProxyPostProcessor CallingProxyPostProcessor { protected get; set; }

        public IConversionHelper ConversionHelper { protected get; set; }

        public IPropertyInfoProvider PropertyInfoProvider { protected get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(CallingProxyPostProcessor, "CallingProxyPostProcessor");
            ParamChecker.AssertNotNull(ConversionHelper, "ConversionHelper");
            ParamChecker.AssertNotNull(PropertyInfoProvider, "PropertyInfoProvider");
        }

        protected int GetBeanConfigurationAmount(BeanContextInit beanContextInit)
        {
            IList<IBeanConfiguration> beanConfigurations = beanContextInit.beanContextFactory.GetBeanConfigurations();
            if (beanConfigurations == null)
            {
                return 0;
            }
            return beanConfigurations.Count;
        }

        public void InitializeBeanContext(ServiceContext beanContext, BeanContextFactory beanContextFactory)
        {
            beanContext.SetBeanContextFactory(beanContextFactory);
            if (beanContextFactory.GetBeanConfigurations() == null)
            {
                return;
            }
            IdentityLinkedMap<Object, IBeanConfiguration> objectToBeanConfigurationMap = new IdentityLinkedMap<Object, IBeanConfiguration>();
            HashMap<String, IBeanConfiguration> nameToBeanConfigurationMap = new HashMap<String, IBeanConfiguration>();
            HashMap<Type, IBeanConfiguration> typeToBeanConfigurationMap = new HashMap<Type, IBeanConfiguration>();
            IdentityHashSet<Object> allLifeCycledBeansSet = new IdentityHashSet<Object>();
            System.Collections.Generic.List<IBeanConfiguration> beanConfHierarchy = new System.Collections.Generic.List<IBeanConfiguration>();
            IISet<IBeanConfiguration> alreadyHandledConfigsSet = new IdentityHashSet<IBeanConfiguration>();
            System.Collections.Generic.List<Object> initializedOrdering = new System.Collections.Generic.List<Object>();

            BeanContextInit beanContextInit = new BeanContextInit();
            BeanContextInit oldBeanContextInit = currentBeanContextInitTL.Value;
            try
            {
                currentBeanContextInitTL.Value = beanContextInit;

                beanContextInit.beanContext = beanContext;
                beanContextInit.beanContextFactory = beanContextFactory;
                beanContextInit.objectToBeanConfigurationMap = objectToBeanConfigurationMap;
                beanContextInit.allLifeCycledBeansSet = allLifeCycledBeansSet;
                beanContextInit.initializedOrdering = initializedOrdering;

                Properties contextProps = beanContextFactory.GetProperties();
                beanContextInit.properties = contextProps;

                beanContextFactory.RegisterExternalBean("properties", contextProps).Autowireable(typeof(IProperties), typeof(Properties));

                Object priorityBean;
                do
                {
                    priorityBean = null;
                    int highestPriority = 0;

                    InstantiateBeans(beanContextInit, nameToBeanConfigurationMap, alreadyHandledConfigsSet, true);

                    foreach (Entry<Object, IBeanConfiguration> entry in objectToBeanConfigurationMap)
                    {
                        Object bean = entry.Key;

                        int priorityOfBean = GetPriorityOfBean(bean.GetType());
                        if (priorityOfBean > highestPriority)
                        {
                            highestPriority = priorityOfBean;
                            priorityBean = bean;
                            continue;
                        }
                    }
                    if (priorityBean != null)
                    {
                        InitializeBean(beanContextInit, priorityBean);
                    }
                }
                while (priorityBean != null);

                while (true)
                {
                    int beanConfigurationCountBefore = GetBeanConfigurationAmount(beanContextInit);
                    InstantiateBeans(beanContextInit, nameToBeanConfigurationMap, alreadyHandledConfigsSet, false);

                    // Now load properties-service from the current context (it may be
                    // another)
                    beanContextInit.properties = (Properties)beanContext.GetService<IProperties>(true);

                    ResolveBeansInSequence(beanContextInit);
                    int beanConfigurationCountAfter = GetBeanConfigurationAmount(beanContextInit);
                    if (beanConfigurationCountAfter == beanConfigurationCountBefore)
                    {
                        break;
                    }
                }
                CheckIfAllBeanConfigsAreHandledCorrectly(beanContextInit, alreadyHandledConfigsSet);

                // Notify first all modules that this context is now ready
                for (int a = 0, size = initializedOrdering.Count; a < size; a++)
                {
                    Object bean = initializedOrdering[a];
                    if (bean is IStartingModule)
                    {
                        ((IStartingModule)bean).AfterStarted(beanContext);
                    }
                }
                // Then notify all link containers that this context is now ready for
                // linking
                for (int a = 0, size = initializedOrdering.Count; a < size; a++)
                {
                    Object bean = initializedOrdering[a];
                    if (bean is ILinkContainer)
                    {
                        ILinkContainer linkContainer = (ILinkContainer)bean;
                        linkContainer.Link();
                    }
                }
                IList<ILinkContainer> linkContainers = beanContext.GetLinkContainers();
                if (linkContainers != null)
                {
                    for (int a = 0, size = linkContainers.Count; a < size; a++)
                    {
                        ILinkContainer linkContainer = linkContainers[a];
                        if (allLifeCycledBeansSet.Contains(linkContainer))
                        {
                            // Nothing to do because this container has already been handled some lines before
                            continue;
                        }
                        linkContainer.Link();
                    }
                }
                beanContext.SetRunning();
                // Then notify all "normal" beans that this context is now ready
                for (int a = 0, size = initializedOrdering.Count; a < size; a++)
                {
                    Object bean = initializedOrdering[a];
                    if (bean is IStartingBean)
                    {
                        ((IStartingBean)bean).AfterStarted();
                    }
                }
                PublishMonitorableBeans(beanContextInit, initializedOrdering);
            }
            catch (Exception)
            {
                List<IDisposableBean> toDestroyOnError = beanContextInit.toDestroyOnError;
                for (int a = 0, size = toDestroyOnError.Count; a < size; a++)
                {
                    toDestroyOnError[a].Destroy();
                }
                throw;
            }
            finally
            {
                currentBeanContextInitTL.Value = oldBeanContextInit;
            }
        }

        protected StringBuilder ConvertBeanContextName(String beanContextName)
        {
            String[] split = beanContextName.Split('/');
            StringBuilder sb = new StringBuilder();
            sb.Append("de.osthus.ambeth:module=ioc,context=");
            for (int b = 0, sizeB = split.Length; b < sizeB; b++)
            {
                if (b > 0)
                {
                    sb.Append(",context");
                    sb.Append(b);
                    sb.Append("=");
                }
                sb.Append(split[b]);
            }
            sb.Append(",name=");
            return sb;
        }

        protected String CreateMonitoringNameOfBean(StringBuilder beanContextName, IBeanConfiguration beanConfiguration)
        {
            int oldLength = beanContextName.Length;
            beanContextName.Append(beanConfiguration.GetName());
            try
            {
                return beanContextName.ToString();
            }
            finally
            {
                beanContextName.Length = oldLength;
            }
        }

        protected void PublishMonitorableBeans(BeanContextInit beanContextInit, IList<Object> initializedOrdering)
        {
            bool monitorBeansActive = Boolean.Parse(beanContextInit.properties.GetString(IocConfigurationConstants.MonitorBeansActive, "true"));
            if (!monitorBeansActive)
            {
                return;
            }
            IPropertyInfoProvider propertyInfoProvider = beanContextInit.beanContext.GetService<IPropertyInfoProvider>(false);
            if (propertyInfoProvider == null)
            {
                return;
            }
            //IServiceContext beanContext = beanContextInit.beanContext;
            //IdentityHashMap<Object, IBeanConfiguration> objectToHandledBeanConfigurationMap = beanContextInit.objectToHandledBeanConfigurationMap;
            //final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            //final List<ObjectName> mBeans = new ArrayList<ObjectName>();
            //boolean success = false;
            //try
            //{
            //    StringBuilder beanContextName = convertBeanContextName(beanContext.getName());
            //    for (int a = 0, size = initializedOrdering.size(); a < size; a++)
            //    {
            //        final Object bean = initializedOrdering.get(a);
            //        IBeanConfiguration beanConfiguration = objectToHandledBeanConfigurationMap.get(bean);
            //        if (beanConfiguration == null || beanConfiguration.getName() == null)
            //        {
            //            // beans without a name will not be browsable
            //            continue;
            //        }
            //        BeanMonitoringSupport mBean = new BeanMonitoringSupport(bean, beanContext);
            //        if (mBean.getMBeanInfo().getAttributes().length == 0)
            //        {
            //            continue;
            //        }
            //        try
            //        {
            //            ObjectName name = createMonitoringNameOfBean(beanContextName, beanConfiguration);
            //            mbs.registerMBean(mBean, name);
            //            mBeans.add(name);
            //        }
            //        catch (Throwable e)
            //        {
            //            throw RuntimeExceptionUtil.mask(e);
            //        }
            //    }
            //    success = true;
            //}
            //finally
            //{
            //    if (!success)
            //    {
            //        for (int a = mBeans.size(); a-- > 0;)
            //        {
            //            ObjectName name = mBeans.get(a);
            //            try
            //            {
            //                mbs.unregisterMBean(name);
            //            }
            //            catch (Throwable e)
            //            {
            //                throw RuntimeExceptionUtil.mask(e);
            //            }
            //        }
            //    }
            //}
            //beanContext.registerDisposeHook(new IBackgroundWorkerParamDelegate<IServiceContext>()
            //{
            //    @Override
            //    public void invoke(IServiceContext beanContext) throws Throwable
            //    {
            //        for (int a = mBeans.size(); a-- > 0;)
            //        {
            //            ObjectName name = mBeans.get(a);
            //            mbs.unregisterMBean(name);
            //        }
            //    }
            //});
        }

        protected void ResolveBeansInSequence(BeanContextInit beanContextInit)
        {
            IdentityLinkedMap<Object, IBeanConfiguration> objectToBeanConfigurationMap = beanContextInit.objectToBeanConfigurationMap;
            IList<Object> initializedBeans = new List<Object>();

            while (objectToBeanConfigurationMap.Count > 0)
            {
                foreach (Entry<Object, IBeanConfiguration> entry in objectToBeanConfigurationMap)
                {
                    Object bean = entry.Key;
                    IBeanConfiguration beanConfiguration = entry.Value;

                    InitializeBean(beanContextInit, bean);
                    break;
                }
            }
        }

        protected void CheckIfAllBeanConfigsAreHandledCorrectly(BeanContextInit beanContextInit, IISet<IBeanConfiguration> alreadyHandledConfigsSet)
        {
            BeanContextFactory beanContextFactory = beanContextInit.beanContextFactory;
            ServiceContext beanContext = beanContextInit.beanContext;
            IList<IBeanConfiguration> basicBeanConfigurations = beanContextFactory.GetBeanConfigurations();
            if (basicBeanConfigurations != null)
            {
                for (int a = basicBeanConfigurations.Count; a-- > 0; )
                {
                    IBeanConfiguration beanConfiguration = basicBeanConfigurations[a];
                    if (alreadyHandledConfigsSet.Contains(beanConfiguration))
                    {
                        continue;
                    }
                    IList<IBeanConfiguration> hierarchy = new List<IBeanConfiguration>();
                    String missingBeanName = FillParentHierarchyIfValid(beanContextInit, beanConfiguration, hierarchy);

                    throw MaskBeanBasedException("Parent bean definition '" + missingBeanName + "' not found", beanConfiguration, null);
                }
            }
        }

        protected void InitializeAutowiring(BeanContextInit beanContextInit, IBeanConfiguration beanConfiguration, Object bean, Type beanType, IPropertyInfo[] propertyInfos,
            IISet<String> alreadySpecifiedPropertyNamesSet, IISet<String> ignoredPropertyNamesSet)
        {
            bool highPriorityBean = IsHighPriorityBean(bean);

            foreach (IPropertyInfo prop in propertyInfos)
            {
                String propertyName = prop.Name;
                if (!prop.IsWritable || alreadySpecifiedPropertyNamesSet.Contains(propertyName))
                {
                    // Property already explicitly specified. No
                    // autowiring necessary here
                    continue;
                }
                if (prop.GetAnnotation<Self>() != null)
                {
                    // Self-annotated properties are not considered for auto-wiring
                    continue;
                }
                if (ignoredPropertyNamesSet.Contains(propertyName))
                {
                    // Property marked as ignored. No autowiring wanted here
                    continue;
                }
                Type propertyType = prop.PropertyType;
                if (primitiveSet.Contains(propertyType) || (propertyType.IsArray && primitiveSet.Contains(propertyType.GetElementType())))
                {
                    continue;
                }
                AutowiredAttribute autowired = prop.GetAnnotation<AutowiredAttribute>();
                if (autowired == null && prop is FieldPropertyInfo)
                {
                    // Handle fields only if they are explicitly annotated
                    continue;
                }
                String beanName = autowired != null ? autowired.Value : null;
                if (beanName != null && beanName.Length == 0)
                {
                    beanName = null;
                }
                Object refBean = ResolveBean(beanName, propertyType, highPriorityBean, beanContextInit);
                if (refBean == null)
                {
                    if (autowired != null && !autowired.Optional)
                    {
                        throw MaskBeanBasedException("Could not resolve mandatory autowiring constraint on property '" + prop.Name + "'", beanConfiguration,
                                null);
                    }
                    continue;
                }
                prop.SetValue(bean, refBean);
            }
        }

        protected Object ResolveBean(String beanName, Type propertyType, bool isHighPriorityBean, BeanContextInit beanContextInit)
        {
            ServiceContext beanContext = beanContextInit.beanContext;
            ILinkedMap<Object, IBeanConfiguration> objectToBeanConfigurationMap = beanContextInit.objectToBeanConfigurationMap;
            // Module beans are only allowed to demand beans from the parent
            // context
            Object refBean = beanName != null ? beanContext.GetDirectBean(beanName) : beanContext.GetDirectBean(propertyType);
            if (refBean != null && objectToBeanConfigurationMap != null && objectToBeanConfigurationMap.ContainsKey(refBean))
            {
                InitializeBean(beanContextInit, refBean);
            }
            if (beanName != null)
            {
                return beanContext.GetServiceIntern(beanName, propertyType, isHighPriorityBean ? SearchType.PARENT : SearchType.CASCADE);
            }
            return beanContext.GetServiceIntern(propertyType, isHighPriorityBean ? SearchType.PARENT : SearchType.CASCADE);
        }

        protected void CallInitializingCallbacks(BeanContextInit beanContextInit, Object bean, bool joinLifecycle)
        {
            ServiceContext beanContext = beanContextInit.beanContext;
            IList<Object> initializedOrdering = beanContextInit.initializedOrdering;
            if (bean is IInitializingBean)
            {
                ((IInitializingBean)bean).AfterPropertiesSet();
            }
            if (bean is IPropertyLoadingBean)
            {
                ((IPropertyLoadingBean)bean).ApplyProperties(beanContextInit.properties);
            }
            if (bean is IInitializingModule)
            {
                ((IInitializingModule)bean).AfterPropertiesSet(beanContextInit.beanContextFactory);
            }
            if (bean is IBeanPreProcessor)
            {
                beanContext.AddPreProcessor((IBeanPreProcessor)bean);
            }
            if (bean is IBeanPostProcessor)
            {
                beanContext.AddPostProcessor((IBeanPostProcessor)bean);
            }
            if (bean is ILinkContainer)
            {
                beanContext.AddLinkContainer((ILinkContainer)bean);
                if (beanContext.IsRunning)
                {
                    ((ILinkContainer)bean).Link();
                }
            }
            if (joinLifecycle && bean is IDisposableBean)
            {
                beanContext.RegisterDisposable((IDisposableBean)bean);
            }
            if (initializedOrdering != null)
            {
                initializedOrdering.Add(bean);
            }
        }

        public void InitializeBeanIfNecessary(Object bean, BeanContextInit beanContextInit)
        {
            //beanContextInit.
        }

        public Object InitializeBean(ServiceContext beanContext, BeanContextFactory beanContextFactory, IBeanConfiguration beanConfiguration, Object bean,
                IList<IBeanConfiguration> beanConfHierarchy, bool joinLifecycle)
        {
            BeanContextInit currentBeanContextInit = currentBeanContextInitTL.Value;
            if (currentBeanContextInit == null)
            {
                currentBeanContextInit = new BeanContextInit();
                currentBeanContextInit.beanContext = beanContext;
                currentBeanContextInit.beanContextFactory = beanContextFactory;
                currentBeanContextInit.objectToBeanConfigurationMap = new IdentityLinkedMap<Object, IBeanConfiguration>();

                currentBeanContextInit.properties = beanContext.GetService<Properties>();
            }
            InitializeBean(currentBeanContextInit, beanConfiguration, bean, beanConfHierarchy, joinLifecycle);
            if (joinLifecycle && bean is IStartingBean)
            {
                ((IStartingBean)bean).AfterStarted();
            }
            return PostProcessBean(currentBeanContextInit, beanConfiguration, beanConfiguration.GetBeanType(), bean, beanConfHierarchy);
        }

        public void InitializeBean(BeanContextInit beanContextInit, Object bean)
        {
            IdentityLinkedMap<Object, IBeanConfiguration> objectToBeanConfigurationMap = beanContextInit.objectToBeanConfigurationMap;
            IBeanConfiguration beanConfiguration = objectToBeanConfigurationMap.Get(bean);
            objectToBeanConfigurationMap.Remove(bean);
            IISet<Object> allLifeCycledBeansSet = beanContextInit.allLifeCycledBeansSet;

            IList<IBeanConfiguration> beanConfHierarchy = new List<IBeanConfiguration>(3);
            if (FillParentHierarchyIfValid(beanContextInit, beanConfiguration, beanConfHierarchy) != null)
            {
                throw MaskBeanBasedException("Must never happen at this point", beanConfiguration, null);
            }
            allLifeCycledBeansSet.Add(bean);

            InitializeBean(beanContextInit, beanConfiguration, bean, beanConfHierarchy, true);
        }

        public void InitializeBean(BeanContextInit beanContextInit, IBeanConfiguration beanConfiguration, Object bean, IList<IBeanConfiguration> beanConfHierarchy,
            bool joinLifecycle)
        {
            if (!(bean is IInitializingModule) && !beanConfiguration.IsWithLifecycle())
            {
                if (bean is IPropertyLoadingBean)
                {
                    ((IPropertyLoadingBean)bean).ApplyProperties(beanContextInit.properties);
                }
                return;
            }
            ServiceContext beanContext = beanContextInit.beanContext;
            BeanContextFactory beanContextFactory = beanContextInit.beanContextFactory;
            IList<IBeanPreProcessor> preProcessors = beanContext.GetPreProcessors();

            IList<IPropertyConfiguration> propertyConfigurations = new List<IPropertyConfiguration>();
            IISet<String> ignoredPropertyNames = new De.Osthus.Ambeth.Collections.CHashSet<String>();
            IISet<String> alreadySpecifiedPropertyNamesSet = new De.Osthus.Ambeth.Collections.CHashSet<String>();
            try
            {
                Type beanType = ResolveTypeInHierarchy(beanConfHierarchy);
                ResolveAllBeanConfInHierarchy(beanConfHierarchy, propertyConfigurations);

                IPropertyInfo[] propertyInfos = PropertyInfoProvider.GetProperties(beanType);

                if (preProcessors != null)
                {
                    String beanName = beanConfiguration.GetName();
                    Properties properties = beanContextInit.properties;
                    for (int a = 0, size = preProcessors.Count; a < size; a++)
                    {
                        IBeanPreProcessor preProcessor = preProcessors[a];
                        preProcessor.PreProcessProperties(beanContextFactory, properties, beanName, bean, beanType, propertyConfigurations, propertyInfos);
                    }
                }
                InitializeDefining(beanContextInit, beanConfiguration, bean, beanType, propertyInfos, propertyConfigurations, alreadySpecifiedPropertyNamesSet);
                ResolveAllIgnoredPropertiesInHierarchy(beanConfHierarchy, beanType, ignoredPropertyNames);

                InitializeAutowiring(beanContextInit, beanConfiguration, bean, beanType, propertyInfos, alreadySpecifiedPropertyNamesSet, ignoredPropertyNames);
                CallInitializingCallbacks(beanContextInit, bean, joinLifecycle);
            }
            catch (Exception e)
            {
                throw MaskBeanBasedException(e, beanContextInit, beanConfiguration, null, bean);
            }
        }

        protected Exception CreateBeanContextDeclarationExceptionIfPossible(Exception e, IBeanConfiguration beanConfiguration,
            IPropertyConfiguration propertyConfiguration)
        {
            if (e is BeanContextDeclarationException || e is BeanContextInitException)
            {
                return e;
            }
            StackFrame[] declarationStackTrace = null;
            if (propertyConfiguration != null)
            {
                declarationStackTrace = propertyConfiguration.GetDeclarationStackTrace();
                if (declarationStackTrace == null)
                {
                    declarationStackTrace = propertyConfiguration.BeanConfiguration.GetDeclarationStackTrace();
                }
            }
            if (declarationStackTrace == null && beanConfiguration != null)
            {
                declarationStackTrace = beanConfiguration.GetDeclarationStackTrace();
            }
            if (declarationStackTrace == null)
            {
                return e;
            }
            if (e != null)
            {
                return new BeanContextDeclarationException(declarationStackTrace, e);
            }
            return new BeanContextDeclarationException(declarationStackTrace);
        }

        protected Exception MaskBeanBasedException(Exception e, BeanContextInit beanContextInit, IBeanConfiguration beanConfiguration,
            IPropertyConfiguration propertyConfiguration, Object bean)
        {
            StringBuilder sb = new StringBuilder();
            Type beanType = null;
            if (bean != null)
            {
                beanType = bean.GetType();
            }
            else
            {
                List<IBeanConfiguration> beanConfHierarchy = new List<IBeanConfiguration>();
                FillParentHierarchyIfValid(beanContextInit, beanConfiguration, beanConfHierarchy);
                beanType = ResolveTypeInHierarchy(beanConfHierarchy);
            }
            if (beanConfiguration.GetName() == null)
            {
                sb.Append("Error occured while handling anonymous bean of type ").Append(beanType != null ? beanType.FullName : "<unknown>");
            }
            else
            {
                sb.Append("Error occured while handling bean '").Append(beanConfiguration.GetName()).Append("' of type ")
                        .Append(beanType != null ? beanType.FullName : "<unknown>");
            }
            return MaskBeanBasedException(sb.ToString(), e, beanConfiguration, propertyConfiguration);
        }

        protected Exception MaskBeanBasedException(String message, IBeanConfiguration beanConfiguration, IPropertyConfiguration propertyConfiguration)
        {
            return MaskBeanBasedException(message, null, beanConfiguration, propertyConfiguration);
        }

        protected Exception MaskBeanBasedException(String message, Exception e, IBeanConfiguration beanConfiguration, IPropertyConfiguration propertyConfiguration)
        {
            e = CreateBeanContextDeclarationExceptionIfPossible(e, beanConfiguration, propertyConfiguration);

            StringBuilder sb = new StringBuilder();
            sb.Append(message);
            if (!(e is BeanContextInitException))
            {
                sb.Append("\r\n");
                BeanContextInitException beanContextInitException2 = new BeanContextInitException(sb.ToString(), RuntimeExceptionUtil.EMPTY_STACK_TRACE, e);
                return beanContextInitException2;
            }
            sb.Insert(0, "\r\n");
            sb.Insert(0, e.Message);
            BeanContextInitException beanContextInitException = new BeanContextInitException(sb.ToString(), e.StackTrace, e.InnerException);
            return beanContextInitException;
        }

        protected IPropertyInfo AutoResolveProperty(Type beanType, IPropertyConfiguration propertyConf, IISet<String> alreadySpecifiedPropertyNamesSet)
        {
            return AutoResolveAndSetPropertyIntern(null, beanType, null, propertyConf, null, null, alreadySpecifiedPropertyNamesSet);
        }

        protected void AutoResolveAndSetProperties(Object bean, Type beanType, IPropertyInfo[] properties, IPropertyConfiguration propertyConf, String beanName, Object refBean,
                IISet<String> alreadySpecifiedPropertyNamesSet)
        {
            AutoResolveAndSetPropertyIntern(bean, beanType, properties, propertyConf, beanName, refBean, alreadySpecifiedPropertyNamesSet);
        }

        protected IPropertyInfo AutoResolveAndSetPropertyIntern(Object bean, Type beanType, IPropertyInfo[] properties, IPropertyConfiguration propertyConf, String beanName,
                Object refBean, IISet<String> alreadySpecifiedPropertyNamesSet)
        {
            String propertyName = propertyConf.GetPropertyName();
            if (propertyName != null)
            {
                IPropertyInfo property = PropertyInfoProvider.GetProperty(beanType, propertyName);
                if (property == null)
                {
                    property = PropertyInfoProvider.GetProperty(beanType, Char.ToUpper(propertyName[0]) + propertyName.Substring(1));
                    if (property == null)
                    {
                        throw MaskBeanBasedException("Bean property " + beanType.FullName + "." + propertyName + " not found", null, propertyConf);
                    }
                }
                return property;
            }
            Type refBeanClass = refBean.GetType();
            bool atLeastOnePropertyFound = false;
            // Autoresolve property name by type of the requested bean
            for (int a = properties.Length; a-- > 0; )
            {
                IPropertyInfo property = properties[a];
                if (!property.IsWritable)
                {
                    continue;
                }
                if (alreadySpecifiedPropertyNamesSet.Contains(property.Name))
                {
                    // Ignore all already handled properties for potential
                    // autoresolving
                    continue;
                }
                if (!property.PropertyType.IsAssignableFrom(refBeanClass))
                {
                    continue;
                }
                // At this point the property WILL match and we intend to see this
                // as property found
                // even if it has already been matched (and done) by another
                // propertyRef-definition before
                try
                {
                    property.SetValue(bean, refBean);
                    atLeastOnePropertyFound = true;
                }
                catch (Exception e)
                {
                    throw MaskBeanBasedException("Propertyrefs did not work on type \"" + beanType + "\". Tried to set refbean \"" + refBean + "\" of type: \"" + refBeanClass + "\" to property \"" + propertyName + "\"", e, null, propertyConf);
                }
                alreadySpecifiedPropertyNamesSet.Add(property.Name);
            }
            if (!atLeastOnePropertyFound)
            {
                throw MaskBeanBasedException("Impossible autoresolve property scenario: There is no property which accepts a bean of type "
                        + refBeanClass.FullName + "' as represented by bean name '" + beanName + "'", null, propertyConf);
            }
            return null;
        }

        protected void InitializeDefining(BeanContextInit beanContextInit, IBeanConfiguration beanConfiguration, Object bean, Type beanType,
                IPropertyInfo[] propertyInfos, IList<IPropertyConfiguration> propertyConfigurations, IISet<String> alreadySpecifiedPropertyNamesSet)
        {
            for (int a = propertyConfigurations.Count; a-- > 0; )
            {
                IPropertyConfiguration propertyConf = propertyConfigurations[a];

                String refBeanName = propertyConf.GetBeanName();
                if (refBeanName == null)
                {
                    InitializePrimitive(beanContextInit, bean, beanType, propertyConf, alreadySpecifiedPropertyNamesSet);
                    continue;
                }
                InitializeRelation(beanContextInit, beanConfiguration, bean, beanType, propertyConf, propertyInfos, alreadySpecifiedPropertyNamesSet);
            }
        }

        protected void InitializePrimitive(BeanContextInit beanContextInit, Object bean, Type beanType, IPropertyConfiguration propertyConf,
                IISet<String> alreadySpecifiedPropertyNamesSet)
        {
            Object value = propertyConf.GetValue();
            IProperties properties = beanContextInit.properties;

            if (value is String)
            {
                value = properties.ResolvePropertyParts((String)value);

                if (value == null)
                {
                    throw MaskBeanBasedException("Environmental property '" + propertyConf.GetValue()
                            + "' could not be resolved while configuring bean property '" + propertyConf.GetPropertyName() + "'", null, propertyConf);
                }
            }
            IPropertyInfo primitiveProperty = AutoResolveProperty(beanType, propertyConf, alreadySpecifiedPropertyNamesSet);

            Object convertedValue = ConversionHelper.ConvertValueToType(primitiveProperty.PropertyType, value);

            if (!alreadySpecifiedPropertyNamesSet.Add(propertyConf.GetPropertyName()))
            {
                Log.Debug("Property '" + propertyConf.GetPropertyName() + "' already specified by higher priorized configuration. Ignoring setting property with value '"
                        + convertedValue + "'");
                return;
            }
            primitiveProperty.SetValue(bean, convertedValue);
        }

        protected bool IsHighPriorityBean(Object bean)
        {
            return IsHighPriorityBean(bean.GetType());
        }

        protected bool IsHighPriorityBean(Type beanType)
        {
            return GetPriorityOfBean(beanType) != 0;
        }

        protected int GetPriorityOfBean(Type beanType)
        {
            if (typeof(IPropertyLoadingBean).IsAssignableFrom(beanType))
            {
                return 3;
            }
            else if (typeof(IBeanPreProcessor).IsAssignableFrom(beanType) || typeof(IBeanPostProcessor).IsAssignableFrom(beanType))
            {
                return 2;
            }
            else if (typeof(IInitializingModule).IsAssignableFrom(beanType))
            {
                return 1;
            }
            return 0;
        }

        protected void InitializeRelation(BeanContextInit beanContextInit, IBeanConfiguration beanConfiguration, Object bean, Type beanType,
                IPropertyConfiguration propertyConf, IPropertyInfo[] propertyInfos, IISet<String> alreadySpecifiedPropertyNamesSet)
        {
            ServiceContext beanContext = beanContextInit.beanContext;
            IdentityLinkedMap<Object, IBeanConfiguration> objectToBeanConfigurationMap = beanContextInit.objectToBeanConfigurationMap;

            String refBeanName = propertyConf.GetBeanName();

            // Module beans are only allowed to demand beans from the parent
            // context
            Object refBean = beanContext.GetDirectBean(refBeanName);
            if (refBean != null && objectToBeanConfigurationMap != null && objectToBeanConfigurationMap.ContainsKey(refBean))
            {
                InitializeBean(beanContextInit, refBean);
            }
            refBean = beanContext.GetServiceIntern<Object>(refBeanName, IsHighPriorityBean(bean) ? SearchType.PARENT : SearchType.CASCADE);
            if (refBean == null)
            {
                if (propertyConf.IsOptional())
                {
                    return;
                }
                String message;
                if (propertyConf.GetPropertyName() != null)
                {
                    message = "Bean '" + refBeanName + "' not found to set bean property '" + propertyConf.GetPropertyName() + "'";
                }
                else
                {
                    message = "Bean '" + refBeanName + "' not found to look for autoresolve property";
                }
                throw MaskBeanBasedException(message, beanConfiguration, propertyConf);
            }
            IBeanConfiguration refBeanConfiguration = beanContextInit.objectToBeanConfigurationMap.Get(refBean);
            if (refBeanConfiguration != null)
            {
                // Object is not yet initialized. We try to do this before we use it
                InitializeBean(beanContextInit, refBean);
            }
            if (propertyConf.GetPropertyName() == null)
            {
                AutoResolveAndSetProperties(bean, beanType, propertyInfos, propertyConf, refBeanName, refBean, alreadySpecifiedPropertyNamesSet);
                return;
            }
            IPropertyInfo refProperty = AutoResolveProperty(beanType, propertyConf, alreadySpecifiedPropertyNamesSet);

            if (!alreadySpecifiedPropertyNamesSet.Add(refProperty.Name))
            {
                Log.Debug("Property '" + refProperty.Name
                        + "' already specified by higher priorized configuration. Ignoring setting property with ref to bean '" + refBeanName + "'");
                return;
            }
            if (!refProperty.PropertyType.IsAssignableFrom(refBean.GetType()))
            {
                throw MaskBeanBasedException("Impossible property scenario: Property '" + propertyConf.GetPropertyName() + "' does not accept a bean of type '"
                        + refBean.GetType().FullName + "' as represented by bean name '" + refBeanName + "'", beanConfiguration, propertyConf);
            }
            refProperty.SetValue(bean, refBean);
        }

        protected void ResolveAllBeanConfInHierarchy(IList<IBeanConfiguration> beanConfigurations, IList<IPropertyConfiguration> propertyConfs)
        {
            for (int a = 0, size = beanConfigurations.Count; a < size; a++)
            {
                IBeanConfiguration beanConfiguration = beanConfigurations[a];
                IList<IPropertyConfiguration> propertyConfigurations = beanConfiguration.GetPropertyConfigurations();
                if (propertyConfigurations != null)
                {
                    for (int b = 0, sizeB = propertyConfigurations.Count; b < sizeB; b++)
                    {
                        propertyConfs.Add(propertyConfigurations[b]);
                    }
                }
            }
        }

        protected void InstantiateBeans(BeanContextInit beanContextInit, IMap<String, IBeanConfiguration> nameToBeanConfigurationMap,
                IISet<IBeanConfiguration> alreadyHandledConfigsSet, bool highPriorityOnly)
        {
            BeanContextFactory beanContextFactory = beanContextInit.beanContextFactory;
            IList<IBeanConfiguration> beanConfigurations = beanContextFactory.GetBeanConfigurations();
            if (beanConfigurations == null || beanConfigurations.Count == 0)
            {
                return;
            }
            ServiceContext beanContext = beanContextInit.beanContext;
            IdentityLinkedMap<Object, IBeanConfiguration> objectToBeanConfigurationMap = beanContextInit.objectToBeanConfigurationMap;

            HashMap<int, OrderState> orderToHighBeanConfigurations = new HashMap<int, OrderState>();
            HashMap<int, OrderState> orderToLowBeanConfigurations = new HashMap<int, OrderState>();

            SortBeanConfigurations(beanContextInit, beanConfigurations, alreadyHandledConfigsSet, orderToHighBeanConfigurations,
                    orderToLowBeanConfigurations, highPriorityOnly);

            List<IBeanConfiguration> beanConfHierarchy = new List<IBeanConfiguration>();

            bool atLeastOneHandled = true;
            while (atLeastOneHandled)
            {
                atLeastOneHandled = false;

                while (true)
                {
                    BeanConfigState beanConfigState = ResolveNextPrecedenceBean(beanContextInit, orderToHighBeanConfigurations, orderToLowBeanConfigurations,
                            highPriorityOnly);
                    if (beanConfigState == null)
                    {
                        break;
                    }
                    IBeanConfiguration beanConfiguration = beanConfigState.GetBeanConfiguration();
                    Type beanType = beanConfigState.GetBeanType();
                    Object bean = null;
                    IISet<Type> allAutowireableTypes = new De.Osthus.Ambeth.Collections.CHashSet<Type>();
                    try
                    {
                        if (beanConfiguration is BeanConfiguration)
                        {
                            bean = beanConfiguration.GetInstance(beanType);
                        }
                        else if (beanConfiguration is BeanInstanceConfiguration)
                        {
                            bean = beanConfiguration.GetInstance();
                        }
                        else
                        {
                            throw MaskBeanBasedException("Instance of '" + beanConfiguration.GetType() + "' not supported here", beanConfiguration, null);
                        }
                        alreadyHandledConfigsSet.Add(beanConfiguration);
                        atLeastOneHandled = true;

                        if (!objectToBeanConfigurationMap.PutIfNotExists(bean, beanConfiguration))
                        {
                            throw MaskBeanBasedException("Bean instance " + bean + " registered twice.", beanConfiguration, null);
                        }
                        if (FillParentHierarchyIfValid(beanContextInit, beanConfiguration, beanConfHierarchy) != null)
                        {
                            throw MaskBeanBasedException("Bean configuration must be valid at this point", beanConfiguration, null);
                        }
                        bean = PostProcessBean(beanContextInit, beanConfiguration, beanType, bean, beanConfHierarchy);
                        beanConfHierarchy.Clear();

                        PublishNamesAndAliasesAndTypes(beanContextInit, beanConfiguration, bean);

                        IBeanConfiguration currBeanConf = beanConfiguration;
                        while (currBeanConf.GetParentName() != null)
                        {
                            IBeanConfiguration parentBeanConf = beanContext.GetBeanConfiguration(beanContextFactory, currBeanConf.GetParentName());

                            if (parentBeanConf == null)
                            {
                                throw MaskBeanBasedException("Parent bean with name '" + currBeanConf.GetParentName() + "' not found", beanConfiguration, null);
                            }
                            if (!parentBeanConf.IsAbstract())
                            {
                                // The parent bean definition is a valid bean by
                                // itself. So the parent hierarchy will not be
                                // handled here
                                break;
                            }
                            PublishNamesAndAliasesAndTypes(beanContextInit, parentBeanConf, bean);
                            currBeanConf = parentBeanConf;
                        }
                    }
                    catch (Exception e)
                    {
                        throw MaskBeanBasedException(e, beanContextInit, beanConfiguration, null, bean);
                    }
                }
            }
        }

        protected Object PostProcessBean(BeanContextInit beanContextInit, IBeanConfiguration beanConfiguration, Type beanType, Object bean,
            IList<IBeanConfiguration> beanConfHierarchy)
        {
            ServiceContext beanContext = beanContextInit.beanContext;
            BeanContextFactory beanContextFactory = beanContextInit.beanContextFactory;
            IList<IBeanPostProcessor> postProcessors = beanContext.GetPostProcessors();
            if (postProcessors == null)
            {
                return bean;
            }
            IISet<Type> allAutowireableTypes = new De.Osthus.Ambeth.Collections.CHashSet<Type>();
            ResolveAllAutowireableInterfacesInHierarchy(beanConfHierarchy, allAutowireableTypes);

            Type[] allInterfaces = bean.GetType().GetInterfaces();
            for (int b = allInterfaces.Length; b-- > 0; )
            {
                Type implementingInterface = allInterfaces[b];
                allAutowireableTypes.Add(implementingInterface);
            }
            // Do not manipulate the bean variable until all
            // postprocessors are called without failure
            Object currBean = bean;

            for (int b = 0, sizeB = postProcessors.Count; b < sizeB; b++)
            {
                IBeanPostProcessor postProcessor = postProcessors[b];
                try
                {
                    currBean = postProcessor.PostProcessBean(beanContextFactory, beanContext,
                            beanConfiguration, beanType, currBean, allAutowireableTypes);
                }
                catch (Exception e)
                {
                    throw MaskBeanBasedException("Error occured while post-processing with '" + postProcessor + "'", e, beanConfiguration, null);
                }
            }
            CallingProxyPostProcessor.BeanPostProcessed(beanContextFactory, beanContext, beanConfiguration, beanType, currBean, bean);
            return currBean;
        }

        protected void SortBeanConfigurations(BeanContextInit beanContextInit, IList<IBeanConfiguration> beanConfigurations,
            ISet<IBeanConfiguration> alreadyHandledConfigsSet,
            IMap<int, OrderState> orderToHighBeanConfigurations, IMap<int, OrderState> orderToLowBeanConfigurations, bool highPriorityOnly)
        {
            List<IBeanConfiguration> beanConfHierarchy = new List<IBeanConfiguration>();
            for (int a = 0, size = beanConfigurations.Count; a < size; a++)
            {
                IBeanConfiguration beanConfiguration = beanConfigurations[a];
                if (alreadyHandledConfigsSet.Contains(beanConfiguration))
                {
                    // Already handled so we do not bother anymore
                    continue;
                }
                if (beanConfiguration.IsAbstract())
                {
                    // Abstract bean configurations will not be instantiated -
                    // they are templates for other beans
                    alreadyHandledConfigsSet.Add(beanConfiguration);
                    continue;
                }
                beanConfHierarchy.Clear();
                if (FillParentHierarchyIfValid(beanContextInit, beanConfiguration, beanConfHierarchy) != null)
                {
                    // Something in the hierarchy is currently not valid
                    // Maybe with another module in this context the parent bean
                    // definitions can be resolved later
                    continue;
                }
                Type currentBeanType = ResolveTypeInHierarchy(beanConfHierarchy);
                bool highPriority = IsHighPriorityBean(currentBeanType);
                if (highPriorityOnly && !highPriority)
                {
                    continue;
                }
                IMap<int, OrderState> orderToBeanConfigurations = highPriority ? orderToHighBeanConfigurations : orderToLowBeanConfigurations;

                PrecedenceType currentPrecedenceType = beanConfiguration.GetPrecedence();
                int order = precedenceOrder.Get(currentPrecedenceType);

                OrderState list = orderToBeanConfigurations.Get(order);
                if (list == null)
                {
                    list = new OrderState();
                    orderToBeanConfigurations.Put(order, list);
                }
                list.Add(new BeanConfigState(beanConfiguration, currentBeanType));
            }
        }

        protected BeanConfigState ResolveNextPrecedenceBean(BeanContextInit beanContextInit, IMap<int, OrderState> orderToHighBeanConfigurations,
                IMap<int, OrderState> orderToLowBeanConfigurations, bool highPriorityOnly)
        {
            List<int> orders = new List<int>(orderToHighBeanConfigurations.KeySet());
            orders.Sort();
            for (int a = 0, size = orders.Count; a < size; a++)
            {
                OrderState list = orderToHighBeanConfigurations.Get(orders[a]);
                BeanConfigState beanConfigState = list.ConsumeBeanConfigState();
                if (beanConfigState != null)
                {
                    return beanConfigState;
                }
            }
            orders = new List<int>(orderToLowBeanConfigurations.KeySet());
            orders.Sort();
            for (int a = 0, size = orders.Count; a < size; a++)
            {
                OrderState list = orderToLowBeanConfigurations.Get(orders[a]);
                BeanConfigState beanConfigState = list.ConsumeBeanConfigState();
                if (beanConfigState != null)
                {
                    return beanConfigState;
                }
            }
            return null;
        }

        public IList<IBeanConfiguration> FillParentHierarchyIfValid(ServiceContext beanContext, BeanContextFactory beanContextFactory,
                IBeanConfiguration beanConfiguration)
        {
            BeanContextInit beanContextInit = new BeanContextInit();
            beanContextInit.beanContext = beanContext;
            beanContextInit.beanContextFactory = beanContextFactory;
            beanContextInit.properties = beanContext.GetService<Properties>();

            List<IBeanConfiguration> beanConfHierarchy = new List<IBeanConfiguration>();
            String missingBeanName = FillParentHierarchyIfValid(beanContextInit, beanConfiguration, beanConfHierarchy);
            if (missingBeanName == null)
            {
                return beanConfHierarchy;
            }
            throw MaskBeanBasedException("Illegal bean hierarchy: Bean '" + missingBeanName + "' not found", beanConfiguration, null);
        }

        public String FillParentHierarchyIfValid(BeanContextInit beanContextInit, IBeanConfiguration beanConfiguration, IList<IBeanConfiguration> targetBeanList)
        {
            targetBeanList.Add(beanConfiguration);
            IBeanConfiguration currBeanConf = beanConfiguration;
            while (currBeanConf.GetParentName() != null)
            {
                IBeanConfiguration parentBeanConf = beanContextInit.beanContext.GetBeanConfiguration(beanContextInit.beanContextFactory,
                        currBeanConf.GetParentName());

                if (parentBeanConf == null)
                {
                    targetBeanList.Clear();
                    return currBeanConf.GetParentName();
                }
                targetBeanList.Add(parentBeanConf);

                currBeanConf = parentBeanConf;
            }
            return null;
        }

        protected void PublishNamesAndAliasesAndTypes(BeanContextInit beanContextInit, IBeanConfiguration beanConfiguration, Object bean)
        {
            ServiceContext beanContext = beanContextInit.beanContext;
            BeanContextFactory beanContextFactory = beanContextInit.beanContextFactory;

            String beanName = beanConfiguration.GetName();
            if (beanName != null && beanName.Length > 0)
            {
                if (!beanConfiguration.IsAbstract())
                {
                    beanContext.AddNamedBean(beanName, bean);
                }
                IDictionary<String, IList<String>> beanNameToAliasesMap = beanContextFactory.GetBeanNameToAliasesMap();
                if (beanNameToAliasesMap != null)
                {
                    IList<String> aliasList = DictionaryExtension.ValueOrDefault(beanNameToAliasesMap, beanName);
                    if (aliasList != null)
                    {
                        for (int a = aliasList.Count; a-- > 0; )
                        {
                            String aliasName = aliasList[a];
                            beanContext.AddNamedBean(aliasName, bean);
                        }
                    }
                }
            }
            IList<Type> autowireableTypes = beanConfiguration.GetAutowireableTypes();
            if (autowireableTypes != null)
            {
                for (int autowireableIndex = autowireableTypes.Count; autowireableIndex-- > 0; )
                {
                    Type autowireableType = autowireableTypes[autowireableIndex];
                    beanContext.AddAutowiredBean(autowireableType, bean);
                }
            }
        }


        public Type ResolveTypeInHierarchy(IList<IBeanConfiguration> beanConfigurations)
        {
            for (int a = 0, size = beanConfigurations.Count; a < size; a++)
            {
                IBeanConfiguration beanConfiguration = beanConfigurations[a];
                Type type = beanConfiguration.GetBeanType();
                if (type != null)
                {
                    return type;
                }
            }
            return null;
        }

        protected void ResolveAllIgnoredPropertiesInHierarchy(IList<IBeanConfiguration> beanConfHierarchy, Type beanType, IISet<String> ignoredProperties)
        {
            IMap<String, IPropertyInfo> propertyMap = PropertyInfoProvider.GetPropertyMap(beanType);
            for (int a = 0, size = beanConfHierarchy.Count; a < size; a++)
            {
                IBeanConfiguration beanConfiguration = beanConfHierarchy[a];
                IList<String> ignoredPropertyNames = beanConfiguration.GetIgnoredPropertyNames();
                if (ignoredPropertyNames == null)
                {
                    continue;
                }
                for (int b = ignoredPropertyNames.Count; b-- > 0; )
                {
                    String ignoredPropertyName = ignoredPropertyNames[b];

                    if (!propertyMap.ContainsKey(ignoredPropertyName))
                    {
                        String uppercaseFirst = StringConversionHelper.UpperCaseFirst(ignoredPropertyName);
                        if (!propertyMap.ContainsKey(uppercaseFirst))
                        {
                            throw MaskBeanBasedException("Property '" + ignoredPropertyName
                                    + "' not found to ignore. This is only a check for consistency. However the following list of properties has been found: "
                                    + propertyMap.KeySet(), beanConfiguration, null);
                        }
                        ignoredPropertyName = uppercaseFirst;
                    }
                    ignoredProperties.Add(ignoredPropertyName);
                }
            }
        }

        protected void ResolveAllAutowireableInterfacesInHierarchy(IList<IBeanConfiguration> beanConfHierarchy, IISet<Type> autowireableInterfaces)
        {
            for (int a = 0, size = beanConfHierarchy.Count; a < size; a++)
            {
                IBeanConfiguration beanConfiguration = beanConfHierarchy[a];
                IList<Type> autowireableTypes = beanConfiguration.GetAutowireableTypes();
                if (autowireableTypes != null)
                {
                    autowireableInterfaces.AddAll(autowireableTypes);
                }
            }
        }
    }
}