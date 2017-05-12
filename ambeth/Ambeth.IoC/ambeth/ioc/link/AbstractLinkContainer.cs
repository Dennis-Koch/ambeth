using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Ioc.Exceptions;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;
using System;

namespace De.Osthus.Ambeth.Ioc.Link
{
    abstract public class AbstractLinkContainer : ILinkContainer, IInitializingBean
    {
        public static readonly String PROPERTY_ARGUMENTS = "Arguments";

        public static readonly String PROPERTY_OPTIONAL = "Optional";

        public static readonly String PROPERTY_REGISTRY = "Registry";

        public static readonly String PROPERTY_REGISTRY_NAME = "RegistryBeanName";

        public static readonly String PROPERTY_REGISTRY_PROPERTY_NAME = "RegistryPropertyName";

        public static readonly String PROPERTY_LISTENER = "Listener";

        public static readonly String PROPERTY_LISTENER_BEAN = "ListenerBean";

        public static readonly String PROPERTY_LISTENER_NAME = "ListenerBeanName";

        public static readonly String PROPERTY_LISTENER_METHOD_NAME = "ListenerMethodName";

        public static readonly String PROPERTY_REGISTRY_TYPE = "RegistryBeanAutowiredType";

        public Object Listener { protected get; set; }

        public IBeanConfiguration ListenerBean { protected get; set; }

        public String ListenerBeanName { protected get; set; }

        public String ListenerMethodName { protected get; set; }

        public IServiceContext BeanContext { protected get; set; }

        public IDelegateFactory DelegateFactory { protected get; set; }

        public Object Registry { protected get; set; }

        public Type RegistryBeanAutowiredType { protected get; set; }

        public String RegistryBeanName { protected get; set; }

        public String RegistryPropertyName { protected get; set; }

        public Object[] Arguments { protected get; set; }

        public bool Optional { protected get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertTrue(RegistryBeanAutowiredType != null || RegistryBeanName != null || Registry != null,
                    "either property 'RegistryBeanAutowiredType', 'RegistryBeanName' or 'Registry' must be valid");
            ParamChecker.AssertTrue(Listener != null || ListenerBean != null || ListenerBeanName != null, "either property 'Listener' or 'ListenerBean' or 'ListenerBeanName' must be valid");
            ParamChecker.AssertParamNotNull(BeanContext, "BeanContext");
            ParamChecker.AssertParamNotNull(DelegateFactory, "DelegateFactory");
            if (Arguments == null)
            {
                Arguments = EmptyList.EmptyArray<Object>();
            }
        }

        protected Object ResolveRegistry()
        {
            Object registry = this.Registry;
            if (registry != null)
            {
                registry = ResolveRegistryIntern(registry);
                return registry;
            }
            if (RegistryBeanName != null)
            {
                registry = BeanContext.GetService(RegistryBeanName, !Optional);
            }
            else
            {
                registry = BeanContext.GetService(RegistryBeanAutowiredType, !Optional);
            }
            registry = ResolveRegistryIntern(registry);
            this.Registry = registry;
            return registry;
        }

        protected virtual Object ResolveRegistryIntern(Object registry)
        {
            return registry;
        }

        protected Object ResolveListener()
        {
            Object listener = this.Listener;
            if (listener != null)
            {
                listener = ResolveListenerIntern(listener);
                this.Listener = listener;
                return listener;
            }
            else if (ListenerBeanName != null)
            {
                listener = BeanContext.GetService(ListenerBeanName, !Optional);
                if (listener == null)
                {
                    return null;
                }
            }
            else if (ListenerBean != null)
            {
                ListenerBeanName = ListenerBean.GetName();
                if (ListenerBeanName == null)
                {
                    listener = ListenerBean.GetInstance();
                    if (listener == null)
                    {
                        throw new LinkException("No listener instance received from " + ListenerBean.GetType().FullName + ".getInstance()" + " to link to registry", this);
                    }
                }
                else
                {
                    listener = BeanContext.GetService(ListenerBeanName, !Optional);
                    if (listener == null)
                    {
                        return null;
                    }
                }
            }
            else
            {
                throw new LinkException("Listener not found. Must never happen.", this);
            }
            listener = ResolveListenerIntern(listener);
            this.Listener = listener;
            return listener;
        }

        protected virtual Object ResolveListenerIntern(Object listener)
        {
            if (listener == null)
            {
                throw new LinkException("Must never happen", this);
            }
            return listener;
        }

        public void Link()
        {
            try
            {
                Object registry = ResolveRegistry();
                if (registry == null)
                {
                    return;
                }
                Object listener = ResolveListener();
                if (listener == null)
                {
                    return;
                }
                HandleLink(registry, listener);
            }
            catch (System.Exception e)
            {
                throw new LinkException("An error occured while trying to link "
                    + (ListenerBeanName != null ? "'" + ListenerBeanName + "'" : Listener)
                    + " to " + (RegistryBeanName != null ? "'" + RegistryBeanName + "'" : Registry), e, this);
            }
        }

        public void Unlink()
        {
            try
            {
                if (Registry == null || Listener == null)
                {
                    // Nothing to do because there was no (successful) call to link() before
                    if (!Optional && GetLog().DebugEnabled)
                    {
                        GetLog().Debug("Unlink has been called without prior linking. If no other exception is visible in the logs then this may be a bug");
                    }
                    return;
                }
                HandleUnlink(Registry, Listener);
            }
            catch (System.Exception e)
            {
                throw new LinkException("An error occured while trying to unlink "
                    + (ListenerBeanName != null ? "'" + ListenerBeanName + "'" : Listener)
                    + " from " + (RegistryBeanName != null ? "'" + RegistryBeanName + "'" : Registry), e, this);
            }
            finally
            {
                Registry = null;
                Listener = null;
            }
        }

        protected abstract ILogger GetLog();

        protected abstract void HandleLink(Object registry, Object listener);

        protected abstract void HandleUnlink(Object registry, Object listener);
    }
}