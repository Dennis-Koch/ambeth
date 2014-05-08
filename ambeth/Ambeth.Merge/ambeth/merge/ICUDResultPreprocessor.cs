
namespace De.Osthus.Ambeth.Merge
{
    public interface ICUDResultPreprocessor
    {
        ProceedWithMergeHook GetProceedWithMergeHook();

        void CleanUp(ProceedWithMergeHook proceedWithMergeHook);

        // Returns null, if preprocessing has not yet finished
        bool? GetPreprocessSuccess(ProceedWithMergeHook proceedWithMergeHook);
    }
}
