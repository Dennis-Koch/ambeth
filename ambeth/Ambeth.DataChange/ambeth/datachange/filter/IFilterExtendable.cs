using System;
using System.Net;

namespace De.Ostus.Ambeth.DataChange.Filter
{
    public interface IFilterExtendable
    {
        void RegisterFilter(IFilter filter, String topic);

        void UnregisterFilter(IFilter filter, String topic);
    }
}
