using System;
using System.Collections;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Collections.Specialized;
using System.ComponentModel;

namespace De.Osthus.Minerva.Core
{
    public class ModelMultiContainer<T> : IModelMultiContainer<T>, IModelSingleContainer<T>
    {
        protected readonly IList<T> values = new ObservableCollection<T>();

        public virtual event PropertyChangedEventHandler PropertyChanged;
        public virtual event NotifyCollectionChangedEventHandler CollectionChanged;

        public virtual int Count
        {
            get
            {
                return values.Count;
            }
        }

        public virtual T Value
        {
            get
            {
                if (values.Count > 0)
                {
                    return values[0];
                }
                return default(T);
            }
            set
            {
                if (value == null)
                {
                    if (values.Count == 0)
                    {
                        // Nothing to do
                        return;
                    }
                    // Remove the first item from the list (which is the pendant to the nulled single-value 'Value')
                    // After this there may be another object at index 0 which somehow may be irritating because
                    // Value has been set null but has surprisingly another non-null value with the next get
                    // This behavior is intended
                    values.RemoveAt(0);
                }
                else if (values.Count == 0)
                {
                    values.Add(value);
                }
                else
                {
                    if (Object.ReferenceEquals(values[0], value))
                    {
                        return;
                    }
                    values[0] = value;
                }
                OnPropertyChanged("Value");
            }
        }

        public virtual IList<T> Values
        {
            get
            {
                return values;
            }
            set
            {
                if (value == null || value.Count == 0)
                {
                    if (values.Count == 0)
                    {
                        // Nothing to do
                        return;
                    }
                }
                T oldValue = Value;
                values.Clear();
                if (value == null)
                {
                    // Values = null leaves an empty Collection
                    return;
                }
                for (int i = 0; i < value.Count; ++i)
                {
                    values.Add(value[i]);
                }
                T newValue = Value;
                // The Value-property did not yet note any change to its underlying values[0] relationship
                // Now we lock for a potential change afterwards
                if (!Object.ReferenceEquals(oldValue, newValue))
                {
                    OnPropertyChanged("Value");
                }
                // The Values-Property itself has not been changed. The reference remains always the same!
                // So we intentionally do not fire a PropertyChanged for 'Values'
            }
        }
        
        public ModelMultiContainer()
        {
            if (Values is INotifyCollectionChanged)
            {
                ((INotifyCollectionChanged)Values).CollectionChanged += OnCollectionChanged;
            }
        }

        protected void OnCollectionChanged(Object sender, NotifyCollectionChangedEventArgs e)
        {
            if (CollectionChanged != null)
            {
                CollectionChanged.Invoke(sender, e);
            }
        }

        public void OnPropertyChanged(String propertyName)
        {
            if (PropertyChanged != null)
            {
                PropertyChanged.Invoke(this, new PropertyChangedEventArgs(propertyName));
            }
        }

        public IEnumerable ValuesData
        {
            get
            {
                return Values;
            }
        }

        public Object ValueData
        {
            get
            {
                return Value;
            }
            set
            {
                Value = (T)value;
            }
        }
    }
}
