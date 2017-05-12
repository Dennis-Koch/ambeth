using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Merge.Model;

namespace De.Osthus.Ambeth.Merge
{
    public interface IMergeListener
    {
        ICUDResult PreMerge(ICUDResult cudResult, ICache cache);

        void PostMerge(ICUDResult cudResult, IObjRef[] updatedObjRefs);
    }
}