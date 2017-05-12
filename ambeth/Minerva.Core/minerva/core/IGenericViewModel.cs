using System;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Collections.Specialized;
using System.Collections.Generic;

namespace De.Osthus.Minerva.Core
{
    public interface IGenericViewModel<T> : IGenericViewModel, INotifyCollectionChanged, INotPersistedDataContainer<T>, IModelMultiContainer<T>
    {
        IList<T> Objects { get; }

        IList<T> GetClonedObjects();

        void RemoveAt(int index);

        void Add(T newObject);

        void InsertAt(int index, T newObject);

        void Replace(int index, T newObject);

        void ClearChangedObjects();
    }

    public interface IGenericViewModel : INotifyPropertyChanged, INotPersistedDataContainer, IModelMultiContainer
    {
        bool IsBusy { get; }

        int PageSize { get; set; }

        int PageIndex { get; set; }

        int ItemCount { get; set; }
    }
}
