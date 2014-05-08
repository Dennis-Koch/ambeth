using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Datachange.Model;

namespace De.Ostus.Ambeth.DataChange.Filter
{
    public interface IFilterRegistry
    {
        IList<String> EvaluateMatchingTopics(IDataChangeEntry dataChangeEntry);
    }
}
