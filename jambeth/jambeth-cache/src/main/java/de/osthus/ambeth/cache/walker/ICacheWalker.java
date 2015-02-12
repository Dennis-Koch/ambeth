package de.osthus.ambeth.cache.walker;

import de.osthus.ambeth.merge.model.IObjRef;

/**
 * Scans the cache hierarchy for the cache-individual occurrence of given entity instances. It resolves all potentially thread-local or transactional proxy
 * instances which greatly reduces debugging complexity for a developer.<br>
 * <br>
 * The walking algorithm is thread-safe and does not change or load anything into the analyzed cache instances by itself - however: after any of its operation
 * terminates there is no guarantee that the cache instances are still in the same state as they were during the "dump" because of potential concurrent
 * operations - e.g. DataChangeEvents (DCEs).<br>
 * <br>
 * CAUTION: Use this functionality ONLY for debugging purpose. Do NEVER transfer or safe information gained from the Walker to anywhere than in a debugger view
 * (e.g. "variable introspection" or "expression view" in an IDE). In some cases it MAY be intentional to store the dump in an INTERNAL log-file. Do NOT "work"
 * with the referred cache instances - keep in mind that even read-accesses may return an inconsistent result compared to the time the walker did its job.<br>
 * <br>
 * In addition the walking algorithm UNDERGOES intentionally a potential Ambeth Security Layer: The response contains information about cache states which would
 * never be available (filtered) for a thread-bound authenticated user.
 */
public interface ICacheWalker
{
	/**
	 * Walks the current cache hierarchy up beginning from a (potentially thread-bound) 1st level cache (ChildCache) to all active instances of 2nd level caches
	 * (RootCache) including thread-bound or transaction-bound instances.
	 * 
	 * @param objRefs
	 *            References to the entities to look for while walking
	 * @return Tree-like structure starting always from the committed root cache instance (top-down)
	 */
	ICacheWalkerResult walk(IObjRef... objRefs);

	/**
	 * Walks the current cache hierarchy up beginning from a (potentially thread-bound) 1st level cache (ChildCache) to all active instances of 2nd level caches
	 * (RootCache) including thread-bound or transaction-bound instances.
	 * 
	 * @return Tree-like structure starting always from the committed root cache instance (top-down)
	 */
	ICacheWalkerResult walkAll();

	/**
	 * "Walks" the current cache hierarchy up beginning from a (potentially thread-bound) 1st level cache (ChildCache) to all active instances of 2nd level
	 * caches (RootCache) including thread-bound or transaction-bound instances.
	 * 
	 * @param entities
	 *            Explicit entity instances from which ObjRefs will be built to look for while walking.
	 * @return Tree-like structure starting always from the committed root cache instance (top-down)
	 */
	<T> ICacheWalkerResult walkEntities(T... entities);
}