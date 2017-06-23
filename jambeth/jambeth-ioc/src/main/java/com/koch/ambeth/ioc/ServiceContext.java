package com.koch.ambeth.ioc;

/*-
 * #%L
 * jambeth-ioc
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.koch.ambeth.ioc.config.BeanRuntime;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.exception.BeanContextInitException;
import com.koch.ambeth.ioc.factory.BeanContextFactory;
import com.koch.ambeth.ioc.factory.BeanContextInitializer;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.ioc.factory.IBeanContextInitializer;
import com.koch.ambeth.ioc.hierarchy.IBeanContextHolder;
import com.koch.ambeth.ioc.hierarchy.SearchType;
import com.koch.ambeth.ioc.link.ILinkContainer;
import com.koch.ambeth.ioc.link.ILinkRegistryNeededRuntime;
import com.koch.ambeth.ioc.log.ILoggerCache;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.util.IDisposable;
import com.koch.ambeth.util.IPrintable;
import com.koch.ambeth.util.ListUtil;
import com.koch.ambeth.util.Lock;
import com.koch.ambeth.util.LockState;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.ReadWriteLock;
import com.koch.ambeth.util.StringBuilderUtil;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.collections.IdentityHashSet;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.objectcollector.IObjectCollector;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;
import com.koch.ambeth.util.typeinfo.ITypeInfo;
import com.koch.ambeth.util.typeinfo.ITypeInfoProvider;

public class ServiceContext
		implements IServiceContext, IServiceContextIntern, IDisposable, IPrintable {
	public static class SimpleClassNameComparator implements Comparator<Class<?>> {
		private final ITypeInfoProvider typeInfoProvider;

		public SimpleClassNameComparator(ITypeInfoProvider typeInfoProvider) {
			this.typeInfoProvider = typeInfoProvider;
		}

		@Override
		public int compare(Class<?> o1, Class<?> o2) {
			ITypeInfo t1 = typeInfoProvider.getTypeInfo(o1);
			ITypeInfo t2 = typeInfoProvider.getTypeInfo(o2);
			return t1.getSimpleName().compareTo(t2.getSimpleName());
		}
	}

	public static RuntimeException createDuplicateAutowireableException(Class<?> autowireableType,
			Object bean1, Object bean2) {
		return new IllegalArgumentException("A bean is already bound to type "
				+ autowireableType.getName() + ".\nBean 1: " + bean1 + "\nBean 2: " + bean2);
	}

	public static RuntimeException createDuplicateBeanNameException(String beanName, Object bean1,
			Object bean2) {
		return new IllegalArgumentException("A bean is already bound to name " + beanName
				+ ".\nBean 1: " + bean1 + "\nBean 2: " + bean2);
	}

	protected LinkedHashMap<String, Object> nameToServiceDict;

	protected LinkedHashMap<Class<?>, Object> typeToServiceDict;

	protected IList<ILinkContainer> linkContainers;

	protected ArrayList<Object> disposableObjects;

	protected IList<IBeanPreProcessor> preProcessors;

	protected IList<IBeanPostProcessor> postProcessors;

	protected List<IBeanInstantiationProcessor> instantiationProcessors;

	protected boolean disposed, running, disposing;

	protected boolean failOnError;

	protected final Lock readLock, writeLock;

	protected String toStringBackup;

	protected IExternalServiceContext externalServiceContext;

	protected IServiceContextIntern parent;

	protected Set<IServiceContext> children;

	protected IObjectCollector objectCollector;

	protected BeanContextFactory beanContextFactory;

	protected ITypeInfoProvider typeInfoProvider;

	protected String name;

	public ServiceContext(String name, IObjectCollector objectCollector,
			IExternalServiceContext externalServiceContext) {
		this.name = name;
		this.externalServiceContext = externalServiceContext;
		ParamChecker.assertNotNull(objectCollector, "objectCollector");

		this.objectCollector = objectCollector;

		ReadWriteLock rwLock = new ReadWriteLock();
		readLock = rwLock.getReadLock();
		writeLock = rwLock.getWriteLock();

		typeToServiceDict = new LinkedHashMap<>();
		typeToServiceDict.put(IServiceContext.class, this);
		typeToServiceDict.put(IServiceContextIntern.class, this);
	}

	public ServiceContext(String name, IServiceContextIntern parent) {
		this.name = name;
		ParamChecker.assertNotNull(parent, "parent");
		this.parent = parent;

		objectCollector = parent.getService(IThreadLocalObjectCollector.class);

		ParamChecker.assertNotNull(objectCollector, "objectCollector");

		ReadWriteLock rwLock = new ReadWriteLock();
		readLock = rwLock.getReadLock();
		writeLock = rwLock.getWriteLock();

		typeToServiceDict = new LinkedHashMap<>();
		typeToServiceDict.put(IServiceContext.class, this);
		typeToServiceDict.put(IServiceContextIntern.class, this);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public IServiceContext getParent() {
		checkNotDisposed();
		return parent;
	}

	@Override
	public IServiceContext getRoot() {
		checkNotDisposed();
		if (parent == null) {
			return this;
		}
		return parent.getRoot();
	}

	public IExternalServiceContext getExternalServiceContext() {
		checkNotDisposed();
		return externalServiceContext;
	}

	public BeanContextFactory getBeanContextFactory() {
		checkNotDisposed();
		return beanContextFactory;
	}

	public void setBeanContextFactory(BeanContextFactory beanContextFactory) {
		checkNotDisposed();
		this.beanContextFactory = beanContextFactory;
	}

	public List<IBeanPreProcessor> getPreProcessors() {
		checkNotDisposed();
		return preProcessors;
	}

	public List<IBeanPostProcessor> getPostProcessors() {
		checkNotDisposed();
		return postProcessors;
	}

	public List<IBeanInstantiationProcessor> getInstantiationProcessors() {
		checkNotDisposed();
		return instantiationProcessors;
	}

	@Override
	public boolean isDisposed() {
		return disposed;
	}

	@Override
	public boolean isDisposing() {
		return disposing;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	public void addNamedBean(String beanName, Object bean) {
		checkNotDisposed();
		checkNotRunning();
		ParamChecker.assertParamNotNull(beanName, "beanName");
		ParamChecker.assertParamNotNull(bean, "bean");
		if (nameToServiceDict == null) {
			nameToServiceDict = new LinkedHashMap<>();
		}
		if (nameToServiceDict.containsKey(beanName)) {
			throw createDuplicateBeanNameException(beanName, bean, nameToServiceDict.get(beanName));
		}
		if (beanName.contains("&") || beanName.contains("*") || beanName.contains(" ")
				|| beanName.contains("\t")) {
			throw new IllegalArgumentException("Bean name '" + beanName
					+ "'  must not contain any of the following characters: '&', '*' or any whitespace");
		}
		nameToServiceDict.put(beanName, bean);
	}

	public void addAutowiredBean(Class<?> autowireableType, Object bean) {
		checkNotDisposed();
		checkNotRunning();
		ParamChecker.assertParamNotNull(autowireableType, "autowireableType");
		ParamChecker.assertParamNotNull(bean, "bean");

		if (!(bean instanceof IFactoryBean)) {
			// A type check makes no sense on factory beans

			if (!autowireableType.isAssignableFrom(bean.getClass())) {
				throw new ClassCastException("Bean instance of type " + bean.getClass().getName()
						+ " does not match to autowired type " + autowireableType.getName());
			}
		}
		if (!typeToServiceDict.putIfNotExists(autowireableType, bean)) {
			throw createDuplicateAutowireableException(autowireableType, bean,
					typeToServiceDict.get(autowireableType));
		}
	}

	public List<ILinkContainer> getLinkContainers() {
		checkNotDisposed();
		return linkContainers;
	}

	public void addLinkContainer(ILinkContainer linkContainer) {
		checkNotDisposed();
		writeLock.lock();
		try {
			if (linkContainers == null) {
				linkContainers = new ArrayList<>();
			}
			linkContainers.add(linkContainer);
		}
		finally {
			writeLock.unlock();
		}
	}

	public void addPreProcessor(IBeanPreProcessor preProcessor) {
		checkNotDisposed();
		if (preProcessors == null) {
			preProcessors = new ArrayList<>();
		}
		addOrderedProcessor(preProcessor, preProcessors);
	}

	public void addPostProcessor(IBeanPostProcessor postProcessor) {
		checkNotDisposed();
		if (postProcessors == null) {
			postProcessors = new ArrayList<>();
		}
		addOrderedProcessor(postProcessor, postProcessors);
	}

	public void addInstantiationProcessor(IBeanInstantiationProcessor instantiationProcessor) {
		checkNotDisposed();
		if (instantiationProcessors == null) {
			instantiationProcessors = new ArrayList<>();
		}
		addOrderedProcessor(instantiationProcessor, instantiationProcessors);
	}

	protected <T> void addOrderedProcessor(T processor, List<T> processors) {
		ProcessorOrder order = ProcessorOrder.DEFAULT;
		if (processor instanceof IOrderedBeanProcessor) {
			order = ((IOrderedBeanProcessor) processor).getOrder();
			if (order == null) {
				order = ProcessorOrder.DEFAULT;
			}
		}
		boolean added = false;
		// Insert postprocessor at the correct order index
		for (int a = processors.size(); a-- > 0;) {
			T existingPostProcessor = processors.get(a);
			ProcessorOrder existingOrder = ProcessorOrder.DEFAULT;
			if (existingPostProcessor instanceof IOrderedBeanProcessor) {
				existingOrder = ((IOrderedBeanProcessor) existingPostProcessor).getOrder();
				if (existingOrder == null) {
					existingOrder = ProcessorOrder.DEFAULT;
				}
			}
			if (existingOrder.getPosition() >= order.getPosition()) {
				// if same order then append directly behind the existing processor of that level
				processors.add(a + 1, processor);
				added = true;
				break;
			}
		}
		if (!added) {
			processors.add(0, processor);
		}
	}

	@Override
	public IBeanConfiguration getBeanConfiguration(String beanName) {
		return getBeanConfiguration(beanContextFactory, beanName);
	}

	public IBeanConfiguration getBeanConfiguration(BeanContextFactory beanContextFactory,
			String beanName) {
		checkNotDisposed();
		IBeanConfiguration beanConfiguration = beanContextFactory.getBeanConfiguration(beanName);
		if (beanConfiguration == null && parent != null) {
			return parent.getBeanConfiguration(beanName);
		}
		return beanConfiguration;
	}

	protected void checkNotDisposed() {
		if (disposed) {
			throw new IllegalStateException(
					"This bean context is already disposed. It might be a serious error that you still have running code referencing this instance");
		}
	}

	protected void checkNotRunning() {
		if (running) {
			throw new IllegalStateException(
					"This bean context is already running. It might be a serious error that you still want to configure this context");
		}
	}

	public void setRunning() {
		running = true;
	}

	@Override
	public void close() throws Exception {
		dispose();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void dispose() {
		if (disposed || disposing) {
			return;
		}
		ILogger log;
		IServiceContext[] childrenCopy = null;
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try {
			if (disposed || disposing) {
				return;
			}
			log = getService(ILoggerCache.class).getCachedLogger(this, ServiceContext.class);
			if (log.isDebugEnabled()) {
				// Safe the toString-method for debugging purpose. Because this is not possible anymore if
				// the context
				// has been disposed and all bean-references have been cleared
				IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
				StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
				printContent(sb);
				toStringBackup = sb.toString();
				tlObjectCollector.dispose(sb);
				sb = null;
			}
			else {
				toStringBackup = "n/a";
			}
			disposing = true;
			if (children != null && !children.isEmpty()) {
				childrenCopy = children.toArray(new IServiceContext[children.size()]);
				children.clear();
				children = null;
			}
		}
		finally {
			writeLock.unlock();
		}
		if (childrenCopy != null) {
			for (int a = childrenCopy.length; a-- > 0;) {
				IServiceContext childContext = childrenCopy[a];
				try {
					childContext.dispose();
				}
				catch (Throwable e) {
					if (log.isErrorEnabled()) {
						log.error(e);
					}
				}
			}
		}
		writeLock.lock();
		try {
			if (parent != null) {
				parent.childContextDisposed(this);
			}
			if (linkContainers != null) {
				IList<ILinkContainer> linkContainers = this.linkContainers;
				this.linkContainers = null;
				for (int a = linkContainers.size(); a-- > 0;) {
					ILinkContainer listenerContainer = linkContainers.get(a);
					try {
						listenerContainer.unlink();
					}
					catch (Throwable e) {
						if (failOnError) {
							throw RuntimeExceptionUtil.mask(e);
						}
						if (log.isErrorEnabled()) {
							log.error(e);
						}
					}
				}
			}
			if (disposableObjects != null) {
				IList<Object> disposableObjects = this.disposableObjects;
				this.disposableObjects = null;
				for (int a = disposableObjects.size(); a-- > 0;) {
					Object disposableObject = disposableObjects.get(a);
					if (disposableObject instanceof Reference) {
						disposableObject = ((Reference<?>) disposableObject).get();
					}
					if (disposableObject == null) {
						continue;
					}
					if (disposableObject instanceof IDisposableBean) {
						try {
							((IDisposableBean) disposableObject).destroy();
						}
						catch (Throwable e) {
							if (failOnError) {
								throw RuntimeExceptionUtil.mask(e);
							}
							if (log.isErrorEnabled()) {
								log.error(e);
							}
						}
					}
					else if (disposableObject instanceof IDisposable) {
						try {
							((IDisposable) disposableObject).dispose();
						}
						catch (Throwable e) {
							if (failOnError) {
								throw RuntimeExceptionUtil.mask(e);
							}
							if (log.isErrorEnabled()) {
								log.error(e);
							}
						}
					}
					else if (disposableObject instanceof IBackgroundWorkerParamDelegate) {
						try {
							((IBackgroundWorkerParamDelegate<? super ServiceContext>) disposableObject)
									.invoke(this);
						}
						catch (Throwable e) {
							if (failOnError) {
								throw RuntimeExceptionUtil.mask(e);
							}
							if (log.isErrorEnabled()) {
								log.error(e);
							}
						}
					}
				}
			}
			beanContextFactory.dispose();
		}
		finally {
			writeLock.unlock();
			beanContextFactory = null;
			parent = null;
			nameToServiceDict = null;
			typeToServiceDict = null;
			postProcessors = null;
			preProcessors = null;
			parent = null;
			disposed = true;
			running = false;
		}
	}

	@Override
	public void childContextDisposed(IServiceContext childContext) {
		if (children == null) {
			return;
		}
		writeLock.lock();
		try {
			children.remove(childContext);
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public IServiceContext createService(Class<?>... serviceModuleTypes) {
		return createService(null, IServiceContext.class, null, serviceModuleTypes);
	}

	@Override
	public IServiceContext createService(String contextName, Class<?>... serviceModuleTypes) {
		return createService(contextName, IServiceContext.class, null, serviceModuleTypes);
	}

	@Override
	public IServiceContext createService(
			IBackgroundWorkerParamDelegate<IBeanContextFactory> registerPhaseDelegate,
			Class<?>... serviceModuleTypes) {
		return createService(null, IServiceContext.class, registerPhaseDelegate, serviceModuleTypes);
	}

	@Override
	public IServiceContext createService(String contextName,
			IBackgroundWorkerParamDelegate<IBeanContextFactory> registerPhaseDelegate,
			Class<?>... serviceModuleTypes) {
		return createService(contextName, IServiceContext.class, registerPhaseDelegate,
				serviceModuleTypes);
	}

	public <I> I createService(String contextName, Class<I> serviceClass,
			IBackgroundWorkerParamDelegate<IBeanContextFactory> registerPhaseDelegate,
			Class<?>... serviceModuleTypes) {
		checkNotDisposed();
		IBeanContextInitializer beanContextInitializer = registerBean(BeanContextInitializer.class)
				.finish();

		if (contextName == null && registerPhaseDelegate == null && serviceModuleTypes.length == 1) {
			contextName = serviceModuleTypes[0].getSimpleName();
		}
		BeanContextFactory childBeanContextFactory = beanContextFactory
				.createChildContextFactory(beanContextInitializer, this);
		IServiceContext childContext = childBeanContextFactory.create(contextName, this,
				registerPhaseDelegate, serviceModuleTypes);

		writeLock.lock();
		try {
			if (children == null) {
				children = new IdentityHashSet<>();
			}
			children.add(childContext);
		}
		finally {
			writeLock.unlock();
		}
		return childContext.getService(serviceClass);
	}

	@Override
	public <V> IBeanContextHolder<V> createHolder(final Class<V> autowiredBeanClass) {
		checkNotDisposed();
		return new IBeanContextHolder<V>() {
			protected IServiceContext beanContext = ServiceContext.this;

			@Override
			public V getValue() {
				if (beanContext == null) {
					throw new UnsupportedOperationException("This bean context has already been disposed!");
				}
				return beanContext.getService(autowiredBeanClass);
			}

			@Override
			public void dispose() {
				if (beanContext != null) {
					IServiceContext beanContext = this.beanContext;
					this.beanContext = null;
					beanContext.dispose();
				}
			}
		};
	}

	@Override
	public <V> IBeanContextHolder<V> createHolder(final String beanName, Class<V> expectedClass) {
		checkNotDisposed();
		return new IBeanContextHolder<V>() {
			protected IServiceContext beanContext = ServiceContext.this;

			@SuppressWarnings("unchecked")
			@Override
			public V getValue() {
				if (beanContext == null) {
					throw new UnsupportedOperationException("This bean context has already been disposed!");
				}
				return (V) beanContext.getService(beanName);
			}

			@Override
			public void dispose() {
				if (beanContext != null) {
					IServiceContext beanContext = this.beanContext;
					this.beanContext = null;
					beanContext.dispose();
				}
			}
		};
	}

	protected void handleObjects(final HandleObjectsDelegate handleObjectsDelegate) {
		final Set<Object> alreadyHandledSet = IdentityHashSet
				.create(typeToServiceDict.size() + nameToServiceDict.size());
		for (Entry<Class<?>, Object> entry : typeToServiceDict) {
			Object obj = entry.getValue();
			if (alreadyHandledSet.add(obj)) {
				handleObjectsDelegate.invoke(obj);
			}
		}
		for (Entry<String, Object> entry : nameToServiceDict) {
			Object obj = entry.getValue();
			if (alreadyHandledSet.add(obj)) {
				handleObjectsDelegate.invoke(obj);
			}
		}
	}

	@Override
	public ILinkRegistryNeededRuntime<?> link(String listenerBeanName) {
		checkNotDisposed();
		return beanContextFactory.link(this, listenerBeanName);
	}

	@Override
	public ILinkRegistryNeededRuntime<?> link(IBeanConfiguration listenerBean) {
		checkNotDisposed();
		return beanContextFactory.link(this, listenerBean);
	}

	@Override
	public <D> ILinkRegistryNeededRuntime<D> link(D listener) {
		checkNotDisposed();
		return beanContextFactory.link(this, listener);
	}

	@Override
	public void registerDisposable(IDisposableBean disposableBean) {
		registerDisposableIntern(disposableBean, true);
	}

	@Override
	public void registerDisposeHook(IBackgroundWorkerParamDelegate<IServiceContext> waitCallback) {
		registerDisposableIntern(waitCallback, false);
	}

	public void addDisposables(List<Object> disposableObjects) {
		if (this.disposableObjects == null) {
			this.disposableObjects = new ArrayList<>(disposableObjects.size());
		}
		this.disposableObjects.addAll(disposableObjects);
	}

	protected void registerDisposableIntern(Object obj, boolean registerWeakOnRunning) {
		checkNotDisposed();
		ParamChecker.assertParamNotNull(obj, "obj");
		if (!isRunning()) {
			if (disposableObjects == null) {
				disposableObjects = new ArrayList<>();
			}
			disposableObjects.add(obj);
			return;
		}
		Lock writeLock = this.writeLock;
		LockState lockState = null;
		if (readLock.isReadLockHeld() && !this.writeLock.isWriteLockHeld()) {
			lockState = writeLock.releaseAllLocks();
		}
		writeLock.lock();
		try {
			ArrayList<Object> disposableObjects = this.disposableObjects;
			if (disposableObjects == null) {
				disposableObjects = new ArrayList<>();
				this.disposableObjects = disposableObjects;
			}
			// "monte carlo" approach to check for disposable objects without noticeable impact on the
			// runtime performance
			while (!disposableObjects.isEmpty()) {
				int randomIndex = (int) (Math.random() * disposableObjects.size());
				Object disposableObject = disposableObjects.get(randomIndex);
				if (disposableObject instanceof Reference) {
					disposableObject = ((Reference<?>) disposableObject).get();
				}
				if (disposableObject != null) {
					// not a collected object. we finish the search for collected disposables
					break;
				}
				disposableObjects.remove(randomIndex);
			}
			disposableObjects.add(registerWeakOnRunning ? new WeakReference<>(obj) : obj);
		}
		finally {
			writeLock.unlock();
			writeLock.reacquireLocks(lockState);
		}
	}

	@Override
	public <T> T getService(Class<T> serviceType) {
		return getService(serviceType, true);
	}

	@Override
	public <T> T getService(Class<T> type, boolean checkExistence) {
		checkNotDisposed();
		Lock readLock = this.readLock;
		readLock.lock();
		try {
			ParamChecker.assertParamNotNull(type, "type");
			T service = getServiceIntern(type, SearchType.CASCADE);
			if (service == null && checkExistence) {
				throw new IllegalStateException("No bean autowired to type '" + type.getName() + "'");
			}
			return service;
		}
		finally {
			readLock.unlock();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getServiceIntern(Class<T> serviceType, SearchType searchType) {
		Object service = null;
		if (!SearchType.PARENT.equals(searchType)) {
			service = typeToServiceDict.get(serviceType);
		}
		if (service instanceof IFactoryBean) {
			try {
				Object factoryResult = ((IFactoryBean) service).getObject();
				if (factoryResult == null) {
					throw new IllegalStateException("Anonymous factory bean of type "
							+ service.getClass().getName() + " returned null for service type "
							+ serviceType.getName()
							+ ". Possibly a cyclic relationship from the factory to its cascaded dependencies and back");
				}
				service = factoryResult;
			}
			catch (Throwable e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		else if (service == null && parent != null && !SearchType.CURRENT.equals(searchType)) {
			service = parent.getService(serviceType, false);
		}
		if (service == null && externalServiceContext != null) {
			service = externalServiceContext.getServiceByType(serviceType);
		}
		return (T) service;
	}

	@Override
	public Object getDirectBean(Class<?> serviceType) {
		checkNotDisposed();
		return typeToServiceDict.get(serviceType);
	}

	@Override
	public Object getDirectBean(String beanName) {
		checkNotDisposed();
		if (nameToServiceDict == null) {
			return null;
		}
		return nameToServiceDict.get(beanName);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getServiceIntern(String serviceName, Class<T> serviceType, SearchType searchType) {
		String realServiceName = serviceName;
		boolean factoryContentRequest = true, parentOnlyRequest = false;
		while (true) {
			char firstChar = realServiceName.charAt(0);
			if (firstChar == '&') {
				realServiceName = realServiceName.substring(1);
				factoryContentRequest = false;
				continue;
			}
			else if (firstChar == '*') {
				realServiceName = realServiceName.substring(1);
				parentOnlyRequest = true;
				continue;
			}
			// No escape character found, realServiceName is now resolved
			break;
		}
		if (realServiceName.length() == 0) {
			throw new IllegalArgumentException("Bean name '" + serviceName + "' not valid");
		}
		Map<String, String> beanNameToAliasesMap = beanContextFactory.getAliasToBeanNameMap();
		if (beanNameToAliasesMap != null) {
			String realBeanName = beanNameToAliasesMap.get(realServiceName);
			if (realBeanName != null) {
				realServiceName = realBeanName;

				serviceName = StringBuilderUtil.concat(objectCollector.getCurrent(),
						factoryContentRequest ? "" : "&", parentOnlyRequest ? "*" : "", realBeanName);
			}
		}
		Object service = null;
		if (!parentOnlyRequest && !SearchType.PARENT.equals(searchType) && nameToServiceDict != null) {
			service = nameToServiceDict.get(realServiceName);
		}
		if (service instanceof IFactoryBean && factoryContentRequest) {
			try {
				Object factoryResult = ((IFactoryBean) service).getObject();
				if (factoryResult == null) {
					throw new BeanContextInitException("Factory bean '" + serviceName + "' of type "
							+ service.getClass().getName() + " returned null for service type "
							+ serviceType.getName()
							+ ". Possibly a cyclic relationship from the factory to its cascaded dependencies and back");
				}
				service = factoryResult;
			}
			catch (Throwable e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		else if (service == null && parent != null) {
			if (parentOnlyRequest) {
				// Reconstruct factory bean prefix if necessary
				return parent.getService(factoryContentRequest ? "&" + realServiceName : realServiceName,
						serviceType, false);
			}
			else if (!SearchType.CURRENT.equals(searchType)) {
				return parent.getService(serviceName, serviceType, false);
			}
		}
		if (service != null && !serviceType.isAssignableFrom(service.getClass())) {
			throw new BeanContextInitException("Bean with name '" + serviceName
					+ "' not assignable to type '" + serviceType.getName() + "'");
		}
		return (T) service;
	}

	@Override
	public Object getService(String serviceName) {
		return getService(serviceName, true);
	}

	@Override
	public Object getService(String serviceName, boolean checkExistence) {
		checkNotDisposed();
		Lock readLock = this.readLock;
		readLock.lock();
		try {
			if (serviceName == null || serviceName.length() == 0) {
				throw new BeanContextInitException(
						"Tried to get a bean with empty name. This is not allowed.");
			}
			Object service = getServiceIntern(serviceName, Object.class, SearchType.CASCADE);
			if (service == null && checkExistence) {
				throw new BeanContextInitException("No bean found with name '" + serviceName + "'");
			}
			return service;
		}
		finally {
			readLock.unlock();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> V getService(String serviceName, Class<V> targetType) {
		return (V) getService(serviceName, true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> V getService(String serviceName, Class<V> targetType, boolean checkExistence) {
		return (V) getService(serviceName, checkExistence);
	}

	@Override
	public void printContent(StringBuilder sb) {
		sb.append("n/a");
		// if (disposed)
		// {
		// sb.append(toStringBackup);
		// return;
		// }
		// checkNotDisposed();
		// IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		// readLock.lock();
		// try
		// {
		// sb.append("Named content (").append(nameToServiceDict != null ? nameToServiceDict.size() :
		// 0).append("): [");
		// if (nameToServiceDict != null)
		// {
		// ArrayList<String> list = new ArrayList<String>();
		// ISet<Entry<String, Object>> entrySet = nameToServiceDict.entrySet();
		// IIterator<Entry<String, Object>> iter = entrySet.iteratorDisposable();
		// while (iter.hasNext())
		// {
		// Entry<String, Object> entry = iter.next();
		//
		// list.add(entry.getKey());
		// }
		// Collections.sort(list);
		//
		// boolean first = true;
		//
		// for (int a = 0, size = list.size(); a < size; a++)
		// {
		// if (first)
		// {
		// first = false;
		// }
		// else
		// {
		// sb.append(',');
		// }
		// sb.append(list.get(a));
		// }
		// iter.dispose();
		// entrySet.dispose();
		// list.dispose();
		// }
		// sb.append("]").append(System.getProperty("line.separator")).append("Autowired content
		// (").append(typeToServiceDict.size()).append("): [");
		// ArrayList<Class<?>> list = tlObjectCollector.create(ArrayList.class);
		//
		// ISet<Entry<Class<?>, Object>> entrySet = typeToServiceDict.entrySet();
		// IIterator<Entry<Class<?>, Object>> iter = entrySet.iteratorDisposable();
		// while (iter.hasNext())
		// {
		// Entry<Class<?>, Object> entry = iter.next();
		//
		// list.add(entry.getKey());
		// }
		// ITypeInfoProvider typeInfoProvider = getTypeInfoProvider();
		// if (typeInfoProvider != null)
		// {
		// Collections.sort(list, new SimpleClassNameComparator(typeInfoProvider));
		// }
		// else
		// {
		// Collections.sort(list, new Comparator<Class<?>>()
		// {
		// @Override
		// public int compare(Class<?> o1, Class<?> o2)
		// {
		// return o1.getSimpleName().compareTo(o2.getSimpleName());
		// }
		// });
		// }
		// boolean first = true;
		// for (int a = 0, size = list.size(); a < size; a++)
		// {
		// if (first)
		// {
		// first = false;
		// }
		// else
		// {
		// sb.append(',');
		// }
		// sb.append(list.get(a).getName());
		// }
		// iter.dispose();
		// entrySet.dispose();
		// list.dispose();
		// sb.append("]");
		// }
		// finally
		// {
		// readLock.unlock();
		// }
		// if (parent != null)
		// {
		// sb.append(SystemUtil.lineSeparator());
		// sb.append("Parent");
		// sb.append(SystemUtil.lineSeparator());
		// parent.printContent(sb);
		// }
	}

	protected ITypeInfoProvider getTypeInfoProvider() {
		if (typeInfoProvider == null) {
			typeInfoProvider = getService(ITypeInfoProvider.class, false);
		}
		return typeInfoProvider;
	}

	@Override
	public <T> IList<T> getObjects(final Class<T> type) {
		checkNotDisposed();

		final IdentityHashSet<T> result = new IdentityHashSet<>();

		IList<T> parentResult = parent != null ? parent.getObjects(type) : null;
		if (parentResult != null) {
			result.addAll(parentResult);
		}
		Lock readLock = this.readLock;
		readLock.lock();
		try {
			handleObjects(new HandleObjectsDelegate() {
				@SuppressWarnings("unchecked")
				@Override
				public void invoke(Object obj) {
					if (type.isAssignableFrom(obj.getClass())) {
						result.add((T) obj);
					}
				}
			});
			return result.toList();
		}
		finally {
			readLock.unlock();
		}
	}

	@Override
	public <T extends Annotation> IList<Object> getAnnotatedObjects(final Class<T> annoType) {
		final ISet<Object> set = new IdentityHashSet<>();
		Lock readLock = this.readLock;
		readLock.lock();
		try {
			handleObjects(new HandleObjectsDelegate() {
				@Override
				public void invoke(Object obj) {
					Annotation anno = obj.getClass().getAnnotation(annoType);
					if (anno != null) {
						set.add(obj);
					}
				}
			});
			return set.toList();
		}
		finally {
			readLock.unlock();
		}
	}

	@Override
	public <T> IList<T> getImplementingObjects(final Class<T> interfaceType) {
		final IdentityHashSet<T> set = new IdentityHashSet<>();
		Lock readLock = this.readLock;
		readLock.lock();
		try {
			handleObjects(new HandleObjectsDelegate() {
				@SuppressWarnings("unchecked")
				@Override
				public void invoke(Object obj) {
					if (interfaceType.isAssignableFrom(obj.getClass())) {
						set.add((T) obj);
					}
				}
			});
			return ListUtil.anyToList(objectCollector, set);
		}
		finally {
			readLock.unlock();
		}
	}

	@Deprecated
	@Override
	public <V> IBeanRuntime<V> registerAnonymousBean(Class<V> beanType) {
		return registerBean(beanType);
	}

	@Override
	public <V> IBeanRuntime<V> registerBean(Class<V> beanType) {
		checkNotDisposed();
		ParamChecker.assertParamNotNull(beanType, "beanType");
		return new BeanRuntime<>(this, beanType, true);
	}

	@Override
	public <V> IBeanRuntime<V> registerExternalBean(V externalBean) {
		checkNotDisposed();
		ParamChecker.assertParamNotNull(externalBean, "externalBean");
		return new BeanRuntime<>(this, externalBean, false);
	}

	@Override
	public <V> IBeanRuntime<V> registerWithLifecycle(V obj) {
		checkNotDisposed();
		ParamChecker.assertParamNotNull(obj, "obj");
		return new BeanRuntime<>(this, obj, true);
	}

	@Override
	public String toString() {
		if (disposed) {
			return toStringBackup;
		}
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb) {
		printContent(sb);
	}

	@Override
	public ISet<Class<?>> collectAllTypeWiredServices() {
		HashSet<Class<?>> typeWiredNonFactoryServices = new HashSet<>();
		for (Entry<Class<?>, Object> entry : typeToServiceDict) {
			Class<?> serviceType = entry.getKey();
			typeWiredNonFactoryServices.add(serviceType);
		}
		return typeWiredNonFactoryServices;
	}
}
