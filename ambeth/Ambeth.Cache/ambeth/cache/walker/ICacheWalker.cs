using De.Osthus.Ambeth.Merge.Model;

namespace De.Osthus.Ambeth.Walker
{
    /// <summary>
    /// Scans the cache hierarchy for the cache-individual occurrence of given entity instances. It resolves all potentially thread-local or transactional proxy
    /// instances which greatly reduces debugging complexity for a developer.<br>
    /// <br>
    /// The walking algorithm is thread-safe and does not change or load anything into the analyzed cache instances by itself - however: after any of its operation
    /// terminates there is no guarantee that the cache instances are still in the same state as they were during the "dump" because of potential concurrent
    /// operations - e.g. DataChangeEvents (DCEs).<br>
    /// <br>
    /// CAUTION: Use this functionality ONLY for debugging purpose. Do NEVER transfer or safe information gained from the Walker to anywhere than in a debugger view
    /// (e.g. "variable introspection" or "expression view" in an IDE). In some cases it MAY be intentional to store the dump in an INTERNAL log-file. Do NOT "work"
    /// with the referred cache instances - keep in mind that even read-accesses may return an inconsistent result compared to the time the walker did its job.<br>
    /// <br>
    /// In addition the walking algorithm UNDERGOES intentionally a potential Ambeth Security Layer: The response contains information about cache states which would
    /// never be available (filtered) for a thread-bound authenticated user.
    /// </summary>
    public interface ICacheWalker
    {
        /// <summary>
        /// Walks the current cache hierarchy up beginning from a (potentially thread-bound) 1st level cache (ChildCache) to all active instances of 2nd level caches
        /// (RootCache) including thread-bound or transaction-bound instances.
        /// </summary>
        /// <param name="objRefs">References to the entities to look for while walking</param>
        /// <returns>Tree-like structure starting always from the committed root cache instance (top-down)</returns>
        ICacheWalkerResult Walk(params IObjRef[] objRefs);

        /// <summary>
        /// Walks the current cache hierarchy up beginning from a (potentially thread-bound) 1st level cache (ChildCache) to all active instances of 2nd level caches
        /// (RootCache) including thread-bound or transaction-bound instances.
        /// </summary>
        /// <returns>Tree-like structure starting always from the committed root cache instance (top-down)</returns>
        ICacheWalkerResult WalkAll();

        /// <summary>
        /// "Walks" the current cache hierarchy up beginning from a (potentially thread-bound) 1st level cache (ChildCache) to all active instances of 2nd level
        /// caches (RootCache) including thread-bound or transaction-bound instances.
        /// </summary>
        /// <typeparam name="T"></typeparam>
        /// <param name="entities">Explicit entity instances from which ObjRefs will be built to look for while walking.</param>
        /// <returns>Tree-like structure starting always from the committed root cache instance (top-down)</returns>
        ICacheWalkerResult WalkEntities<T>(params T[] entities);
    }
}