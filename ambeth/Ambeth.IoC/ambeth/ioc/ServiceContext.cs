using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Hierarchy;
using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Ioc.Exceptions;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Ioc.Hierarchy;
using De.Osthus.Ambeth.Ioc.Link;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;
using System.Text;
using System.Threading;

namespace De.Osthus.Ambeth.Ioc
{
    public class ServiceContext : IServiceContext, IServiceContextIntern, IDisposable, IPrintable
    {
        public static Exception CreateDuplicateAutowireableException(Type autowireableType, Object bean1, Object bean2)
	    {
		    return new ArgumentException("A bean is already bound to type " + autowireableType.FullName + ".\nBean 1: " + bean1 + "\nBean 2: " + bean2);
	    }

	    public static Exception CreateDuplicateBeanNameException(String beanName, Object bean1, Object bean2)
	    {
		    return new ArgumentException("A bean is already bound to name " + beanName + ".\nBean 1: " + bean1 + "\nBean 2: " + bean2);
	    }

        protected static readonly Random rnd = new Random();

        protected LinkedHashMap<String, Object> nameToServiceDict;

        protected readonly HashMap<Type, Object> typeToServiceDict = new HashMap<Type, Object>();

        protected IList<ILinkContainer> linkContainers;

        protected List<Object> disposableObjects;

        protected IList<IBeanPreProcessor> preProcessors;
        protected IList<IBeanPostProcessor> postProcessors;

        protected bool disposed, running, disposing;

        protected bool failOnError;

        protected Lock readLock, writeLock;

        protected String toStringBackup;

        protected IServiceContextIntern parent;

        protected ISet<IServiceContext> children;

        protected BeanContextFactory beanContextFactory;

        protected String name;

        public ServiceContext(String name, IServiceContextIntern parent)
        {
            this.name = name;
            this.parent = parent;

            ReadWriteLock rwLock = new ReadWriteLock();
            readLock = rwLock.ReadLock;
            writeLock = rwLock.WriteLock;

            typeToServiceDict.Put(typeof(IServiceContext), this);
            typeToServiceDict.Put(typeof(IServiceContextIntern), this);
        }

        public String Name
        {
            get
            {
                return name;
            }
        }

        public IServiceContext GetParent()
        {
            CheckNotDisposed();
            return parent;
        }

        public IServiceContext GetRoot()
        {
            CheckNotDisposed();
            if (parent == null)
            {
                return this;
            }
            return parent.GetRoot();
        }

        public BeanContextFactory GetBeanContextFactory()
        {
            CheckNotDisposed();
            return beanContextFactory;
        }

        public void SetBeanContextFactory(BeanContextFactory beanContextFactory)
        {
            CheckNotDisposed();
            this.beanContextFactory = beanContextFactory;
        }

        public IList<IBeanPreProcessor> GetPreProcessors()
        {
            CheckNotDisposed();
            return preProcessors;
        }

        public IList<IBeanPostProcessor> GetPostProcessors()
        {
            CheckNotDisposed();
            return postProcessors;
        }

        public bool IsDisposed
        {
            get
            {
                return disposed;
            }
        }

        public bool IsRunning
        {
            get
            {
                return running;
            }
        }

        public void ChildContextDisposed(IServiceContext childContext)
        {
            if (children == null)
            {
                return;
            }
            writeLock.Lock();
            try
            {
                children.Remove(childContext);
            }
            finally
            {
                writeLock.Unlock();
            }
        }

        public void AddNamedBean(String beanName, Object bean)
        {
            CheckNotDisposed();
            CheckNotRunning();
            ParamChecker.AssertParamNotNull(beanName, "beanName");
            ParamChecker.AssertParamNotNull(bean, "bean");
            if (nameToServiceDict == null)
            {
                nameToServiceDict = new LinkedHashMap<String, Object>();
            }
            if (nameToServiceDict.ContainsKey(beanName))
            {
                throw CreateDuplicateBeanNameException(beanName, bean, nameToServiceDict.Get(beanName));
            }
            if (beanName.Contains("&") || beanName.Contains("*") || beanName.Contains(" ") || beanName.Contains("\t"))
            {
                throw new ArgumentException("Bean name '" + beanName + "'  must not contain any of the following characters: '&', '*' or any whitespace");
            }
            nameToServiceDict.Put(beanName, bean);
        }

        public void AddAutowiredBean(Type autowireableType, Object bean)
        {
            CheckNotDisposed();
            CheckNotRunning();
            ParamChecker.AssertParamNotNull(autowireableType, "autowireableType");
            ParamChecker.AssertParamNotNull(bean, "bean");

            if (!(bean is IFactoryBean))
            {
                // A type check makes no sense on factory beans

                if (!autowireableType.IsAssignableFrom(bean.GetType()))
                {
                    throw new InvalidCastException("Bean instance of type " + bean.GetType().FullName + " does not match to autowired type "
                            + autowireableType.FullName);
                }
            }
            if (!typeToServiceDict.PutIfNotExists(autowireableType, bean))
            {
                throw CreateDuplicateAutowireableException(autowireableType, bean, typeToServiceDict.Get(autowireableType));
            }
        }

        public IList<ILinkContainer> GetLinkContainers()
        {
            CheckNotDisposed();
            return linkContainers;
        }

        public void AddLinkContainer(ILinkContainer linkContainer)
        {
            CheckNotDisposed();
            writeLock.Lock();
            try
            {
                if (linkContainers == null)
                {
                    linkContainers = new List<ILinkContainer>();
                }
                linkContainers.Add(linkContainer);
            }
            finally
            {
                writeLock.Unlock();
            }
        }

        public void AddPreProcessor(IBeanPreProcessor preProcessor)
        {
            CheckNotDisposed();
            CheckNotRunning();
            if (preProcessors == null)
            {
                preProcessors = new List<IBeanPreProcessor>();
            }
            preProcessors.Add(preProcessor);
        }

        public void AddPostProcessor(IBeanPostProcessor postProcessor)
        {
            CheckNotDisposed();
            if (postProcessors == null)
            {
                postProcessors = new List<IBeanPostProcessor>();
            }
            PostProcessorOrder order = PostProcessorOrder.DEFAULT;
		    if (postProcessor is IOrderedBeanPostProcessor)
		    {
			    order = ((IOrderedBeanPostProcessor) postProcessor).GetOrder();
			    if (order == null)
			    {
				    order = PostProcessorOrder.DEFAULT;
			    }
		    }
		    bool added = false;
		    // Insert postprocessor at the correct order index
		    for (int a = postProcessors.Count; a-- > 0;)
		    {
			    IBeanPostProcessor existingPostProcessor = postProcessors[a];
			    PostProcessorOrder existingOrder = PostProcessorOrder.DEFAULT;
			    if (existingPostProcessor is IOrderedBeanPostProcessor)
			    {
				    existingOrder = ((IOrderedBeanPostProcessor) existingPostProcessor).GetOrder();
				    if (existingOrder == null)
				    {
					    existingOrder = PostProcessorOrder.DEFAULT;
				    }
			    }
			    if (existingOrder.Position >= order.Position)
			    {
				    // if same order then append directly behind the existing processor of that level
                    postProcessors.Insert(a + 1, postProcessor);
				    added = true;
				    break;
			    }
		    }
		    if (!added)
		    {
			    postProcessors.Insert(0, postProcessor);
		    }
        }

        public IBeanConfiguration GetBeanConfiguration(String beanName)
        {
            return GetBeanConfiguration(beanContextFactory, beanName);
        }

        public IBeanConfiguration GetBeanConfiguration(BeanContextFactory beanContextFactory, String beanName)
        {
            CheckNotDisposed();
            IBeanConfiguration beanConfiguration = beanContextFactory.GetBeanConfiguration(beanName);
            if (beanConfiguration == null && parent != null)
            {
                return parent.GetBeanConfiguration(beanName);
            }
            return beanConfiguration;
        }

        protected void CheckNotDisposed()
        {
            if (disposed)
            {
                throw new System.Exception(
                        "This bean context is already disposed. It might be a serious error that you still have running code referencing this instance");
            }
        }

        protected void CheckNotRunning()
        {
            if (running)
            {
                throw new System.Exception("This bean context is already running. It might be a serious error that you still want to configure this context");
            }
        }

        public void SetRunning()
        {
            running = true;
        }

        public void Dispose()
        {
            if (disposed || disposing)
            {
                return;
            }
            ILogger log;
            IServiceContext[] childrenCopy = null;
            writeLock.Lock();
            try
            {
                if (disposed || disposing)
                {
                    return;
                }
                log = GetService<ILoggerCache>().GetCachedLogger(this, typeof(ServiceContext));
                if (log.DebugEnabled)
                {
                    // Safe the toString-method for debugging purpose. Because this is not possible anymore if the context
                    // has been disposed and all bean-references have been cleared
                    toStringBackup = StringBuilderUtil.Concat(delegate(StringBuilder sb)
                    {
                        PrintContent(sb);
                    });
                }
                else
                {
                    toStringBackup = "n/a";
                }
                disposing = true;
                if (children != null && children.Count > 0)
                {
                    childrenCopy = new IServiceContext[children.Count];
                    children.CopyTo(childrenCopy, 0);
                    children.Clear();
                }
            }
            finally
            {
                writeLock.Unlock();
            }
            if (childrenCopy != null)
            {
                foreach (IServiceContext childContext in childrenCopy)
                {
                    try
                    {
                        childContext.Dispose();
                    }
                    catch (Exception e)
                    {
                        if (log.ErrorEnabled)
                        {
                            log.Error(e);
                        }
                    }
                }
            }
            writeLock.Lock();
            try
            {
                if (parent != null)
                {
                    parent.ChildContextDisposed(this);
                    parent = null;
                }
                if (this.linkContainers != null)
                {
                    IList<ILinkContainer> linkContainers = this.linkContainers;
                    this.linkContainers = null;
                    for (int a = linkContainers.Count; a-- > 0; )
                    {
                        ILinkContainer listenerContainer = linkContainers[a];
                        try
                        {
                            listenerContainer.Unlink();
                        }
                        catch (System.Exception e)
                        {
                            if (failOnError)
                            {
                                throw;
                            }
                            if (log.ErrorEnabled)
                            {
                                log.Error(e);
                            }
                        }
                    }
                }
                if (this.disposableObjects != null)
                {
                    IList<Object> disposableObjects = this.disposableObjects;
                    this.disposableObjects = null;
                    for (int a = disposableObjects.Count; a-- > 0; )
                    {
                        Object disposableObject = disposableObjects[a];
                        if (disposableObject is WeakReference)
                        {
                            disposableObject = ((WeakReference)disposableObject).Target;
                        }
                        if (disposableObject is IDisposableBean)
                        {
                            try
                            {
                                ((IDisposableBean)disposableObject).Destroy();
                            }
                            catch (System.Exception e)
                            {
                                if (failOnError)
                                {
                                    throw;
                                }
                                if (log.ErrorEnabled)
                                {
                                    log.Error(e);
                                }
                            }
                        }
                        else if (disposableObject is IDisposable)
                        {
                            try
                            {
                                ((IDisposable)disposableObject).Dispose();
                            }
                            catch (System.Exception e)
                            {
                                if (failOnError)
                                {
                                    throw;
                                }
                                if (log.ErrorEnabled)
                                {
                                    log.Error(e);
                                }
                            }
                        }
                        else if (disposableObject is IBackgroundWorkerParamDelegate<IServiceContext>)
                        {
                            try
                            {
                                ((IBackgroundWorkerParamDelegate<IServiceContext>)disposableObject).Invoke(this);
                            }
                            catch (System.Exception e)
                            {
                                if (failOnError)
                                {
                                    throw;
                                }
                                if (log.ErrorEnabled)
                                {
                                    log.Error(e);
                                }
                            }
                        }
                    }
                }

                if (nameToServiceDict != null)
                {
                    nameToServiceDict.Clear();
                }
                typeToServiceDict.Clear();
                if (postProcessors != null)
                {
                    postProcessors.Clear();
                }
                if (preProcessors != null)
                {
                    preProcessors.Clear();
                }
                beanContextFactory.Dispose();
            }
            finally
            {
                writeLock.Unlock();
                beanContextFactory = null;
                linkContainers = null;
                nameToServiceDict = null;
                postProcessors = null;
                preProcessors = null;
                parent = null;
                disposed = true;
                running = false;
            }
        }

        public IServiceContext CreateService(params Type[] serviceModuleTypes)
        {
            return CreateService(null, null, serviceModuleTypes);
        }

        public IServiceContext CreateService(String contextName, params Type[] serviceModuleTypes)
        {
            return CreateService(contextName, null, serviceModuleTypes);
        }

        public IServiceContext CreateService(IBackgroundWorkerParamDelegate<IBeanContextFactory> registerPhaseDelegate, params Type[] serviceModuleTypes)
        {
            return CreateService(null, registerPhaseDelegate, serviceModuleTypes);
        }

        public IServiceContext CreateService(String contextName, IBackgroundWorkerParamDelegate<IBeanContextFactory> registerPhaseDelegate, params Type[] serviceModuleTypes)
        {
            CheckNotDisposed();
            IBeanContextInitializer beanContextInitializer = RegisterBean<BeanContextInitializer>().Finish();

            if (contextName == null && registerPhaseDelegate == null && serviceModuleTypes.Length == 1)
            {
                contextName = serviceModuleTypes[0].Name;
            }
            BeanContextFactory childBeanContextFactory = beanContextFactory.CreateChildContextFactory(beanContextInitializer, this);
            IServiceContext childContext = childBeanContextFactory.Create(contextName, this, registerPhaseDelegate, serviceModuleTypes);

            writeLock.Lock();
            try
            {
                if (children == null)
                {
                    children = new IdentityHashSet<IServiceContext>();
                }
                children.Add(childContext);
            }
            finally
            {
                writeLock.Unlock();
            }
            return childContext;
        }

        public IBeanContextHolder<I> CreateService<I>(params Type[] serviceModuleTypes)
        {
            return CreateService<I>(null, null, serviceModuleTypes);
        }

        public IBeanContextHolder<I> CreateService<I>(String contextName, params Type[] serviceModuleTypes)
        {
            return CreateService<I>(contextName, null, serviceModuleTypes);
        }

        public IBeanContextHolder<I> CreateService<I>(IBackgroundWorkerParamDelegate<IBeanContextFactory> registerPhaseDelegate, params Type[] serviceModuleTypes)
        {
            return CreateService<I>(null, registerPhaseDelegate, serviceModuleTypes);
        }

        public IBeanContextHolder<I> CreateService<I>(String contextName, IBackgroundWorkerParamDelegate<IBeanContextFactory> registerPhaseDelegate, params Type[] serviceModuleTypes)
        {
            CheckNotDisposed();
            IBeanContextInitializer beanContextInitializer = RegisterBean<BeanContextInitializer>().Finish();

            if (contextName == null && registerPhaseDelegate == null && serviceModuleTypes.Length == 1)
            {
                contextName = serviceModuleTypes[0].Name;
            }
            BeanContextFactory childBeanContextFactory = beanContextFactory.CreateChildContextFactory(beanContextInitializer, this);
            IServiceContext childContext = childBeanContextFactory.Create(contextName, this, registerPhaseDelegate, serviceModuleTypes);

            writeLock.Lock();
            try
            {
                if (children == null)
                {
                    children = new IdentityHashSet<IServiceContext>();
                }
                children.Add(childContext);
            }
            finally
            {
                writeLock.Unlock();
            }
            return new BeanContextHolder<I>(childContext);
        }

        public IBeanContextHolder<I> CreateHolder<I>()
        {
            CheckNotDisposed();
            return new BeanContextHolder<I>(this);
        }

        public IBeanContextHolder CreateHolder(String beanName)
        {
            CheckNotDisposed();
            return new NamedBeanContextHolder(this, beanName);
        }

        public IBeanContextHolder<I> CreateHolder<I>(String beanName)
        {
            CheckNotDisposed();
            return new NamedBeanContextHolder<I>(this, beanName);
        }

        protected void HandleObjects(HandleObjectsDelegate handleObjectsDelegate)
        {
            IdentityHashSet<Object> alreadyHandledSet = new IdentityHashSet<Object>();
            foreach (Entry<Type, Object> entry in typeToServiceDict)
            {
                Object obj = entry.Value;
                if (alreadyHandledSet.Add(obj))
                {
                    handleObjectsDelegate.Invoke(obj);
                }
            }
            foreach (Entry<String, Object> entry in nameToServiceDict)
            {
                Object obj = entry.Value;
                if (alreadyHandledSet.Add(obj))
                {
                    handleObjectsDelegate.Invoke(obj);
                }
            }
        }

        public ILinkRegistryNeededRuntime Link(String listenerBeanName)
        {
            CheckNotDisposed();
            return beanContextFactory.Link(this, listenerBeanName);
        }

        public ILinkRegistryNeededRuntime Link(IBeanConfiguration listenerBean)
        {
            CheckNotDisposed();
            return beanContextFactory.Link(this, listenerBean);
        }

        public ILinkRegistryNeededRuntime<D> Link<D>(D listener)
        {
            CheckNotDisposed();
            return beanContextFactory.Link(this, listener);
        }

        [Obsolete]
        public void LinkToNamed<R>(String registryBeanName, String listenerBeanName)
        {
            LinkToNamed(registryBeanName, listenerBeanName, typeof(R));
        }

        [Obsolete]
        public void LinkToNamed(String registryBeanName, String listenerBeanName, Type registryClass)
        {
            CheckNotDisposed();
            beanContextFactory.Link(this, registryBeanName, listenerBeanName, registryClass);
        }

        [Obsolete]
        public void LinkToNamed<R>(String registryBeanName, String listenerBeanName, Object[] arguments)
        {
            LinkToNamed(registryBeanName, listenerBeanName, typeof(R), arguments);
        }

        [Obsolete]
        public void LinkToNamed(String registryBeanName, String listenerBeanName, Type registryClass, Object[] arguments)
        {
            CheckNotDisposed();
            beanContextFactory.Link(this, registryBeanName, listenerBeanName, registryClass, arguments);
        }

        [Obsolete]
        public void LinkToEvent<D>(String eventProviderBeanName, IEventDelegate<D> eventName, String listenerBeanName, String methodName)
        {
            CheckNotDisposed();
            beanContextFactory.LinkToEvent(this, eventProviderBeanName, eventName, listenerBeanName, methodName);
        }

        [Obsolete]
        public void LinkToEvent<D>(String eventProviderBeanName, IEventDelegate<D> eventName, String handlerDelegateBeanName)
        {
            CheckNotDisposed();
            beanContextFactory.LinkToEvent(this, eventProviderBeanName, eventName, handlerDelegateBeanName);
        }

        [Obsolete]
        public void LinkToEvent<D>(String eventProviderBeanName, IEventDelegate<D> eventName, D handlerDelegate)
        {
            CheckNotDisposed();
            beanContextFactory.LinkToEvent(this, eventProviderBeanName, eventName, handlerDelegate);
        }

        [Obsolete]
        public void LinkToEvent<R>(String eventProviderBeanName, String listenerBeanName, String methodName)
        {
            CheckNotDisposed();
            beanContextFactory.LinkToEvent<R>(this, eventProviderBeanName, listenerBeanName, methodName);
        }

        [Obsolete]
        public void LinkToEvent<R>(String eventName, String handlerDelegateBeanName)
        {
            CheckNotDisposed();
            beanContextFactory.LinkToEvent<R>(this, eventName, handlerDelegateBeanName);
        }

        [Obsolete]
        public void LinkToEvent<R>(String eventName, Delegate handlerDelegate)
        {
            CheckNotDisposed();
            beanContextFactory.LinkToEvent<R>(this, eventName, handlerDelegate);
        }

        [Obsolete]
        public void Link<R>(IBeanConfiguration listenerBean)
        {
            Link(listenerBean, typeof(R));
        }

        [Obsolete]
        public void Link(IBeanConfiguration listenerBean, Type autowiredRegistryClass)
        {
            CheckNotDisposed();
            beanContextFactory.Link(this, listenerBean, autowiredRegistryClass);
        }

        [Obsolete]
        public void Link<R>(IBeanConfiguration listenerBean, Object[] arguments)
        {
            Link(listenerBean, typeof(R), arguments);
        }

        [Obsolete]
        public void Link(IBeanConfiguration listenerBean, Type autowiredRegistryClass, Object[] arguments)
        {
            CheckNotDisposed();
            beanContextFactory.Link(this, listenerBean, autowiredRegistryClass, arguments);
        }

        [Obsolete]
        public void Link<R>(String listenerBeanName)
        {
            Link(listenerBeanName, typeof(R));
        }

        [Obsolete]
        public void Link(String listenerBeanName, Type autowiredRegistryClass)
        {
            CheckNotDisposed();
            beanContextFactory.Link(this, listenerBeanName, autowiredRegistryClass);
        }

        [Obsolete]
        public void Link<R>(String listenerBeanName, Object[] arguments)
        {
            Link(listenerBeanName, typeof(R), arguments);
        }

        [Obsolete]
        public void Link(String listenerBeanName, Type autowiredRegistryClass, Object[] arguments)
        {
            CheckNotDisposed();
            beanContextFactory.Link(this, listenerBeanName, autowiredRegistryClass, arguments);
        }

        public void RegisterDisposable(IDisposableBean disposableBean)
        {
            RegisterDisposableIntern(disposableBean, true);
        }

        public void RegisterDisposeHook(IBackgroundWorkerParamDelegate<IServiceContext> waitCallback)
        {
            RegisterDisposableIntern(waitCallback, false);
        }

        public void AddDisposables(IList<Object> disposableObjects)
        {
            if (this.disposableObjects == null)
            {
                this.disposableObjects = new List<Object>(disposableObjects.Count);
            }
            this.disposableObjects.AddRange(disposableObjects);
        }

        protected void RegisterDisposableIntern(Object obj, bool registerWeakOnRunning)
	    {
		    CheckNotDisposed();
		    ParamChecker.AssertParamNotNull(obj, "obj");
		    if (!IsRunning)
		    {
			    if (disposableObjects == null)
			    {
				    disposableObjects = new List<Object>();
			    }
			    disposableObjects.Add(obj);
                return;
		    }
		    Lock writeLock = this.writeLock;
		    writeLock.Lock();
		    try
		    {
			    List<Object> disposableObjects = this.disposableObjects;
			    if (disposableObjects == null)
			    {
				    disposableObjects = new List<Object>();
				    this.disposableObjects = disposableObjects;
			    }
			    // "monte carlo" approach to check for disposable objects without noticeable impact on the runtime performance
			    while (disposableObjects.Count > 0)
			    {
				    int randomIndex = rnd.Next(disposableObjects.Count);
				    Object disposableObject = disposableObjects[randomIndex];
				    if (disposableObject is WeakReference)
				    {
					    disposableObject = ((WeakReference) disposableObject).Target;
				    }
				    if (disposableObject != null)
				    {
					    // not a collected object. we finish the search for collected disposables
					    break;
				    }
				    disposableObjects.RemoveAt(randomIndex);
			    }
			    disposableObjects.Add(registerWeakOnRunning ? new WeakReference(obj) : obj);
		    }
		    finally
		    {
			    writeLock.Unlock();
		    }
	    }

        public I GetService<I>()
        {
            return GetService<I>(true);
        }


        public I GetService<I>(bool checkExistence)
        {
            CheckNotDisposed();
            readLock.Lock();
            try
            {
                Object service = GetServiceIntern(typeof(I), SearchType.CASCADE);
                if (service == null && checkExistence)
                {
                    throw new System.Exception("No bean autowired to type '" + typeof(I).FullName + "'");
                }
                return (I)service;
            }
            finally
            {
                readLock.Unlock();
            }
        }

        public I GetServiceIntern<I>(SearchType searchType)
        {
            return (I)GetServiceIntern(typeof(I), searchType);
        }

        public Object GetServiceIntern(Type autowiredType, SearchType searchType)
        {
            Object service = null;
            if (!SearchType.PARENT.Equals(searchType))
            {
                service = typeToServiceDict.Get(autowiredType);
            }
            if (service is IFactoryBean)
            {
                Object factoryResult = ((IFactoryBean)service).GetObject();
                if (factoryResult == null)
                {
                    throw new BeanContextInitException("Anonymous factory bean of type " + service.GetType().FullName + " returned null for service type "
                            + autowiredType.FullName + ". Possibly a cyclic relationship from the factory to its cascaded dependencies and back");
                }
                service = factoryResult;
            }
            else if (service == null && parent != null && !SearchType.CURRENT.Equals(searchType))
            {
                return parent.GetService(autowiredType, false);
            }
            return service;
        }

        public Object GetDirectBean(Type serviceType)
        {
            CheckNotDisposed();
            return typeToServiceDict.Get(serviceType);
        }

        public Object GetDirectBean(String beanName)
        {
            CheckNotDisposed();
            if (nameToServiceDict == null)
            {
                return null;
            }
            return nameToServiceDict.Get(beanName);
        }

        public I GetServiceIntern<I>(String serviceName, SearchType searchType)
        {
            return (I) GetServiceIntern(serviceName, typeof(I), searchType);
        }

        public Object GetServiceIntern(String serviceName, Type serviceType, SearchType searchType)
        {
            String realServiceName = serviceName;
            bool factoryContentRequest = true, parentOnlyRequest = false;
            while (true)
            {
                if (realServiceName[0] == '&')
                {
                    realServiceName = realServiceName.Substring(1);
                    factoryContentRequest = false;
                    continue;
                }
                else if (realServiceName[0] == '*')
                {
                    realServiceName = realServiceName.Substring(1);
                    parentOnlyRequest = true;
                    continue;
                }
                // No escape character found, realServiceName is now resolved
                break;
            }
            if (realServiceName.Length == 0)
            {
                throw new ArgumentException("Bean name '" + serviceName + "' not valid");
            }
            IDictionary<String, String> beanNameToAliasesMap = beanContextFactory.GetAliasToBeanNameMap();
            if (beanNameToAliasesMap != null)
            {
                String realBeanName = DictionaryExtension.ValueOrDefault(beanNameToAliasesMap, realServiceName);
                if (realBeanName != null)
                {
                    realServiceName = realBeanName;
                    serviceName = (factoryContentRequest ? "" : "&") + (parentOnlyRequest ? "*" : "") + realBeanName;
                }
            }
            Object service = null;
            if (!parentOnlyRequest && !SearchType.PARENT.Equals(searchType) && nameToServiceDict != null)
            {
                service = nameToServiceDict.Get(realServiceName);
            }
            if (service is IFactoryBean && factoryContentRequest)
            {
                Object factoryResult = ((IFactoryBean)service).GetObject();
                if (factoryResult == null)
                {
                    throw new BeanContextInitException("Factory bean '" + serviceName + "' of type " + service.GetType().FullName
                            + " returned null for service type " + serviceType.FullName
                            + ". Possibly a cyclic relationship from the factory to its cascaded dependencies and back");
                }
                service = factoryResult;
            }
            else if (service == null && parent != null)
            {
                if (parentOnlyRequest)
                {
                    // Reconstruct factory bean prefix if necessary
                    return parent.GetService(factoryContentRequest ? "&" + realServiceName : realServiceName, false);
                }
                else if (!SearchType.CURRENT.Equals(searchType))
                {
                    return parent.GetService(serviceName, false);
                }
            }
            if (service != null && !serviceType.IsAssignableFrom(service.GetType()))
            {
                throw new Exception("Bean with name '" + serviceName + "' not assignable to type '" + serviceType.FullName + "'");
            }
            return service;
        }

        public Object GetService(Type autowiredType)
        {
            return GetService(autowiredType, true);
        }

        public Object GetService(Type autowiredType, bool checkExistence)
        {
            CheckNotDisposed();
            readLock.Lock();
            try
            {
                ParamChecker.AssertParamNotNull(autowiredType, "autowiredType");
                Object service = GetServiceIntern(autowiredType, SearchType.CASCADE);
                if (service == null && checkExistence)
                {
                    throw new System.Exception("No bean found autowired to type '" + autowiredType.FullName + "'");
                }
                return service;
            }
            finally
            {
                readLock.Unlock();
            }
        }

        public Object GetService(String serviceName)
        {
            return GetService(serviceName, true);
        }

        public Object GetService(String serviceName, bool checkExistence)
        {
            CheckNotDisposed();
            readLock.Lock();
            try
            {
                ParamChecker.AssertParamNotNull(serviceName, "serviceName");
                Object service = GetServiceIntern<Object>(serviceName, SearchType.CASCADE);
                if (service == null && checkExistence)
                {
                    throw new Exception("No bean found with name '" + serviceName + "'");
                }
                return service;
            }
            finally
            {
                readLock.Unlock();
            }
        }

        public V GetService<V>(String serviceName)
        {
            return GetService<V>(serviceName, true);
        }

        public V GetService<V>(String serviceName, bool checkExistence)
        {
            CheckNotDisposed();
            readLock.Lock();
            try
            {
                if (String.IsNullOrEmpty(serviceName))
                {
                    throw new Exception("Tried to get a bean with empty name. This is not allowed.");
                }
                V service = GetServiceIntern<V>(serviceName, SearchType.CASCADE);
                if (service == null && checkExistence)
                {
                    throw new Exception("No bean found with name '" + serviceName + "'");
                }
                return service;
            }
            finally
            {
                readLock.Unlock();
            }
        }

        public void PrintContent(StringBuilder sb)
        {
            sb.Append("n/a");
            //if (disposed)
            //{
            //    sb.Append(toStringBackup);
            //    return;
            //}
            //readLock.Lock();
            //try
            //{
            //    sb.Append("Named content (").Append(nameToServiceDict != null ? nameToServiceDict.Count : 0).Append("): [");
            //    if (nameToServiceDict != null)
            //    {
            //        List<String> list = new List<String>();
            //        foreach (KeyValuePair<String, Object> entry in nameToServiceDict)
            //        {
            //            list.Add(entry.Key);
            //        }
            //        list.Sort();

            //        bool first = true;

            //        for (int a = 0, size = list.Count; a < size; a++)
            //        {
            //            if (first)
            //            {
            //                first = false;
            //            }
            //            else
            //            {
            //                sb.Append(',');
            //            }
            //            sb.Append(list[a]);
            //        }
            //    }
            //    sb.AppendLine("]").Append("Autowired content (")
            //            .Append(typeToServiceDict.Count).Append("): [");
            //    List<Type> typeList = new List<Type>();

            //    foreach (KeyValuePair<Type, Object> entry in typeToServiceDict)
            //    {
            //        typeList.Add(entry.Key);
            //    }
            //    typeList.Sort(delegate(Type left, Type right)
            //    {
            //        return left.FullName.CompareTo(right.FullName);
            //    });
            //    bool typeFirst = true;
            //    for (int a = 0, size = typeList.Count; a < size; a++)
            //    {
            //        if (typeFirst)
            //        {
            //            typeFirst = false;
            //        }
            //        else
            //        {
            //            sb.Append(',');
            //        }
            //        sb.Append(typeList[a].FullName);
            //    }
            //}
            //finally
            //{
            //    readLock.Unlock();
            //}
            //if (parent != null)
            //{
            //    sb.AppendLine();
            //    sb.AppendLine("Parent");
            //    parent.PrintContent(sb);
            //}
        }


        public IList<T> GetObjects<T>()
        {
            CheckNotDisposed();

            IdentityHashSet<T> result = new IdentityHashSet<T>();

            IList<T> parentResult = parent != null ? parent.GetObjects<T>() : null;
            if (parentResult != null)
            {
                foreach (T parentItem in parentResult)
                {
                    result.Add(parentItem);
                }
            }
            readLock.Lock();
            try
            {
                HandleObjects(delegate(Object obj)
                {
                    if (typeof(T).IsAssignableFrom(obj.GetType()))
                    {
                        result.Add((T)obj);
                    }
                });
                return ListUtil.ToList(result);
            }
            finally
            {
                readLock.Unlock();
            }
        }

        public IList<T> GetImplementingObjects<T>()
        {
            IdentityHashSet<T> set = new IdentityHashSet<T>();
            Lock readLock = this.readLock;
            readLock.Lock();
            try
            {
                HandleObjects(delegate(Object obj)
                {
                    if (typeof(T).IsAssignableFrom(obj.GetType()))
                    {
                        set.Add((T)obj);
                    }
                });
                return ListUtil.ToList(set);
            }
            finally
            {
                readLock.Unlock();
            }
        }

        [Obsolete]
        public IBeanRuntime<V> RegisterAnonymousBean<V>()
        {
            return RegisterBean<V>();
        }

        [Obsolete]
        public IBeanRuntime<Object> RegisterAnonymousBean<Object>(Type type)
        {
            return RegisterBean<Object>(type);
        }

        public IBeanRuntime<V> RegisterBean<V>()
        {
            CheckNotDisposed();
            return new BeanRuntime<V>(this, typeof(V), true);
        }

        public IBeanRuntime<Object> RegisterBean<Object>(Type type)
        {
            CheckNotDisposed();
            ParamChecker.AssertParamNotNull(type, "type");
            return new BeanRuntime<Object>(this, type, true);
        }

        public IBeanRuntime<V> RegisterWithLifecycle<V>(V obj)
        {
            CheckNotDisposed();
            ParamChecker.AssertParamNotNull(obj, "obj");
            return new BeanRuntime<V>(this, obj, true);
        }

        public IBeanRuntime<V> RegisterExternalBean<V>(V externalBean)
        {
            CheckNotDisposed();
            ParamChecker.AssertParamNotNull(externalBean, "externalBean");
            return new BeanRuntime<V>(this, externalBean, false);
        }


        public override String ToString()
        {
            if (disposed)
            {
                return toStringBackup;
            }
            StringBuilder sb = new StringBuilder();
            ToString(sb);
            return sb.ToString();
        }

        public void ToString(StringBuilder sb)
        {
            PrintContent(sb);
        }
    }
}