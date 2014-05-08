using System;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Ink;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Shapes;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;
using De.Osthus.Minerva.Bind;
using System.Collections.Generic;
using De.Osthus.Ambeth.Datachange.Model;
using De.Osthus.Minerva.Security;
using De.Osthus.Ambeth.Filter.Model;

namespace De.Osthus.Minerva.Core
{
    public abstract class AbstractRefresher<T> : IRefresher<T>, IInitializingBean
    {
        public ISecurityScopeProvider SecurityScopeProvider { get; set; }

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(SecurityScopeProvider, "SecurityScopeProvider");
        }

        public virtual IList<T> Refresh(IList<IDataChangeEntry> itemsToRefresh, params object[] contextInformation)
        {
            return Populate(contextInformation);
        }

        public virtual IList<T> GetObjectsFromPagingResponse(IPagingResponse pagingResponse)
        {
            IList<T> result = new List<T>();
            foreach (Object obj in pagingResponse.Result)
            {
                result.Add((T)obj);
            }
            return result;
        }

        public abstract IList<T> Populate(params Object[] contextInformation);
    }
}
