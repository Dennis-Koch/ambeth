using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Datachange.Model;
using De.Osthus.Ambeth.Filter.Model;

namespace De.Osthus.Minerva.Core
{
    public interface IPagingRefresher<T> : IBaseRefresher<T>
    {
        IPagingResponse Refresh(IList<IDataChangeEntry> itemsToRefresh, IFilterDescriptor filterDescriptor, IList<ISortDescriptor> sortDescriptors, IPagingRequest pagingRequest, params Object[] contextInformation);

        IPagingResponse Populate(IFilterDescriptor filterDescriptor, IList<ISortDescriptor> sortDescriptors, IPagingRequest pagingRequest, params Object[] contextInformation);
    }
}
