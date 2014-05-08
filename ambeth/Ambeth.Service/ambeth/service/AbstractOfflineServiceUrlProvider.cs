using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Extendable;
using De.Osthus.Ambeth.Util;
using System;
using System.ComponentModel;
using System.Threading;

namespace De.Osthus.Ambeth.Service
{
    public abstract class AbstractOfflineServiceUrlProvider : IServiceUrlProvider, IOfflineListenerExtendable, IInitializingBean, INotifyPropertyChanged
    {
        public virtual event PropertyChangedEventHandler PropertyChanged;

        protected bool isOffline;

        [Property(ServiceConfigurationConstants.OfflineMode, DefaultValue = "false")]
        public virtual bool IsOffline
        {
            get
            {
                return isOffline;
            }
            set
            {
                if (isOffline == value)
                {
                    return;
                }
                isOffline = value;
                ThreadPool.QueueUserWorkItem(delegate(Object handle)
                {
                    IsOfflineChanged();
                    SyncContext.Post(delegate(Object handle2)
                    {
                        if (PropertyChanged != null)
                        {
                            PropertyChanged.Invoke(this, new PropertyChangedEventArgs("IsOffline"));
                        }
                    }, null);
                }, null);
            }
        }

        protected readonly IExtendableContainer<IOfflineListener> offlineListeners = new DefaultExtendableContainer<IOfflineListener>("offlineListener");

        public SynchronizationContext SyncContext { get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertParamNotNull(SyncContext, "SyncContext");
        }

        public void LockForRestart(bool offlineAfterRestart)
        {
            IOfflineListener[] listeners = offlineListeners.GetExtensions();

            foreach (IOfflineListener offlineListener in listeners)
            {
                if (offlineAfterRestart)
                {
                    offlineListener.BeginOffline();
                }
                else
                {
                    offlineListener.BeginOnline();
                }
            }
            foreach (IOfflineListener offlineListener in listeners)
            {
                if (offlineAfterRestart)
                {
                    offlineListener.HandleOffline();
                }
                else
                {
                    offlineListener.HandleOnline();
                }
            }
        }

        protected virtual void IsOfflineChanged()
        {
            IOfflineListener[] listeners = offlineListeners.GetExtensions();

            foreach (IOfflineListener offlineListener in listeners)
            {
                if (isOffline)
                {
                    offlineListener.BeginOffline();
                }
                else
                {
                    offlineListener.BeginOnline();
                }
            }
            foreach (IOfflineListener offlineListener in listeners)
            {
                if (isOffline)
                {
                    offlineListener.HandleOffline();
                }
                else
                {
                    offlineListener.HandleOnline();
                }
            }
            foreach (IOfflineListener offlineListener in listeners)
            {
                if (isOffline)
                {
                    offlineListener.EndOffline();
                }
                else
                {
                    offlineListener.EndOnline();
                }
            }
        }

        public abstract String GetServiceURL(Type serviceInterface, String serviceName);

        public virtual void AddOfflineListener(IOfflineListener offlineListener)
        {
            offlineListeners.Register(offlineListener);
        }

        public virtual void RemoveOfflineListener(IOfflineListener offlineListener)
        {
            offlineListeners.Unregister(offlineListener);
        }
    }
}
