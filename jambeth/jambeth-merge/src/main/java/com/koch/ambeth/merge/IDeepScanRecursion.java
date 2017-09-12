package com.koch.ambeth.merge;

/**
 * Allows to apply a generic algorithm of crawling through collections and arrays of entities and
 * calling a provided delegate for each resolved entity instance. It is also ensured that a resolved
 * entity is forwarded exactly once.
 */
public interface IDeepScanRecursion {
	/**
	 * The handle provided to the {@link EntityDelegate#visitEntity(Object, Proceed)} when called by
	 * the {@link IDeepScanRecursion#handleDeep(Object, EntityDelegate)} algorithm. It allows to
	 * proceed recursively with a given object handle that - in most cases - may have been resolved by
	 * accessing specific properties of the previous passed on entity instance (e.g. a relational
	 * value).
	 */
	interface Proceed {
		boolean proceed(Object obj);

		boolean proceed(Object obj, EntityDelegate entityDelegate);
	}

	/**
	 * The delegate provided initially to the
	 * {@link IDeepScanRecursion#handleDeep(Object, EntityDelegate) algorithm. The delegate be locally
	 * exchanged can during recursion when using the {@link Proceed#proceed(Object, EntityDelegate)}
	 * overload. As long as {@link Proceed#proceed(Object)} is used the current delegate is
	 * continuously applied.
	 */
	interface EntityDelegate {
		/**
		 * Called by the recursion algorithm for each discovered entity. This method is called once per
		 * each discovered instance per initial call to
		 * {@link IDeepScanRecursion#handleDeep(Object, EntityDelegate)}.
		 *
		 * @param entity The entity discovered by crawling through the initial object handle on any
		 *        depth
		 * @param proceed The handle to allow to crawl "deeper" through the object graph by reusing the
		 *        existing recursion session (means that it is further guaranteed to not produce a cycle
		 *        - each resolved entity, entity-containing collection or entity-containing array at any
		 *        depth is only processed once)
		 * @return false if the complete recursion shall terminate (that is: no other entities are
		 *         resolved and the outer {@link IDeepScanRecursion#handleDeep(Object, EntityDelegate)}
		 *         call will terminate. True if the algorithm shall further proceed with other entities.
		 */
		boolean visitEntity(Object entity, Proceed proceed);
	}

	/**
	 * Instantiates a new recursive process to "crawl through" the given obj instance and calling the
	 * specific delegate for each discovered entity instance.
	 *
	 * @param obj The initial object handle to start the crawling algorithm. It may be any type of
	 *        {@link Iterable}, array or a single entity instance.
	 * @param entityDelegate The delegate which receives any discovered entity instance. This delegate
	 *        may "finish" the recursion at that step or it may invoke an additional depth of
	 *        recursion by calling one of the methods of the passed on {@link Proceed} handle.
	 */
	void handleDeep(Object obj, EntityDelegate entityDelegate);
}
