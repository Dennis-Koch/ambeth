using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Collections.Specialized;
using System.ComponentModel;
using System.Threading;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Util;
using De.Osthus.Minerva.Core;

namespace De.Osthus.Minerva.Core
{
    public class FilteringMultiContainer<T> : ModelMultiContainer<T>, IInitializingBean, IDisposableBean
    {
        public override event PropertyChangedEventHandler PropertyChanged;

        private int busyCount = 0;

        private Object busyLock = new Object();

        protected IModelContainer dataSource;

        public IModelContainer DataSource
        {
            get
            {
                return dataSource;
            }
            set
            {
                if (Object.ReferenceEquals(dataSource, value))
                {
                    return;
                }
                if (dataSource != null)
                {
                    if (dataSource is INotifyPropertyChanged)
                    {
                        ((INotifyPropertyChanged)dataSource).PropertyChanged -= HandlePropertyChangedOfDataSource;
                    }
                    if (DataSource is INotifyCollectionChanged)
                    {
                        ((INotifyCollectionChanged)dataSource).CollectionChanged -= HandleCollectionChangedOfDataSource;
                    }
                }
                dataSource = value;
                if (dataSource != null)
                {
                    if (dataSource is INotifyPropertyChanged)
                    {
                        ((INotifyPropertyChanged)dataSource).PropertyChanged += HandlePropertyChangedOfDataSource;
                    }
                    if (DataSource is INotifyCollectionChanged)
                    {
                        ((INotifyCollectionChanged)dataSource).CollectionChanged += HandleCollectionChangedOfDataSource;
                    }
                }
            }
        }

        protected bool isBusy = false;
        public virtual bool IsBusy
        {
            get
            {
                return isBusy;
            }
            set
            {
                if (value == isBusy)
                {
                    return;
                }
                isBusy = value;
                RaisePropertyChanged("IsBusy");
            }
        }

        public IClientFilter<T> ClientFilter { get; set; }

        public SynchronizationContext SyncContext { get; set; }

        public IDelayedExecution DelayedExecution { get; set; }

        public IGuiThreadHelper GuiThreadHelper { get; set; }

        public IThreadPool ThreadPool { get; set; }

        public long QueueInterval { get; set; }

        protected readonly ISet<String> listeningPropertyNames = new HashSet<String>();

        protected readonly IList<T> unmodifiableValues;

        protected QueueGroupKey queueGroupKey;

        public FilteringMultiContainer()
        {
            // build a single wrapper instance around the internal modifiable ObservableCollection
            unmodifiableValues = new ReadOnlyObservableCollection<T>((ObservableCollection<T>)values);
        }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(ClientFilter, "ClientFilter");
            ParamChecker.AssertNotNull(DataSource, "DataSource");
            ParamChecker.AssertNotNull(DelayedExecution, "DelayedExecution");
            ParamChecker.AssertNotNull(GuiThreadHelper, "GuiThreadHelper");
            ParamChecker.AssertNotNull(ThreadPool, "ThreadPool");
            if (QueueInterval == 0)
            {
                // 250ms as default
                QueueInterval = 250;
            }
            if (QueueInterval > 0)
            {
                queueGroupKey = new QueueGroupKey(QueueInterval, true, FilterValues);
            }
            listeningPropertyNames.Add("Value"); // IModelSingleContainer<T>
            listeningPropertyNames.Add("ValueData");// IModelSingleContainer
            listeningPropertyNames.Add("Values");// IModelMultiContainer<T>
            listeningPropertyNames.Add("ValuesData"); // IModelMultiContainer
            ClientFilter.ClientFilterChanged += OnClientFilterChanged;
        }

        public virtual void Destroy()
        {
            ClientFilter.ClientFilterChanged -= OnClientFilterChanged;
            // Unregister listeners. Cleanup potential memory leak
            DataSource = null;
        }

        public void FilterValues()
        {
            if (DataSource == null)
            {
                return;
            }
            IList<T> readonlyDataSourceContext;
            if (DataSource is IModelMultiContainer<T>)
            {
                readonlyDataSourceContext = ((IModelMultiContainer<T>)DataSource).Values;
            }
            else if (DataSource is IModelSingleContainer<T>)
            {
                readonlyDataSourceContext = new List<T>(1);
                T value = ((IModelSingleContainer<T>)DataSource).Value;
                if (value != null)
                {
                    readonlyDataSourceContext.Add(value);
                }
            }
            else
            {
                throw new NotSupportedException(typeof(IModelContainer).Name + " of type '" + DataSource.GetType() + "' not supported");
            }
            SetBusy();
            if (GuiThreadHelper.IsInGuiThread())
            {
            	// Create a 'safe copy' of the datasource from within gui thread
                IList<T> clonedDataSourceContext = new List<T>(readonlyDataSourceContext);
                ThreadPool.Queue(delegate()
                {
                    try
                    {
                        IList<T> values = ClientFilter.Filter(clonedDataSourceContext);

                        // This will not set the Values-IList<T> Pointer but will clear and reinit its content
                        // This assumption is important because our unmodifiableValues-Property depends on it
                        SyncContext.Send((object state) =>
                        {
                            base.Values = values;
                        }, null);
                    }
                    finally
                    {
                        SetUnbusy();
                    }
                });
            }
            else
            {
                try
                {
                    IList<T> clonedDataSourceContext = null;
                    SyncContext.Send((object state) =>
                    {
                        // Create a 'safe copy' of the datasource from within gui thread
                        clonedDataSourceContext = new List<T>(readonlyDataSourceContext);
                    }, null);

                    IList<T> values = ClientFilter.Filter(clonedDataSourceContext);

                    // This will not set the Values-IList<T> Pointer but will clear and reinit its content
                    // This assumption is important because our unmodifiableValues-Property depends on it
                    SyncContext.Send((object state) =>
                    {
                        base.Values = values;
                    }, null);
                }
                finally
                {
                    SetUnbusy();
                }
            }
        }
        
        protected void QueueFilterValues()
        {
            if (queueGroupKey == null)
            {
                // Queueing deactivated. Fire event immediately
                FilterValues();
                return;
            }
            // This is a no-op, if the given queueGroupKey is already considered pending in the ThreadPool:
            DelayedExecution.Queue(queueGroupKey);
        }

        public void HandlePropertyChangedOfDataSource(Object sender, PropertyChangedEventArgs arg)
        {
            if (!listeningPropertyNames.Contains(arg.PropertyName))
            {
                // We are not interested in PCEs of any unknown property
                return;
            }
            QueueFilterValues();
        }

        public void HandleCollectionChangedOfDataSource(Object sender, NotifyCollectionChangedEventArgs arg)
        {
            QueueFilterValues();
        }
        
        public virtual void OnClientFilterChanged(Object sender, EventArgs e)
        {
            // Ugly to use PropCE here. TODO: Extendable-Pattern with dedicated interfaces
            QueueFilterValues();
        }

        public override T Value
        {
            get
            {
                return base.Value;
            }
            set
            {
                throw new NotSupportedException("This " + typeof(IModelContainer).Name + " is intended as a read-only (potentially filtered) view of another " + typeof(IModelContainer).Name + " as its source");
            }
        }

        public override IList<T> Values
        {
            get
            {
                // Return the unmodifiableValues which is a mirror of the real Values List but does not allow any modification
                // Do NOT clone or copy this list here, because XAML databinding logic may depend on a consistent collection pointer
                return unmodifiableValues;
            }
            set
            {
               throw new NotSupportedException("This " + typeof(IModelContainer).Name + " is intended as a read-only (potentially filtered) view of another " + typeof(IModelContainer).Name + " as its source");
            }
        }

        protected virtual void RaisePropertyChanged(String propertyName)
        {
            GuiThreadHelper.InvokeInGui(delegate()
            {
                var localEventHandler = PropertyChanged;
                if (localEventHandler != null)
                {
                    localEventHandler.Invoke(this, new PropertyChangedEventArgs(propertyName));
                }
            });
        }

        protected void SetBusy()
        {
            lock (busyLock)
            {
                ++busyCount;
                IsBusy = true;
            }
        }

        protected void SetUnbusy()
        {
            lock (busyLock)
            {
                if (busyCount > 0)
                {
                    --busyCount;
                }
                if (busyCount == 0)
                {
                    IsBusy = false;
                }
            }
        }
    }
}
