using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Cache.Rootcachevalue;
using De.Osthus.Ambeth.Metadata;

namespace De.Osthus.Ambeth.Util
{
    public class IndirectValueHolderRef : DirectValueHolderRef
    {
	    protected readonly RootCache rootCache;

	    public IndirectValueHolderRef(RootCacheValue cacheValue, RelationMember member, RootCache rootCache) : base(cacheValue, member)
	    {
		    this.rootCache = rootCache;
	    }

        public IndirectValueHolderRef(RootCacheValue cacheValue, RelationMember member, RootCache rootCache, bool objRefsOnly)
            : base(cacheValue, member, objRefsOnly)
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
