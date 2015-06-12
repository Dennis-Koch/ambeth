using System;

namespace De.Osthus.Ambeth.Cache
{
    /// <summary>
    /// Allows to customize the behavior a cache processes requests.<br>
    /// <br>
    /// A valid behavior considers the following rules:<br>
    /// <br>
    /// Rule 1) Either <code>FailEarly</code> or <code>FailInCacheHierarchy</code> or none of them for default behavior in that aspect<br>
    /// Rule 2) Either <codoe>NoResult</code>, <code>LoadContainerResult</code>, <code>CacheValueResult</code> or none of them for default behavior in that aspect<br>
    /// Rule 3) <code>ReturnMisses</code> or nothing for default behavior in that aspect
    /// </summary>
    [Flags]
    public enum CacheDirective
    {
        /// <summary>
        /// Default cache request behavior. Which can be separated in 3 specific aspects:<br>
        /// <br>
        /// Always returns entity instances even if you call this on an instance of RootCache<br>
        /// Does transparently try to resolve cache-misses by cascading through the cache hierarchy and calling remote CacheRetrievers<br>
        /// Does return only cache-hits. So if at least a single miss happened the result does NOT correlate by index with the requested ObjRefs
        /// </summary>
        None = 0,

        /// <summary>
        /// Customizes the default behavior in a specific aspect:<br>
        /// 2) Does NOT try to resolve cache-misses. It does NOT cascade through the cache hierarchy<br>
        /// </summary>
        FailEarly = 1,

        /// <summary>
        /// Customizes the default behavior in a specific aspect:<br>
        /// Does transparently try to resolve cache-misses by cascading through the cache hierarchy but NOT calling remote CacheRetrievers<br>
        /// </summary>
        FailInCacheHierarchy = 2,

        /// <summary>
        /// Customizes the default behavior in a specific aspect:<br>
        /// 3) Does return cache-hits as well as null entries for cache-misses. So the result does always correlate by index with the requested ObjRefs
        /// </summary>
        ReturnMisses = 4,

        /// <summary>
        /// Customizes the default behavior in a specific aspect:<br>
        /// 3) Does not return any result<br>
        /// <br>
        /// This may be useful to "ensure" that a cache contains an entry after the request. But keep in mind that this makes no sense if the cache refers weakly to
        /// its entries because there is no guarantee how long the cache is able to hold the target instance due to GC
        /// </summary>
        NoResult = 8,

        /// <summary>
        /// Customizes the default behavior in a specific aspect:<br>
        /// 3) Always returns <code>ILoadContainer</code> instances. If you call this on a <code>ChildCache</code> the request is directly passed through the parent
        /// <code>RootCache</code> and executed there to build the response<br>
        /// </summary>
        LoadContainerResult = 16,

        /// <summary>
        /// Customizes the default behavior in a specific aspect:<br>
        /// 3) Always returns the real internal cache entries. If you call this on a <code>ChildCache</code> the request is directly passed through the parent
        /// <code>RootCache</code> and executed there to build the response. A <code>RootCache</code> returns the direct reference to the internal cache entry.<br>
        /// <br>
        /// As long as you hold a hard reference to the result it can be ensured that even for weakly referencing <code>ChildCache</code> or <code>RootCache</code>
        /// instances the internal entries are NOT lost due to GC.<br>
        /// <br>
        /// CAUTION: Any read or write access to these exposed cache instances in the result are not thread-safe and the resulting behavior is undefined.
        /// </summary>
        CacheValueResult = 32
    }
}