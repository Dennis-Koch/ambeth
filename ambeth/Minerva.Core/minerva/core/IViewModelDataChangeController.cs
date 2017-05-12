using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Threading;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Datachange;
using De.Osthus.Ambeth.Event;
using De.Osthus.Ambeth.Datachange.Model;

namespace De.Osthus.Minerva.Core
{
    public interface IViewModelDataChangeController<T> : IViewModelDataChangeController
    {
        void Update(IList<T> itemsToUpdate, OnErrorCallback<T> onErrorCallback, OnSuccessCallback<T> onSuccessCallback);

        void Delete(IList<T> itemsToDelete, OnErrorCallback<T> onErrorCallback, OnSuccessCallback<T> onSuccessCallback);

        void Update(T itemToUpdate, OnErrorCallback<T> onErrorCallback, OnSuccessCallback<T> onSuccessCallback);

        void Delete(T itemToDelete, OnErrorCallback<T> onErrorCallback, OnSuccessCallback<T> onSuccessCallback);
    }

    public interface IViewModelDataChangeController
    {
        void Populate();
    }

    public delegate void OnErrorCallback<T>(T relatedObject, Exception e);

    public delegate void OnSuccessCallback<T>(T relatedObject);
}
