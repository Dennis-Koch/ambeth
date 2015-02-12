using De.Osthus.Ambeth.Merge.Incremental;
using De.Osthus.Ambeth.Merge.Model;
using System;

namespace De.Osthus.Ambeth.Merge
{
    public interface ICUDResultPrinter
    {
        String PrintCUDResult(ICUDResult cudResult, IIncrementalMergeState state);
    }
}