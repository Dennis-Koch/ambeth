using System;

namespace De.Osthus.Ambeth.Merge
{
    public interface IMergeProcess
    {
        void Process(Object objectToMerge, Object objectToDelete, ProceedWithMergeHook proceedHook, MergeFinishedCallback mergeFinishedCallback);

        void Process(Object objectToMerge, Object objectToDelete, ProceedWithMergeHook proceedHook, MergeFinishedCallback mergeFinishedCallback, bool addNewEntitiesToCache);
    }    
}
