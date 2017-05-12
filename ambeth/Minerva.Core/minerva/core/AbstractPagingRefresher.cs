using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Datachange.Model;
using De.Osthus.Ambeth.Filter.Model;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;
using De.Osthus.Minerva.Security;

namespace De.Osthus.Minerva.Core
{
    public abstract class AbstractPagingRefresher<T> : IPagingRefresher<T>, IInitializingBean
    {
        public ISecurityScopeProvider SecurityScopeProvider { get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(SecurityScopeProvider, "SecurityScopeProvider");
        }

        public virtual IPagingResponse Refresh(IList<IDataChangeEntry> itemsToRefresh, IFilterDescriptor filterDescriptor, IList<ISortDescriptor> sortDescriptors, IPagingRequest pagingRequest, params Object[] contextInformation)
        {
            return Populate(filterDescriptor, sortDescriptors, pagingRequest, contextInformation);
        }

        public abstract IPagingResponse Populate(IFilterDescriptor filterDescriptor, IList<ISortDescriptor> sortDescriptors, IPagingRequest pagingRequest, params Object[] contextInformation);
    }
}
