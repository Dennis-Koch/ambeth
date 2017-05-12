using System.Collections.Generic;
using System.ComponentModel;

namespace De.Osthus.Minerva.Core
{
    public interface IClientFilter<T> : INotifyClientFilterChanged
    {
        IList<T> Filter(IList<T> bOsToFilter);
    }
}
