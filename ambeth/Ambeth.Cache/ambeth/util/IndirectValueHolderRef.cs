using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Cache.Rootcachevalue;
using De.Osthus.Ambeth.Typeinfo;

namespace De.Osthus.Ambeth.Util
{
    public class IndirectValueHolderRef : DirectValueHolderRef
    {
	    protected readonly RootCache rootCache;

	    public IndirectValueHolderRef(RootCacheValue cacheValue, IRelationInfoItem member, RootCache rootCache) : base(cacheValue, member)
	    {
		    this.rootCache = rootCache;
	    }

	    public RootCache RootCache
	    {
            get
            {
                return rootCache;
            }
	    }
    }
}
