using De.Osthus.Ambeth.Merge.Incremental;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Model;
using System;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Merge
{
    public interface IMergeServiceExtension
    {
        IOriCollection Merge(ICUDResult cudResult, IMethodDescription methodDescription);

        ICUDResult EvaluateImplicitChanges(ICUDResult cudResult, IIncrementalMergeState incrementalState);

        IList<IEntityMetaData> GetMetaData(IList<Type> entityTypes);

    	IValueObjectConfig GetValueObjectConfig(Type valueType);
    }
}
