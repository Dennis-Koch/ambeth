using System.Collections;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.ComponentModel;

namespace De.Osthus.Minerva.Core
{
    public interface IModelMultiContainer : IModelContainer
    {
        IEnumerable ValuesData { get; }
    }

    public interface IModelMultiContainer<T> : IModelMultiContainer, INotifyPropertyChanged, INotifyCollectionChanged
    {
        IList<T> Values { get; set; }
    }
}
