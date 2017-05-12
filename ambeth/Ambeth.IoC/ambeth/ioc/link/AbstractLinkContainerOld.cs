using De.Osthus.Ambeth.Log;
using System;
using De.Osthus.Ambeth.Ioc.Config;
using System.Reflection;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Ioc.Exceptions;

namespace De.Osthus.Ambeth.Ioc.Link
{
    abstract public class AbstractLinkContainerOld : ILinkContainer, IInitializingBean
    {
        [LogInstance]
		public ILogger Log { private get; set; }

        public Object Listener { get; set; }

        public IBeanConfiguration ListenerBean { get; set; }

        public IServiceContext BeanContext { get; set; }

        public Object[] Arguments { get; set; }

        public Type RegistryBeanAutowiredType { get; set; }

        public String ListenerBeanName { get; set; }

        public String RegistryBeanName { get; set; }

        protected Object resolvedRegistry;

        protected Object resolvedListener;

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertTrue(RegistryBeanAutowiredType != null || RegistryBeanName != null,
                    "either property 'RegistryBeanAutowiredType' or 'RegistryBeanName' must be valid");
            ParamChecker.AssertTrue(Listener != null || ListenerBean != null || ListenerBeanName != null, "either property 'Listener' or 'ListenerBean' or 'ListenerBeanName' must be valid");
            ParamChecker.AssertParamNotNull(BeanContext, "BeanContext");
            ParamChecker.AssertParamNotNull(Arguments, "Arguments");
        }

        protected Object resolveRegistry()
        {
            Object registry;
            if (RegistryBeanName != null)
            {
                registry = BeanContext.GetService(RegistryBeanName);
                if (registry == null)
                {
                    throw new System.Exception("No registry bean with name '" + RegistryBeanName + "' found to link bean");
                }
                return registry;
            }
            registry = BeanContext.GetService(RegistryBeanAutowiredType);
            if (registry == null)
            {
                throw new System.Exception("No registry bean with autowired type " + RegistryBeanAutowiredType.FullName + " found to link bean");
            }
            return registry;
        }

        protected Object resolveListener()
        {
            Object listener = Listener;

            Object listenerTarget;
            if (listener != null && !(listener is LateDelegate))
            {
                listenerTarget = listener;
            }
            else if (ListenerBeanName != null)
            {
                listenerTarget = BeanContext.GetService(ListenerBeanName);
            }
            else if (ListenerBean != null)
            {
                ListenerBeanName = ListenerBean.GetName();
                if (ListenerBeanName == null)
                {
                    listenerTarget = ListenerBean.GetInstance();
                    if (listenerTarget == null)
                    {
                        throw new System.Exception("No listener instance received from " + ListenerBean.GetType().FullName + ".getInstance()" + " to link to registry");
                    }
                }
                else
                {
                    listenerTarget = BeanContext.GetService(ListenerBeanName);
                }
            }
            else
            {
                throw new System.Exception("Listener not found. Must never happen.");
            }
            if (listener is LateDelegate)
            {
                listener = ((LateDelegate)listener).GetDelegate(listener.GetType(), listenerTarget);
            }
            else
            {
                listener = listenerTarget;
            }
            return resolveListenerIntern(listener);
        }

        protected virtual Object resolveListenerIntern(Object listener)
        {
            if (listener == null)
            {
                throw new System.Exception("Must never happen");
            }
            return listener;
        }

        public void Link()
        {
            if (resolvedRegistry != null || resolvedListener != null)
            {
                throw new System.Exception();
            }
            resolvedRegistry = resolveRegistry();
            resolvedListener = resolveListener();

            try
            {
                HandleLink(resolvedRegistry, resolvedListener, Arguments);
            }
            catch (System.Exception e)
            {
                throw new System.Exception("An error occured while trying to link "
                    + (ListenerBeanName != null ? "'" + ListenerBeanName + "'" : resolvedListener)
                    + " to " + (RegistryBeanName != null ? "'" + RegistryBeanName + "'" : resolvedRegistry), e);
            }
        }

        abstract protected void HandleLink(Object registry, Object listener, Object[] argArray);

        public void Unlink()
        {
            if (resolvedRegistry == null || resolvedListener == null)
            {
                // Nothing to do because there was no (successful) call to link() before
                if (Log.DebugEnabled)
                {
                    Log.Debug("Unlink has been called without prior linking. If no other exception is visible in the logs then this may be a bug");
                }
                return;
            }
            try
            {
                HandleUnlink(resolvedRegistry, resolvedListener, Arguments);
            }
            catch (System.Exception e)
            {
                throw new System.Exception("An error occured while trying to unlink "
                    + (ListenerBeanName != null ? "'" + ListenerBeanName + "'" : resolvedListener)
                    + " from " + (RegistryBeanName != null ? "'" + RegistryBeanName + "'" : resolvedRegistry), e);
            }
            finally
            {
                resolvedRegistry = null;
                resolvedListener = null;
            }
        }

        abstract protected void HandleUnlink(Object registry, Object listener, Object[] argArray);
    }
}