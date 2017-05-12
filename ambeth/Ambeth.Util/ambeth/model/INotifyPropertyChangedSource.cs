using System;
using System.Net;
using System.ComponentModel;
using System.Collections.Generic;
using System.Collections.Specialized;
using De.Osthus.Ambeth.Collections.Specialized;

namespace De.Osthus.Ambeth.Model
{
    public interface INotifyPropertyChangedSource
    {
        NotifyCollectionChangedEventHandler CollectionEventHandler { get; }

        PropertyChangedEventHandler ParentChildEventHandler { get; }

        PropertyChangeSupport PropertyChangeSupport { get; }

        void OnPropertyChanged(String propertyName);

        void OnPropertyChanged(String propertyName, Object oldValue, Object newValue);
    }
}