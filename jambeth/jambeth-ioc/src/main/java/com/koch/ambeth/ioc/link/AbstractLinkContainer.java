package com.koch.ambeth.ioc.link;

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

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.IServiceLookup;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.config.IDeclarationStackTraceAware;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.exception.LinkException;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.util.IDelegateFactory;
import com.koch.ambeth.util.ParamChecker;
import lombok.Setter;

import java.lang.ref.WeakReference;
import java.util.Arrays;

public abstract class AbstractLinkContainer implements ILinkContainer, IInitializingBean, IDeclarationStackTraceAware {
    public static final String P_ARGUMENTS = "Arguments";
    public static final String P_OPTIONAL = "Optional";
    public static final String P_REGISTRY = "Registry";
    public static final String P_REGISTRY_PROPERTY_NAME = "RegistryPropertyName";
    public static final String P_LISTENER = "Listener";
    public static final String P_LISTENER_BEAN = "ListenerBean";
    public static final String P_LISTENER_NAME = "ListenerBeanName";
    public static final String P_LISTENER_METHOD_NAME = "ListenerMethodName";
    public static final String P_REGISTRY_TYPE = "RegistryBeanAutowiredType";
    public static final String P_FOREIGN_BEAN_CONTEXT = "ForeignBeanContext";
    public static final String P_FOREIGN_BEAN_CONTEXT_NAME = "ForeignBeanContextName";
    protected static final Object[] emptyArgs = new Object[0];
    @Setter
    @Property(mandatory = false)
    protected Object listener;
    @Setter
    @Property(mandatory = false)
    protected IBeanConfiguration listenerBean;
    @Setter
    @Property(mandatory = false)
    protected String listenerBeanName;
    @Setter
    @Property(mandatory = false)
    protected String listenerMethodName;
    @Setter
    @Autowired
    protected IServiceLookup beanLookup;
    @Setter
    @Property(mandatory = false)
    protected IServiceContext foreignBeanContext;
    @Setter
    @Property(mandatory = false)
    protected String foreignBeanContextName;
    @Setter
    @Autowired
    protected IDelegateFactory delegateFactory;

    @Setter
    @Property(mandatory = false)
    protected Object registry;
    @Setter
    @Property(mandatory = false)
    protected Class<?> registryBeanAutowiredType;
    @Setter
    @Property(mandatory = false)
    protected String registryPropertyName;
    @Setter
    @Property(mandatory = false)
    protected Object[] arguments;
    @Setter
    @Property(mandatory = false)
    protected boolean optional;
    protected Object resolvedListener;
    protected StackTraceElement[] declarationStackTrace;
    protected boolean linked;
    protected int linkCounter;

    @Override
    public void afterPropertiesSet() throws Exception {
        ParamChecker.assertTrue(registryBeanAutowiredType != null || registry != null, "either property 'RegistryBeanAutowiredType', 'RegistryBeanName' or 'Registry' must be valid");
        ParamChecker.assertTrue(listener != null || listenerBean != null || listenerBeanName != null, "either property 'Listener' or 'ListenerBean' or 'ListenerBeanName' must be valid");
        if (arguments == null) {
            arguments = emptyArgs;
        }
    }

    @Override
    public void setDeclarationStackTrace(StackTraceElement[] declarationStackTrace) {
        this.declarationStackTrace = declarationStackTrace;
    }

    protected Object resolveRegistry() {
        var beanLookup = this.beanLookup;
        var hasForeignContextBeenUsed = true;
        if (foreignBeanContext != null) {
            beanLookup = foreignBeanContext;
            hasForeignContextBeenUsed = false;
        } else if (foreignBeanContextName != null) {
            foreignBeanContext = beanLookup.getService(foreignBeanContextName, IServiceContext.class, !optional);
            beanLookup = foreignBeanContext;
            hasForeignContextBeenUsed = false;
        }
        if (beanLookup == null && registry == null) {
            return null;
        }
        var registry = this.registry;
        if (registry instanceof Class) {
            registry = beanLookup.getService((Class<?>) registry, !optional);
            hasForeignContextBeenUsed = true;
        } else if (registry instanceof String) {
            registry = beanLookup.getService((String) registry, !optional);
            hasForeignContextBeenUsed = true;
        } else if (registry instanceof IBeanConfiguration) {
            registry = beanLookup.getService(((IBeanConfiguration) registry).getName(), !optional);
            hasForeignContextBeenUsed = true;
        } else if (registry == null) {
            registry = beanLookup.getService(registryBeanAutowiredType, !optional);
            hasForeignContextBeenUsed = true;
        }
        if (registry == null) {
            return null;
        }
        if (!hasForeignContextBeenUsed) {
            throw new LinkException(ILinkRegistryNeededConfiguration.class.getSimpleName() +
                    ".toContext(...) has been called but at the same time the registry has been provided as an instance with the .to(...) overload", this);
        }
        registry = resolveRegistryIntern(registry);
        this.registry = registry;
        return registry;
    }

    protected Object resolveRegistryIntern(Object registry) {
        return registry;
    }

    protected Object resolveListener() {
        Object listener = this.listener;
        if (listener != null) {
            listener = resolveListenerIntern(listener);
            this.listener = listener;
            return listener;
        } else if (listenerBeanName != null) {
            listener = beanLookup.getService(listenerBeanName, !optional);
            if (listener == null) {
                return null;
            }
        } else if (listenerBean != null) {
            listenerBeanName = listenerBean.getName();
            if (listenerBeanName == null) {
                listener = listenerBean.getInstance();
                if (listener == null) {
                    throw new LinkException("No listener instance received from " + listenerBean.getClass().getName() + ".getInstance()" + " to link to registry", this);
                }
            } else {
                listener = beanLookup.getService(listenerBeanName, !optional);
                if (listener == null) {
                    return null;
                }
            }
        } else {
            throw new LinkException("Listener not found. Must never happen.", this);
        }
        listener = resolveListenerIntern(listener);
        this.listener = listener;
        return listener;
    }

    protected Object resolveListenerIntern(Object listener) {
        if (listener == null) {
            throw new LinkException("Must never happen", this);
        }
        return listener;
    }

    @Override
    public synchronized boolean link() {
        if (linked) {
            return false;
        }
        Object registry = null, listener = null;
        try {
            registry = resolveRegistry();
            if (registry == null) {
                return false;
            }
            listener = resolveListener();
            if (listener == null) {
                return false;
            }
            handleLink(registry, listener);
        } catch (Throwable e) {
            if (listener == null) {
                listener = this.listener;
            }
            if (registry == null) {
                registry = this.registry;
            }
            if (declarationStackTrace != null) {
                throw new LinkException("An error occured while trying to link " + (listenerBeanName != null ? "'" + listenerBeanName + "'" : listener) + " to '" + registry + "'\n" +
                        Arrays.toString(declarationStackTrace), e, this);
            }
            throw new LinkException("An error occured while trying to link " + (listenerBeanName != null ? "'" + listenerBeanName + "'" : listener) + " to '" + registry + "'", e, this);
        }
        linked = true;
        linkCounter++;
        if (foreignBeanContext != null) {
            foreignBeanContext.registerDisposable(new LinkDisposable(this));
        }
        return true;
    }

    @Override
    public synchronized boolean unlink() {
        if (!linked) {
            return false;
        }
        try {
            if (registry == null || listener == null) {
                // Nothing to do because there was no (successful) call to link() before
                ILogger log = getLog();
                if (!optional && log.isDebugEnabled()) {
                    log.debug("Unlink has been called without prior linking. If no other exception is visible in the logs then this may be a bug");
                }
                return false;
            }
            handleUnlink(registry, listener);
        } catch (Exception e) {
            throw new LinkException("An error occured while trying to unlink " + (listenerBeanName != null ? "'" + listenerBeanName + "'" : listener) + " from '" + registry + "'", e, this);
        }
        linked = false;
        return true;
    }

    protected abstract ILogger getLog();

    protected abstract void handleLink(Object registry, Object listener) throws Exception;

    protected abstract void handleUnlink(Object registry, Object listener) throws Exception;

    public static class LinkDisposable extends WeakReference<AbstractLinkContainer> implements IDisposableBean {
        private final int linkCounter;

        public LinkDisposable(AbstractLinkContainer target) {
            super(target);
            linkCounter = target.linkCounter;
        }

        @Override
        public void destroy() throws Throwable {
            AbstractLinkContainer target = get();
            if (target == null || linkCounter != target.linkCounter || !target.linked) {
                // this delegate is already outdated
                return;
            }
            target.unlink();
        }
    }
}
