using System;
using System.ComponentModel;

namespace De.Osthus.Minerva.Core
{
    public interface IModelSingleContainer : IModelContainer
    {
        Object ValueData { get; set; }
    }

    public interface IModelSingleContainer<T> : IModelSingleContainer, INotifyPropertyChanged
    {
        T Value { get; set; }
    }
}
