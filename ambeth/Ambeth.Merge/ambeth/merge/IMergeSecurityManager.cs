using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Model;

namespace De.Osthus.Ambeth.Merge
{
    public interface IMergeSecurityManager
    {
        void CheckMergeAccess(ICUDResult cudResult, IMethodDescription methodDescription);
    }
}