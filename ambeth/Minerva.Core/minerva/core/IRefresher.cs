using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Datachange.Model;

namespace De.Osthus.Minerva.Core
{
    public interface IRefresher<T> : IBaseRefresher<T>
    {
        IList<T> Refresh(IList<IDataChangeEntry> itemsToRefresh, params Object[] contextInformation);

        IList<T> Populate(params Object[] contextInformation);
    }
}
